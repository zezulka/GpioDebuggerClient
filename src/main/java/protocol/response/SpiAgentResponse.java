package protocol.response;

import core.gui.App;
import java.net.InetAddress;

public final class SpiAgentResponse implements AgentResponse {

    private final String responseBody;
    private final InetAddress address;

    public SpiAgentResponse(String responseBody, InetAddress address) {
        this.responseBody = responseBody;
        this.address = address;
    }

    @Override
    public void react() {
        App.displaySpiResponse(responseBody, address);
    }

}
