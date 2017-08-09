package protocol.response;

import core.gui.App;
import protocol.ClientPin;
import protocol.Signal;

public final class GpioAgentResponse implements AgentResponse {

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
