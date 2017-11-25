package gui.pin.layout;

import java.util.Collections;

public final class CubieBoardPinLayout extends AbstractPinLayout {

    private static final CubieBoardPinLayout INSTANCE
            = new CubieBoardPinLayout();

    private CubieBoardPinLayout() {
        super(Collections.EMPTY_LIST);
    }

    public static CubieBoardPinLayout getInstance() {
        return INSTANCE;
    }
}
