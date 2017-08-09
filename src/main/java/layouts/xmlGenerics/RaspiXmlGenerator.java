package layouts.xmlGenerics;

import protocol.BoardType;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class RaspiXmlGenerator extends AbstractDeviceXmlGenerator {

    private static final int PIN_ROWS = 20;

    public RaspiXmlGenerator() {
        super(PIN_ROWS, 2, BoardType.RASPBERRY_PI, "Raspi");
    }
}
