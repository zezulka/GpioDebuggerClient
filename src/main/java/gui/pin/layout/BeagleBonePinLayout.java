package gui.pin.layout;

public final class BeagleBonePinLayout extends AbstractPinLayout {

    private static final BeagleBonePinLayout INSTANCE
            = new BeagleBonePinLayout();

    private BeagleBonePinLayout() {
        super(null);
    }

    public static BeagleBonePinLayout getInstance() {
        return INSTANCE;
    }
}
