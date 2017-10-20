package gui.pin.layout;

import protocol.ClientPin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AbstractPinLayout implements PinLayout {

    private final List<ClientPin> pins;

    protected AbstractPinLayout(List<ClientPin> pins) {
        Objects.requireNonNull(pins, "pins");
        this.pins = new ArrayList<>(pins);
    }

    @Override
    public final List<ClientPin> getPins() {
        return Collections.unmodifiableList(this.pins);
    }

    @Override
    public final ClientPin getPinFromIndex(int i) {
        for (ClientPin pin : pins) {
            if (pin.getPort() == i) {
                return pin;
            }
        }
        throw new IllegalArgumentException("index not found: " + i);
    }
}
