package gui.layouts.xmlGenerics;

import protocol.BoardType;

public final class DeviceXmlGeneratorFactory {

    private DeviceXmlGeneratorFactory() {
    }

    public static DeviceXmlGenerator from(BoardType boardType) {
        switch (boardType) {
            case BEAGLEBONEBLACK : return new BeagleBoneBlackXmlGenerator();
            case CUBIEBOARD : return new CubieBoardXmlGenerator();
            case RASPBERRY_PI : return new RaspiXmlGenerator();
            default:
                throw new IllegalArgumentException("Not supported yet.");
        }
    }
}
