package core.net;

import core.gui.App;
import static core.net.ClientNetworkManager.DEFAULT_SOCK_PORT;
import core.util.MessageParser;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import javafx.application.Platform;
import layouts.controllers.ControllerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.BoardType;

public class ClientConnectionThread implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectionThread.class);

    private final AgentConnectionValueObject connection;

    public ClientConnectionThread(AgentConnectionValueObject connection) {
        this.connection = connection;
    }

    /**
     * Main loop of the application logic. An attempt is made to initialise this
     * manager (singleton); if manager succeeded in connecting to the supplied
     * IP address, it then tries to receive "handshake" message, which contains
     * name of the device. If handshake message is valid (=parse completed
     * successfully), it then invokes appropriate method to switch to the given
     * scene which enables user to control the device the client connected to.
     *
     * While the agent is alive, this thread iterates through selection keys and
     * deals with them (this includes reading messages from input stream or
     * writing client messages to output stream).
     */
    @Override
    public void run() {
        iterateThroughRegisteredKeys();
    }

    private void iterateThroughRegisteredKeys() {
        try {
            while (true) {
                connection.getSelector().select();
                if(!connection.getSelector().isOpen()) {
                    break;
                }
                Iterator<SelectionKey> keys = connection.getSelector().selectedKeys().iterator();
                processSelectionKeys(keys);
                if (!isAlive()) {
                    break;
                }
            }
        } catch (IOException ex) {
            LOGGER.error(null, ex);
        } finally {
            cleanUpResources();
        }
    }

    /**
     * Checks whether device on the supplied IP address is alive.
     *
     * @param ipAddress
     * @return true if alive, false otherwise
     */
    private boolean isAlive() {
        return connection.getSelector().isOpen() && connection.getChannel().isOpen() && connection.getChannel().isConnected();
    }

    private void processSelectionKeys(Iterator<SelectionKey> keys) throws IOException {
        if (keys == null) {
            return;
        }
        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();
            if (!key.isValid()) {
                LOGGER.error(String.format("A nonvalid key has been registered: %s", key.toString()));
                continue;
            }
            if (key.isConnectable() && !connect(key, connection.getInetAddress(), connection.getChannel())) {
                break;
            }
            if (key.isWritable() && connection.getMessageToSend() != null) {
                write(key, connection);
            }
            if (key.isReadable()) {
                if (connection.getBoardType() == null)  {
                    if(readInitMessage()) {
                        App.loadNewTab(connection.getInetAddress(), connection.getBoardType());
                    }
                } else {
                    processAgentMessage();
                }
            }
        }
    }

    private void processAgentMessage() {
        String agentMessage = read();
        if (agentMessage != null) {
            MessageParser.parseAgentMessage(agentMessage);
        } else {
            LOGGER.debug("disconnecting from agent...");
            disconnect();
            Platform.runLater(() -> {
                App.getDevicesTab()
                    .getTabs()
                    .remove(App.getTabFromInetAddress(connection.getInetAddress()));
            });
            ControllerUtils.showInformationDialogMessage(String.format("Disconnected from address %s, device %s", 
                    connection.getInetAddress(), connection.getBoardType()));
        }
    }

    private String read() {
        try {
            ByteBuffer readBuffer = ByteBuffer.allocate(1000);
            readBuffer.clear();
            int length;
            length = connection.getChannel().read(readBuffer);
            if (length == -1) {
                LOGGER.debug("reached end of the input stream");
                connection.getChannel().close();
                return null;
            }
            readBuffer.flip();
            byte[] buff = new byte[1024];
            readBuffer.get(buff, 0, length);
            return (new String(buff).replaceAll("\0", ""));
        } catch (IOException ex) {
            ControllerUtils.showErrorDialogMessage("There has been an error reading message from agent."
                    +    "Either agent is not running on the IP supplied or network connection failure has occured.\n");
            LOGGER.error(ex.getLocalizedMessage());
            disconnect();
            return null;
        }
    }

    private boolean readInitMessage() {
        String agentMessage = read();
        if(agentMessage == null) {
            return false;
        }
        try {
            connection.setBoardType(BoardType.parse(agentMessage));
            ClientNetworkManager.addNewMapping(connection.getInetAddress(), this);
            return true;
        } catch (IllegalArgumentException ex) {
            LOGGER.error("could not parse agent init message");
            return false;
        }
    }

    /**
     * Causes manager to close all resources binded to the existing connection.
     * If connection has not been established, invocation of this method is a
     * no-op (regarding network resources).
     *
     */
    public void disconnect() {
        cleanUpResources();
        ClientNetworkManager.removeMapping(connection.getInetAddress());
    }

    private void write(SelectionKey key, AgentConnectionValueObject connection) throws IOException {
        if (connection.getMessageToSend() == null) {
            throw new IllegalStateException("cannot send null message to agent");
        }
        connection.getChannel().write(ByteBuffer.wrap(connection.getMessageToSend().getBytes()));
        key.interestOps(SelectionKey.OP_READ);
        connection.setMessageToSend(null);
    }

    private boolean connect(SelectionKey key, InetAddress ipAddress, SocketChannel channel) {
        try {
            channel.connect(new InetSocketAddress(ipAddress, DEFAULT_SOCK_PORT));
            if (channel.isConnectionPending() && channel.finishConnect()) {
                LOGGER.info("done connecting to server");
            } else {
                return false;
            }
            channel.configureBlocking(false);
            key.interestOps(SelectionKey.OP_READ);
            return true;
        } catch (IOException ex) {
            LOGGER.error("Could not connect to server, reason: ", ex);
            return false;
        }
    }

    /**
     *
     * @throws IllegalArgumentException if no connection to {@code ipAddress}
     * exists.
     */
    private void cleanUpResources() {
        if (connection == null) {
            throw new IllegalArgumentException("connection does not exist");
        }
        SocketChannel channel = connection.getChannel();
        Selector selector = connection.getSelector();
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
        } catch (IOException ex) {
            LOGGER.error(null, ex);
        }
    }

    /**
     * Given message is stored in the appropriate variable and registers this
     * message in SocketChannel to be sent via output stream to agent.
     *
     * @param message
     */
    public void setMessageToSend(String message) {
        connection.setMessageToSend(message);
        if (message != null) {
            try {
                connection.getChannel().register(connection.getSelector(), SelectionKey.OP_WRITE);
                connection.getSelector().wakeup();
            } catch (ClosedChannelException ex) {
                LOGGER.error("There has been an attempt to "
                        + "register write operation on channel which has been closed.", ex);
                throw new IllegalStateException();
            }
        }
    }

}
