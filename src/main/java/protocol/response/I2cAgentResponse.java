package protocol.response;

import core.gui.App;

public final class I2cAgentResponse implements AgentResponse {

    private final String responseBody;

    public I2cAgentResponse(String responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public void react() {
        App.writeI2cResponseIntoTextArea(responseBody);
    }
}
