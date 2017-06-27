package protocol;

import core.gui.App;

public class SpiAgentResponse implements AgentResponse {

    private final String responseBody;

    public SpiAgentResponse(String responseBody) {
        this.responseBody = responseBody;
    }
    
    @Override
    public void react() {
        App.writeSpiResponseIntoTextArea(responseBody);
    }
    
}
