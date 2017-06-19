package layouts.xmlGenerics;

import protocol.BoardType;

public class DeviceXmlGeneratorFactory {
    public static DeviceXmlGenerator from(BoardType boardType) {
        switch(boardType) {
            case BEAGLEBONEBLACK : return new BeagleBoneBlackXmlGenerator();
            case CUBIEBOARD : return new CubieBoardXmlGenerator();
            case RASPBERRY_PI : return new RaspiXmlGenerator();
        }
        throw new IllegalArgumentException("Board type not supported yet.");
    } 
}
