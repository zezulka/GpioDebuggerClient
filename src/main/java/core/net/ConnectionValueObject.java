package core.net;

import gui.AgentUserPrivileges;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import userdata.DeviceValueObject;

/**
 *
 * @author Miloslav Zezulka
 */
public final class ConnectionValueObject {
    private String messageToSend;
    private final Selector selector;
    private final DeviceValueObject device;
    private final SocketChannel channel;
    private AgentUserPrivileges privileges;

    public ConnectionValueObject(String messageToSend, Selector selector,
            SocketChannel channel, DeviceValueObject device) {
        this.messageToSend = messageToSend;
        this.selector = selector;
        this.channel = channel;
        this.device = device;
    }

    public void setPrivileges(AgentUserPrivileges privileges) {
        this.privileges = privileges;
    }

    public AgentUserPrivileges getPrivileges() {
        return privileges;
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
