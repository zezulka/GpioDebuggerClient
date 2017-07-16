package protocol.response;

import protocol.response.AgentResponse;
import core.gui.App;
import protocol.ClientPin;
import protocol.Signal;

public class GpioAgentResponse implements AgentResponse {

    private final Signal signal;
    private final ClientPin pin;
    
    public GpioAgentResponse(Signal signal, ClientPin pin) {
        this.signal = signal;
        this.pin = pin;
    }
    
    @Override
    public void react() {
        App.setPinButtonColourFromSignal(pin, signal);
    }
         
}
