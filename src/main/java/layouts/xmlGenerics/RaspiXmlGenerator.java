/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.xmlGenerics;

import protocol.BoardType;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class RaspiXmlGenerator extends AbstractDeviceXmlGenerator {    
    public RaspiXmlGenerator() {
        super(20, 2, BoardType.RASPBERRY_PI, "Raspi");
    }
}
