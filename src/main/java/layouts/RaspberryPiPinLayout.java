/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts;

import java.util.Arrays;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class RaspberryPiPinLayout extends AbstractPinLayout {
    
    private static final RaspberryPiPinLayout INSTANCE = new RaspberryPiPinLayout();
    
    private RaspberryPiPinLayout() {
        super(Arrays.asList(RaspiClientPin.values()));
    }
    
    public static RaspberryPiPinLayout getInstance() {
        return INSTANCE;
    }
}
