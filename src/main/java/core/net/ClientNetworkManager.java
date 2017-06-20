package core.net;

import core.Main;
import core.util.MessageParser;
import layouts.controllers.GuiEntryPoint;
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
import layouts.controllers.InterruptTableController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protocol.BoardType;
import protocol.InterruptValueObject;
import protocol.ProtocolMessages;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class ClientNetworkManager implements Runnable {

    private static String messageToSend = null;
    private static Selector selector;
    private static String ipAddress;
    private static BoardType boardType;
    private static SocketChannel channel;

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static final int DEFAULT_SOCK_PORT = 8088;
    private static final int TIMEOUT = 5 * 1000;

    private static final ClientNetworkManager CM = new ClientNetworkManager();

    public static ClientNetworkManager getInstance() {
        return CM;
    }

    private ClientNetworkManager() {
    }

    public void connectToDevice(String ipAddress) {
        setIpAddress(ipAddress);
        new Thread(this).start();
    }
    
    public static void disconnect() {
        cleanUpResources();
        GuiEntryPoint.switchToIpPrompt();
    }

    private static void cleanUpResources() {
        try {
            if (channel != null) {
                channel.close();
            }
            if (selector != null) {
                selector.close();
            }
        } catch (IOException ex) {
            LOGGER.error(null, ex);
        }
        selector = null;
        ipAddress = null;
        channel = null;
        boardType = null;
    }

    private static boolean isAlive() {
        return channel != null && channel.isConnected();
    }

    private boolean initManager() {
        if (selector != null || channel != null) {
            cleanUpResources();
            throw new IllegalStateException("Manager has already been initialized!");
        }
        if (ipAddress == null) {
            return false;
        }
        setIpAddress(ipAddress);
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(true);

            InetAddress inetAddress = InetAddress.getByAddress(getIpAddrArray(ipAddress));
            if (!inetAddress.isReachable(TIMEOUT)) {
                Platform.runLater(() -> {
                    GuiEntryPoint.provideFeedback(String.format("Host %s could not be reached.", ipAddress));
                });
                cleanUpResources();
                return false;
            } else {
                LOGGER.debug(String.format("Host %s is reachable", ipAddress));
            }
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException ex) {
            LOGGER.error(null, ex);
            cleanUpResources();
            return false;
        }
        return true;
    }

    private byte[] getIpAddrArray(String ipAddress) {
        if (ipAddress == null) {
            throw new IllegalArgumentException("ip address cannot be null");
        }
        final int arrSize = 4;
        String[] octets = ipAddress.split("\\.", arrSize);
        byte[] ipAddr = new byte[arrSize];
        for (int i = 0; i < arrSize; i++) {
            ipAddr[i] = (byte) Integer.parseInt(octets[i]);
        }
        return ipAddr;
    }

    public void setIpAddress(String ipAddress) {
        if (ipAddress == null) {
            throw new IllegalArgumentException("ip address cannot be null!");
        }
        ClientNetworkManager.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public static void setMessageToSend(String message) {
        messageToSend = message;
        if (message != null) {
            try {
                channel.register(selector, SelectionKey.OP_WRITE);
                selector.wakeup();
            } catch (ClosedChannelException ex) {
                channel = null;
                LOGGER.error("There has been an attempt to "
                        + "register write operation on channel which has been closed.", ex);
            }
        }
    }

    private void setBoardType(BoardType type) {
        this.boardType = type;
    }

    public BoardType getBoardType() {
        return this.boardType;
    }

    @Override
    public void run() {
        while (true) {
            if (!initManager()) {
                Platform.runLater(() -> {
                    GuiEntryPoint.provideFeedback(ProtocolMessages.C_ERR_CANNOT_CONNECT.toString());
                });
                return;
            }
            iterateThroughRegisteredKeys();
        }
    }

    private void iterateThroughRegisteredKeys() {
        try {
            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                processSelectionKeys(keys);
                if (!isAlive()) {
                    break;
                }
            }
        } catch (IOException ex) {
            LOGGER.error(null, ex);
        } finally {
            cleanUpResources();
            GuiEntryPoint.switchToIpPrompt();
        }
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
            if (key.isConnectable() && !connect(key)) {
                break;
            }
            if (key.isWritable() && this.messageToSend != null) {
                write(key);
            }
            if (key.isReadable()) {
                if (this.boardType == null) {
                    readInitMessage();
                    GuiEntryPoint.switchToCurrentDevice();
                } else {
                    processAgentMessage();
                }
            }
        }
    }

    private void processAgentMessage() throws IOException {
        String agentMessage = read();
        if (agentMessage != null) {
            InterruptValueObject object;
            if ((object = MessageParser.getInterruptValueObjectFromMessage(agentMessage)) != null) {
                InterruptTableController.updateInterruptListener(object);
            } else {
                Platform.runLater(() -> GuiEntryPoint.provideFeedback(agentMessage));
            }
        } else {
            LOGGER.debug("null has been received from agent as a message");
            cleanUpResources();
        }
    }

    private String read() throws IOException {
        ByteBuffer readBuffer = ByteBuffer.allocate(1000);
        readBuffer.clear();
        int length;
        length = channel.read(readBuffer);
        if (length == -1) {
            LOGGER.error("Nothing was read from server");
            channel.close();
            return null;
        }
        readBuffer.flip();
        byte[] buff = new byte[1024];
        readBuffer.get(buff, 0, length);
        return (new String(buff).replaceAll("\0", ""));
    }

    private boolean readInitMessage() throws IOException {
        String agentMessage = read();
        try {
            this.setBoardType(BoardType.parse(agentMessage));
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private void write(SelectionKey key) throws IOException {
        if(messageToSend == null) {
            throw new IllegalStateException("cannot send null message to agent");
        }
        channel.write(ByteBuffer.wrap(messageToSend.getBytes()));
        key.interestOps(SelectionKey.OP_READ);
        setMessageToSend(null);
    }

    private boolean connect(SelectionKey key) {
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
}
