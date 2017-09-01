package gui.layouts;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public final class CubieBoardPinLayout extends AbstractPinLayout {

    private static final CubieBoardPinLayout INSTANCE
            = new CubieBoardPinLayout();

    private CubieBoardPinLayout() {
        super(null);
    }

    public static CubieBoardPinLayout getInstance() {
        return INSTANCE;
    }
}
