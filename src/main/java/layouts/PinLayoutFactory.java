package layouts;

import gpiodebuggerui.Main;
import protocol.BoardType;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class PinLayoutFactory {
    
    public static PinLayout getInstance(BoardType name) {
        switch(name) {
            case RASPBERRY_PI : return new RaspberryPiLayout(Main.getBoard().getPins());
            case BEAGLEBONEBLACK : return new BeagleBoneLayout(Main.getBoard().getPins());
            case CUBIEBOARD : return new CubieBoardLayout(Main.getBoard().getPins());
            default : throw new IllegalArgumentException("illegal enum type (BoardType) provided");
        }
        
    }
    
}
