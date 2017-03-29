/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.controllers;

import protocol.BoardType;

/**
 *
 * @author Miloslav
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
