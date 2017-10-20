package gui.pin.layout;

import protocol.RaspiClientPin;
import java.util.Arrays;

public final class RaspberryPiPinLayout extends AbstractPinLayout {

    private static final RaspberryPiPinLayout INSTANCE
            = new RaspberryPiPinLayout();

    private RaspberryPiPinLayout() {
        super(Arrays.asList(RaspiClientPin.values()));
    }

    public static RaspberryPiPinLayout getInstance() {
        return INSTANCE;
    }
}
