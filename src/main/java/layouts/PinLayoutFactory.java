package layouts;

import protocol.BoardType;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class PinLayoutFactory {
    
    public static PinLayout getInstance(BoardType name) {
        switch(name) {
            case RASPBERRY_PI : return RaspberryPiPinLayout.getInstance();
            case BEAGLEBONEBLACK : return BeagleBonePinLayout.getInstance();
            case CUBIEBOARD : return CubieBoardPinLayout.getInstance();
            default : throw new IllegalArgumentException("illegal enum type (BoardType) provided");
        }
        
    }
    
}
