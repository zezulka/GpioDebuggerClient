package protocol.response;

import core.gui.App;

public final class SpiAgentResponse implements AgentResponse {

    private final String responseBody;

    public SpiAgentResponse(String responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public void react() {
        App.writeSpiResponseIntoTextArea(responseBody);
    }

}
