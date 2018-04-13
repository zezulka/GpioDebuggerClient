package net;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public final class AgentConnection {

    private String messageToSend;
    private final Selector selector;
    private final SocketChannel channel;

    public AgentConnection(String messageToSend, Selector selector,
                           SocketChannel channel) {
        this.messageToSend = messageToSend;
        this.selector = selector;
        this.channel = channel;
    }

    public void setMessageToSend(String messageToSend) {
        this.messageToSend = messageToSend;
    }

    public String getMessageToSend() {
        return messageToSend;
    }

    public Selector getSelector() {
        return selector;
    }

    public SocketChannel getChannel() {
        return channel;
    }
}
