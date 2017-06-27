package core.net;

import core.Main;

import java.io.IOException;

import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import java.util.HashMap;
import java.util.Map;
import layouts.controllers.ControllerUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class ClientNetworkManager {

    private static final Map<InetAddress, ClientConnectionThread> ADDRESSES = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static final int DEFAULT_SOCK_PORT = 8088;
    public static final int TIMEOUT = 5 * 1000;
    private static final ClientNetworkManager INSTANCE = new ClientNetworkManager();

    private ClientNetworkManager() {
    }

    public static ClientNetworkManager getInstance() {
        return INSTANCE;
    }

    /**
     * Triggers manager to connect to the device on the given address. New
     * Thread instance is created, which iterates in infinite loop and scans for
     * selection keys (more information in run method). It is supposed that the
     * ipAddress supplied is valid and agent is alive on the specified address.
     *
     * @param ipAddress
     */
    public void connectToDevice(InetAddress ipAddress) {
        initConnection(ipAddress);
    }

    /**
     * Checks whether the client has already connected to the address. This
     * method should enable application to prevent user from connecting to the
     * same device (to the same IP address, that is) twice.
     *
     * @param ipAddress
     * @return
     */
    private boolean alreadyConnectedToAddress(InetAddress ipAddress) {
        return ADDRESSES.get(ipAddress) != null;
    }

    private void initConnection(InetAddress ipAddress) {
        if (alreadyConnectedToAddress(ipAddress)) {
            ControllerUtils.showErrorDialogMessage("Connection has already been established for this IP address.");
            return;
        }
        Selector selector;
        SocketChannel channel;
        if (ipAddress == null) {
            return;
        }
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException ex) {
            LOGGER.error(null, ex);
            return;
        }
        AgentConnectionValueObject connection = new AgentConnectionValueObject(null, selector, null, channel, ipAddress);
        ClientConnectionThread thread = new ClientConnectionThread(connection);
        ADDRESSES.put(ipAddress, thread);
        new Thread(thread).start();
    }
    
    public static void setMessageToSend(InetAddress address, String messageToSend) {
        ADDRESSES.get(address).setMessageToSend(messageToSend);
    }
    
    public static void disconnect(InetAddress address) {
        ADDRESSES.get(address).disconnect();
        ADDRESSES.remove(address);
    }
    
    public static void disconnectAll() {
        ADDRESSES.forEach((addr, connection) -> {
            connection.disconnect();
        });
        ADDRESSES.clear();
    }
    
    public static boolean isAnyConnectionOpened() {
        return !ADDRESSES.isEmpty();
    }
} 
