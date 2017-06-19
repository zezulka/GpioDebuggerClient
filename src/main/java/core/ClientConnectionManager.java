package core;

import util.MessageParser;
import layouts.controllers.GuiEntryPoint;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalTime;
import java.util.Iterator;
import javafx.application.Platform;
import layouts.controllers.InterruptTableController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protocol.BoardType;
import protocol.ClientPin;
import protocol.InterruptListenerStatus;
import protocol.InterruptType;
import protocol.InterruptValueObject;
import protocol.ListenerState;
import protocol.ProtocolMessages;
import protocol.RaspiClientPin;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class ClientConnectionManager implements Runnable {

    private String messageToSend = null;
    private Selector selector;
    private static String ipAddress;
    private BoardType boardType;
    private SocketChannel channel;

    private static final Logger MAIN_LOGGER = LoggerFactory.getLogger(Main.class);
    public static final int DEFAULT_SOCK_PORT = 8088;
    private static final int TIMEOUT = 5 * 1000;

    private static final ClientConnectionManager CM = new ClientConnectionManager();

    public static ClientConnectionManager getInstance() {
        return CM;
    }

    private ClientConnectionManager() {
    }

    public void connectToDevice(String ipAddress) {
        setIpAddress(ipAddress);
        new Thread(this).start();
    }

    private void resetResources() {
        try {
            if (channel != null) {
                channel.close();
            }
            if (selector != null) {
                selector.close();
            }
        } catch (IOException ex) {
            MAIN_LOGGER.error(null, ex);
        }
        selector = null;
        ipAddress = null;
        channel = null;
        boardType = null;
    }

    public boolean isAlive() {
        return channel != null && channel.isConnected();
    }

    public boolean initManager() {
        if (selector != null || channel != null) {
            resetResources();
            return false;
        }
        if (ipAddress == null) {
            throw new IllegalStateException("Cannot initialize server,"
                    + " ip address has not been set yet!");
        }
        setIpAddress(ipAddress);
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(true);
            String[] octets = ipAddress.split("\\.", 4);
            byte[] ipAddr = new byte[4];
            for (int i = 0; i < 4; i++) {
                ipAddr[i] = (byte) Integer.parseInt(octets[i]);
            }
            InetAddress inetAddress = InetAddress.getByAddress(ipAddr);
            if (!inetAddress.isReachable(TIMEOUT)) {
                Platform.runLater(() -> {
                    GuiEntryPoint.provideFeedback(String.format("Host %s could not be reached.", ipAddress));
                });
                resetResources();
                return false;
            }
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(new InetSocketAddress(ipAddress, DEFAULT_SOCK_PORT));
        } catch (IOException ex) {
            MAIN_LOGGER.error("Init I/O error", ex);
            resetResources();
            return false;
        }
        return true;
    }

    public void setIpAddress(String ipAddress) {
        if (ipAddress == null) {
            throw new IllegalArgumentException("ip address cannot be null!");
        }
        ClientConnectionManager.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setMessageToSend(String message) {
        this.messageToSend = message;
        if (message != null) {
            try {
                channel.register(selector, SelectionKey.OP_WRITE);
                selector.wakeup();
            } catch (ClosedChannelException ex) {
                channel = null;
                MAIN_LOGGER.error("There has been an attempt to "
                        + "register write operation on channel which has been closed.");

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
            try {
                if (!initManager()) {
                    GuiEntryPoint.getInstance().switchToIpPrompt();
                    resetResources();
                    Platform.runLater(() -> {
                        GuiEntryPoint.provideFeedback("Cannot connect to manager. "
                                + "\nPlease make sure that agent instance is running on the specified address.");
                    });
                    return;
                }
                iterateThoughRegisteredKeys();
            } catch (IOException ex) {
                MAIN_LOGGER.error(null, ex);
            } finally {
                close();
            }
        }
    }

    private void iterateThoughRegisteredKeys() throws IOException {
        while (!Thread.interrupted()) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            processSelectionKeys(keys);
            if (!isAlive()) {
                break;
            }
        }
    }

    private void processSelectionKeys(Iterator<SelectionKey> keys) throws IOException {
        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();
            if (!key.isValid()) {
                MAIN_LOGGER.error(String.format("A nonvalid key has been registered: %s", key.toString()));
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
                    readInitMessage(key);
                    GuiEntryPoint.getInstance().switchToCurrentDevice();
                } else {
                    processAgentMessage(key);
                }
            }
        }
    }

    private void processAgentMessage(SelectionKey key) throws IOException {
        String agentMessage = read(key);
        if (agentMessage != null) {
            if (MessageParser.isInterruptMessage(getMessagePrefix(agentMessage))) {
                InterruptValueObject ivo = getInterruptValueObjectFromMessage(agentMessage);
                InterruptTableController.updateInterruptListener(ivo);
            } else {
                Platform.runLater(() -> GuiEntryPoint.provideFeedback(agentMessage));
            }
        } else {
            MAIN_LOGGER.debug("null has been received from agent as a message");
            resetResources();
        }
    }

    private String getMessagePrefix(String message) {
        int firstSeparatorOccurence = message.indexOf(":");
        return message.substring(0, firstSeparatorOccurence < 0 ? message.length() : firstSeparatorOccurence);
    }

    private InterruptValueObject getInterruptValueObjectFromMessage(String agentMessage) {
        MAIN_LOGGER.debug(String.format("Message accepted from agent is about to get processed: %s", agentMessage));
        String[] splitMessage = agentMessage.split(":");
        //bound to Raspi only!!!!
        ClientPin pin;
        InterruptType type;
        try {
            pin = RaspiClientPin.getPin(splitMessage[1]);
            type = InterruptType.getType(splitMessage[2]);
        } catch(IllegalArgumentException ex) {
            return null;
        }
        InterruptValueObject result = new InterruptValueObject(pin, type);
        InterruptListenerStatus status = InterruptListenerStatus.valueOf(splitMessage[0]);
        switch (status) {
            case INTR_GENERATED: {
                result.setLatestInterruptTime(LocalTime.ofNanoOfDay(Long.valueOf(splitMessage[3].replace("\n", ""))));
                break;
            }
            case INTR_STARTED: {
                result.setState(ListenerState.RUNNING);
                break;
            }
            case INTR_STOPPED: {
                result.setState(ListenerState.NOT_RUNNING);
                break;
            }
        }

        return result;
    }

    private void close() {
        if (selector == null) {
            return;
        }
        try {
            selector.close();
        } catch (IOException ex) {
            MAIN_LOGGER.error(null, ex);
        }
    }

    private String read(SelectionKey key) throws IOException {
        ByteBuffer readBuffer = ByteBuffer.allocate(1000);
        readBuffer.clear();
        int length;
        length = channel.read(readBuffer);
        if (length == -1) {
            MAIN_LOGGER.error("Nothing was read from server");
            channel.close();
            key.cancel();
            return null;
        }
        readBuffer.flip();
        byte[] buff = new byte[1024];
        readBuffer.get(buff, 0, length);
        return (new String(buff).replaceAll("\0", ""));
    }

    private void readInitMessage(SelectionKey key) throws IOException {
        String agentMessage = read(key);
        this.setBoardType(BoardType.parse(agentMessage));
    }

    private void write(SelectionKey key) throws IOException {
        channel.write(ByteBuffer.wrap(this.messageToSend.getBytes()));
        key.interestOps(SelectionKey.OP_READ);
        this.setMessageToSend(null);
    }

    private boolean connect(SelectionKey key) {
        try {
            if (channel.isConnectionPending() && channel.finishConnect()) {
                MAIN_LOGGER.info("done connecting to server");
            }
            MAIN_LOGGER.info(ProtocolMessages.C_CONNECTION_OK.toString());
            channel.configureBlocking(false);
            key.interestOps(SelectionKey.OP_READ);

        } catch (IOException ex) {
            MAIN_LOGGER.error("Could not connect to server", ex);
            return false;
        }
        return true;
    }
}
