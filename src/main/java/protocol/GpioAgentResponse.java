package protocol;

import core.gui.App;

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
