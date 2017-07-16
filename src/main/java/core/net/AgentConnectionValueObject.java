package core.net;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import userdata.DeviceValueObject;

/**
 *
 * @author Miloslav Zezulka
 */
public class AgentConnectionValueObject {
    private String messageToSend;
    private final Selector selector;
    private final DeviceValueObject device;
    private final SocketChannel channel;

    public AgentConnectionValueObject(String messageToSend, Selector selector, SocketChannel channel, DeviceValueObject device) {
        this.messageToSend = messageToSend;
        this.selector = selector;
        this.channel = channel;
        this.device = device;
    }

    public void setMessageToSend(String messageToSend) {
        this.messageToSend = messageToSend;
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

    public SocketChannel getChannel() {
        return channel;
    }
}
