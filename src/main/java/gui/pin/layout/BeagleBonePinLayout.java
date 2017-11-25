package gui.pin.layout;

import java.util.Collections;

public final class BeagleBonePinLayout extends AbstractPinLayout {

    private static final BeagleBonePinLayout INSTANCE
            = new BeagleBonePinLayout();

    private BeagleBonePinLayout() {
        super(Collections.EMPTY_LIST);
    }

    public static BeagleBonePinLayout getInstance() {
        return INSTANCE;
    }
}
