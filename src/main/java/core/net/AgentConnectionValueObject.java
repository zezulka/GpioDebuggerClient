package core.net;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import protocol.BoardType;
import userdata.DeviceValueObject;

/**
 *
 * @author Miloslav Zezulka
 */
public class AgentConnectionValueObject {
    private String messageToSend;
    private final Selector selector;
    private BoardType boardType;
    private final DeviceValueObject device;
    private final SocketChannel channel;

    public AgentConnectionValueObject(String messageToSend, Selector selector, BoardType boardType, SocketChannel channel, DeviceValueObject device) {
        this.messageToSend = messageToSend;
        this.selector = selector;
        this.boardType = boardType;
        this.channel = channel;
        this.device = device;
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
    
    public DeviceValueObject getDevice() {
        return this.device;
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
