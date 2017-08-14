package protocol.response;

import core.gui.App;
import java.net.InetAddress;
import protocol.ClientPin;
import protocol.Signal;

public final class GpioAgentResponse implements AgentResponse {

    private final Signal signal;
    private final ClientPin pin;
    private final InetAddress address;

    public GpioAgentResponse(Signal signal, ClientPin pin,
            InetAddress address) {
        this.signal = signal;
        this.pin = pin;
        this.address = address;
    }

    @Override
    public void react() {
        App.setPinButtonColourFromSignal(pin, signal, address);
    }
}
