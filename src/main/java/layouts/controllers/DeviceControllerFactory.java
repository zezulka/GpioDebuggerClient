package layouts.controllers;

import protocol.BoardType;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class DeviceControllerFactory {
    public static DeviceController getController(BoardType board) {
        switch(board) {
            case RASPBERRY_PI: return new RaspiController();
            case BEAGLEBONEBLACK : 
            case CUBIEBOARD: throw new UnsupportedOperationException("not supported yet");
            default: throw new IllegalArgumentException("enum type not supported");
        }
    }
}
