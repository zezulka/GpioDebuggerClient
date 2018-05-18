package net;

import gui.controllers.MasterWindow;
import gui.userdata.DeviceValueObject;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import properties.AppPreferencesExtractor;
import protocol.InterruptManager;
import protocol.MessageParser;

import java.util.concurrent.atomic.AtomicBoolean;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;


public final class ConnectionThread implements Runnable {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(ConnectionThread.class);
    private static final int TIMEOUT = 5000;
    private static final NetworkManager MANAGER = NetworkManager.getInstance();
    private final ConnectionLock cn = new ConnectionLock();
    private AtomicBoolean connected = new AtomicBoolean(false);
    private String currentMessage = null;

    private final DeviceValueObject device;

    public ConnectionThread(DeviceValueObject device) {
        this.device = device;
    }

    /**
     * Main loop of the application logic. An attempt is made to initialize this
     * manager (singleton); if manager succeeded in connecting to the supplied
     * IP address, it then tries to receive the "handshake" message containing
     * name of the device. If the handshake message is valid (=ok parse), it
     * then invokes appropriate method to switch to the given scene which
     * enables user to control the device the client connected to.
     * <p>
     * While the agent is alive, this thread iterates through selection keys and
     * deals with them (this includes reading messages from input stream or
     * writing client messages to output stream).
     */
    @Override
    public void run() {
        try (Context zmqContext = ZMQ.context(1);
             Socket zmqSocket = zmqContext.socket(ZMQ.REQ)) {
            connect(zmqSocket);
            while (connected.get()) {
                synchronized (cn.getLock()) {
                    while (!cn.isReady()) {
                        cn.getLock().wait();
                    }
                    cn.reset();
                }
                zmqSocket.send(currentMessage.getBytes(), 0);
                byte[] reply = zmqSocket.recv(0);
                if (reply == null) {
                    LOGGER.info("Connection lost.");
                    return;
                }
                String strReply = new String(reply);
                parseAgentMessage(strReply);
                LOGGER.info("Received " + strReply);
            }
            LOGGER.info("End of the connection.");
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void parseAgentMessage(String agentMessage) {
        if (agentMessage != null) {
            MessageParser.parseAgentMessage(device, agentMessage);
        }
    }

    /**
     * Causes manager to close all resources binded to the existing device.
     * If device has not been established, invocation of this method is a
     * no-op (regarding network resources).
     */
    public void disconnect() {
        connected.set(false);
        LOGGER.debug("disconnecting from agent...");
        if (device.isDirty() && MasterWindow.getTabManager().findTabByAddress(
                device.getAddress()) != null) {
            Platform.runLater(() -> MasterWindow.getTabManager()
                    .removeTab(device.getAddress()));
        }
        LOGGER.info(String.format("Disconnected from address %s, device %s",
                device.getAddress(),
                device.getBoardType()));
        device.disconnectedProperty().set(true);
        InterruptManager.clearAllListeners(device.getAddress());
        NetworkManager.removeMapping(device.getAddress());
    }

    private void connect(Socket zmqSocket) {
        String connectionString = "tcp://"
                + device.getAddress().getHostAddress() + ":"
                + AppPreferencesExtractor.defaultSocketPort();
        if (zmqSocket.connect(connectionString)) {
            zmqSocket.setReceiveTimeOut(TIMEOUT);
            connected.set(true);
            LOGGER.info("done connecting to server, sending handshake"
                    + " message...");
            zmqSocket.send("INIT".getBytes(), 0);
            MessageParser.parseAgentMessage(device,
                    new String(zmqSocket.recv(0)));
            MANAGER.addNew(device.getAddress(), this);
            device.disconnectedProperty().set(false);
        }
    }

    public void registerMessage(String message) {
        currentMessage = message;
        synchronized (cn.getLock()) {
            cn.ready();
            cn.getLock().notifyAll();
        }
    }


    private static final class ConnectionLock {
        private boolean ready = false;
        private Object lock = new Object();

        private ConnectionLock() {
        }

        private Object getLock() {
            return lock;
        }

        public boolean isReady() {
            return ready;
        }

        public void reset() {
            ready = false;
        }

        public void ready() {
            ready = true;
        }
    }
}
