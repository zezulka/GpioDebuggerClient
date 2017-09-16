package gui.layouts;

import protocol.BoardType;

public final class PinLayoutFactory {

    private PinLayoutFactory() {
    }

    public static PinLayout getInstance(BoardType name) {
        switch (name) {
            case RASPBERRY_PI:
                return RaspberryPiPinLayout.getInstance();
            case BEAGLEBONEBLACK:
                return BeagleBonePinLayout.getInstance();
            case CUBIEBOARD:
                return CubieBoardPinLayout.getInstance();
            default:
                throw new IllegalArgumentException("illegal enum type");
        }

    }
}
