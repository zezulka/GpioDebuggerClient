package core.net;

import core.util.MessageParser;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import javafx.application.Platform;
import gui.layouts.controllers.ControllerUtils;
import gui.layouts.controllers.MasterWindowController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.InterruptManager;

public final class ConnectionThread implements Runnable {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(ConnectionThread.class);

    private final ConnectionValueObject connection;

    public ConnectionThread(ConnectionValueObject connection) {
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
                if (!connection.getSelector().isOpen()) {
                    break;
                }
                Iterator<SelectionKey> keys = connection
                        .getSelector()
                        .selectedKeys()
                        .iterator();
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
        return connection.getSelector().isOpen()
                && connection.getChannel().isOpen()
                && connection.getChannel().isConnected();
    }

    private void processSelectionKeys(Iterator<SelectionKey> keys)
            throws IOException {
        if (keys == null) {
            return;
        }
        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();
            if (!key.isValid()) {
                LOGGER.error(String.format("Invalid key registered: %s", key));
                continue;
            }
            if (key.isConnectable() && !connect(key)) {
                break;
            }
            if (key.isWritable() && connection.getMessageToSend() != null) {
                write(key);
            }
            if (key.isReadable()) {
                processAgentMessage();
            }
        }
    }

    private void processAgentMessage() {
        String agentMessage = read();
        if (agentMessage != null) {
            MessageParser.parseAgentMessage(connection, agentMessage);
        } else {
            LOGGER.debug("disconnecting from agent...");
            disconnect();
            if (connection.getDevice().isDirty()) {
                Platform.runLater(() -> {
                    MasterWindowController.getTabManager()
                            .removeTab(connection.getDevice().getAddress());
                });
                ControllerUtils.showInfoDialog(
                        String.format("Disconnected from address %s, device %s",
                                connection.getDevice().getAddress(),
                                connection.getDevice().getBoardType())
                );
            }
        }
    }

    private String read() {
        try {
            ByteBuffer readBuffer
                    = ByteBuffer.allocate(NetworkManager.BUFFER_SIZE);
            readBuffer.clear();
            int length;
            length = connection.getChannel().read(readBuffer);
            if (length == NetworkManager.END_OF_STREAM) {
                LOGGER.debug("reached end of the input stream");
                return null;
            }
            readBuffer.flip();
            byte[] buff = new byte[NetworkManager.BUFFER_SIZE];
            readBuffer.get(buff, 0, length);
            return (new String(buff).replaceAll("\0", ""));
        } catch (IOException ex) {
            ControllerUtils.showErrorDialog(
                    "There has been an error reading message from agent."
                    + "Either agent is not running on the IP supplied or "
                    + "network connection failure has occured.\n"
            );
            LOGGER.error(ex.getLocalizedMessage());
            disconnect();
            return null;
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
        try {
            connection.getChannel().close();
        } catch (IOException ex) {
            //ignore for the time being
        }
        connection.getDevice().disconnectedProperty().set(true);
        InterruptManager.clearAllListeners(connection.getDevice().getAddress());
        NetworkManager.removeMapping(connection.getDevice().getAddress());
    }

    private void write(SelectionKey key) throws IOException {
        if (connection.getMessageToSend() == null) {
            throw new IllegalStateException("cannot send null message");
        }
        connection
                .getChannel()
                .write(ByteBuffer.wrap(connection
                        .getMessageToSend()
                        .getBytes()));
        key.interestOps(SelectionKey.OP_READ);
        connection.setMessageToSend(null);
    }

    private boolean connect(SelectionKey key) {
        try {
            SocketChannel channel = connection.getChannel();
            channel.connect(
                    new InetSocketAddress(connection.getDevice().getAddress(),
                            NetworkManager.DEFAULT_SOCK_PORT)
            );
            if (channel.isConnectionPending() && channel.finishConnect()) {
                LOGGER.info("done connecting to server");
            } else {
                return false;
            }
            channel.configureBlocking(false);
            key.interestOps(SelectionKey.OP_READ);
            connection.getDevice().disconnectedProperty().set(false);
            return true;
        } catch (IOException ex) {
            LOGGER.error("Could not connect to server, reason: ", ex);
            ControllerUtils
                    .showErrorDialog("Could not connect to server.");
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
                connection.getChannel().register(connection.getSelector(),
                        SelectionKey.OP_WRITE);
                connection.getSelector().wakeup();
            } catch (ClosedChannelException ex) {
                LOGGER.error("There has been an attempt to "
                        + "register write operation on channel "
                        + "which has been closed.", ex);
                throw new IllegalStateException();
            }
        }
    }

}
