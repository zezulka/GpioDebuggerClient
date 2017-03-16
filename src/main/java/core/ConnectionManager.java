package core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.BoardType;
import protocol.ProtocolMessages;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class ConnectionManager implements Runnable {

    private String message;
    private Selector selector;
    private String ipAddress;
    private BoardType boardType;
    
    private static final Logger MAIN_LOGGER = LoggerFactory.getLogger(Main.class);
    public static final int DEFAULT_SOCK_PORT = 1024;
    private static final int TIMEOUT = 1000;
    private static final ConnectionManager INSTANCE = new ConnectionManager();

    public static ConnectionManager getInstance() {
        return INSTANCE;
    }

    private ConnectionManager() {
        try {
            SocketChannel channel;
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);

            channel.register(selector, SelectionKey.OP_CONNECT);
            synchronized(this) {
                while(this.ipAddress == null) {
                    wait();
                }
            }
            channel.connect(new InetSocketAddress(ipAddress, DEFAULT_SOCK_PORT));
        } catch (IOException ex) {
            MAIN_LOGGER.error("Init I/O error", ex);
        } catch (InterruptedException ex) {
            MAIN_LOGGER.error("interrupted thread", ex);
        }
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getIpAddress() {
        return this.ipAddress;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    private void setBoardType(BoardType type) {
        this.boardType = type;
    }
    
    public BoardType getBoardType() {
        return this.boardType;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select(TIMEOUT);

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isConnectable()) {
                        connect(key);
                    }
                    if (message != null && key.isWritable()) {
                        write(key);
                    }
                    if (key.isReadable()) {
                        read(key);
                    }
                }
            }
        } catch (IOException ex) {
            MAIN_LOGGER.error("I/O error", ex);
        } finally {
            close();
        }
    }

    private void close() {
        try {
            selector.close();
        } catch (IOException ex) {
            MAIN_LOGGER.error("I/O error", ex);
        }
    }

    private String read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(1000);
        readBuffer.clear();
        int length;
        length = channel.read(readBuffer);
        if (length == -1) {
            System.out.println("Nothing was read from server");
            channel.close();
            key.cancel();
            return null;
        }
        readBuffer.flip();
        byte[] buff = new byte[1024];
        readBuffer.get(buff, 0, length);
        return new String(buff);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(ByteBuffer.wrap(message.getBytes()));
        key.interestOps(SelectionKey.OP_READ);
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        MAIN_LOGGER.info(ProtocolMessages.C_CONNECTION_OK.toString());
        channel.configureBlocking(false);
        //receive init message
        key.interestOps(SelectionKey.OP_READ);
        this.setBoardType(BoardType.parse(read(key)));
        channel.register(selector, SelectionKey.OP_WRITE);
    }
}
