package protocol.response;

import java.net.InetAddress;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import gui.controllers.Utils;
import gui.controllers.MasterWindow;
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
        Tab t = MasterWindow
                .getTabManager().findTabByAddress(address);
        Button btn = (Button) t.getContent()
                .lookup("#" + pin.getPinId());
        btn.setStyle("");
        String color = signal.getBooleanValue() ? "00AA00" : "FF5555";
        btn.setStyle("-fx-background-color: #" + color);

        Utils.playButtonAnimation(btn);
    }
}
