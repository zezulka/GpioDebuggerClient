package gui.layouts.xmlGenerics;

import protocol.BoardType;

public class RaspiXmlGenerator extends AbstractDeviceXmlGenerator {

    private static final int PIN_ROWS = 20;

    public RaspiXmlGenerator() {
        super(PIN_ROWS, 2, BoardType.RASPBERRY_PI, "Raspi");
    }
}
