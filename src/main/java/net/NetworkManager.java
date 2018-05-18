package net;

import core.Main;

import java.io.IOException;

import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import gui.controllers.Utils;
import util.StringConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gui.userdata.DeviceValueObject;

public final class NetworkManager {

    private static final Map<InetAddress, ConnectionThread> ADDRESSES
            = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static final int BUFFER_SIZE = 1024;
    public static final int END_OF_STREAM = -1;
    private static final NetworkManager INSTANCE = new NetworkManager();

    private NetworkManager() {
    }

    public static NetworkManager getInstance() {
        return INSTANCE;
    }

    /**
     * Checks whether the client has already connected to the address. This
     * method should enable application to prevent user from connecting to the
     * same device (to the same IP address, that is) twice.
     *
     */
    private boolean alreadyConnectedToAddress(InetAddress ipAddress) {
        return ADDRESSES.get(ipAddress) != null;
    }

    void addNew(InetAddress address, ConnectionThread thread) {
        ADDRESSES.put(address, thread);
    }

    public static void removeMapping(InetAddress address) {
        ADDRESSES.remove(address);
    }

    /**
     * Triggers manager to connect to the device on the given address. New
     * Thread instance is created, which iterates in infinite loop and scans for
     * selection keys (more information in run method). It is supposed that the
     * ipAddress supplied is valid and agent is alive on the specified address.
     */
    public boolean connectToDevice(DeviceValueObject device) {
        if (alreadyConnectedToAddress(device.getAddress())) {
            Utils
                    .showErrorDialog(StringConstants.ERR_ALREADY_CONNECTED);
            return false;
        }
        Selector selector;
        SocketChannel channel;
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException ex) {
            LOGGER.error(null, ex);
            return false;
        }
        ConnectionThread thread = new ConnectionThread(device);
        new Thread(thread).start();
        return true;
    }

    public static void setMessageToSend(InetAddress address, String msg) {
        Objects.requireNonNull(address, "address is null");
        Objects.requireNonNull(msg, "message is null");
        ADDRESSES.get(address).registerMessage(msg);
    }

    public static void disconnect(InetAddress address) {
        ADDRESSES.get(address).disconnect();
    }

    public static void disconnectAll() {
        Set<ConnectionThread> threads = new HashSet<>(ADDRESSES.values());
        threads.forEach(ConnectionThread::disconnect);
        ADDRESSES.clear();
    }

    public static boolean isAnyConnectionOpened() {
        return !ADDRESSES.isEmpty();
    }
}
