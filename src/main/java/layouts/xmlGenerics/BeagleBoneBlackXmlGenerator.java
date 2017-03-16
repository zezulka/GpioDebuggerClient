/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.xmlGenerics;

import protocol.BoardType;

/**
 *
 * @author miloslav
 */
public class BeagleBoneBlackXmlGenerator extends AbstractDeviceXmlGenerator {
    
    private static final BeagleBoneBlackXmlGenerator INSTANCE = new BeagleBoneBlackXmlGenerator();
    
    public BeagleBoneBlackXmlGenerator() {
        super(0,0,BoardType.BEAGLEBONEBLACK, "BBB");
        throw new UnsupportedOperationException("not finished yet");
    }
    
    public static BeagleBoneBlackXmlGenerator getInstance() {
        return INSTANCE;
    }
    
}
