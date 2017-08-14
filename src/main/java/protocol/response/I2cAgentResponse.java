package protocol.response;

import core.gui.App;
import java.net.InetAddress;

public final class I2cAgentResponse implements AgentResponse {

    private final String responseBody;
    private final InetAddress address;

    public I2cAgentResponse(String responseBody, InetAddress address) {
        this.responseBody = responseBody;
        this.address = address;
    }

    @Override
    public void react() {
        App.displayI2cResponse(responseBody, address);
    }
}
