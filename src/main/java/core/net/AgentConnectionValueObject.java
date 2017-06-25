package core.net;

import java.net.InetAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import protocol.BoardType;

/**
 *
 * @author Miloslav Zezulka
 */
public class AgentConnectionValueObject {
    private String messageToSend;
    private final Selector selector;
    private BoardType boardType;
    private final InetAddress address;
    private final SocketChannel channel;

    public AgentConnectionValueObject(String messageToSend, Selector selector, BoardType boardType, SocketChannel channel, InetAddress address) {
        this.messageToSend = messageToSend;
        this.selector = selector;
        this.boardType = boardType;
        this.channel = channel;
        this.address = address;
    }

    public void setMessageToSend(String messageToSend) {
        this.messageToSend = messageToSend;
    }
    
    public void setBoardType(BoardType boardType) {
        if(this.boardType != null) {
            throw new IllegalStateException("board type already set!");
        }
        this.boardType = boardType;
    }
    
    public String getMessageToSend() {
        return messageToSend;
    }
    
    public InetAddress getInetAddress() {
        return this.address;
    }

    public Selector getSelector() {
        return selector;
    }

    public BoardType getBoardType() {
        return boardType;
    }

    public SocketChannel getChannel() {
        return channel;
    }
}
