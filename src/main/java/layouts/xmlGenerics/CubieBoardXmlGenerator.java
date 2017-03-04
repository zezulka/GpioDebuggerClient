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
public class CubieBoardXmlGenerator extends AbstractDeviceXmlGenerator {
    
    private static final CubieBoardXmlGenerator INSTANCE = new CubieBoardXmlGenerator();
    
    public CubieBoardXmlGenerator() {
        super(0, 0, BoardType.CUBIEBOARD, "CubieBoard.fxml");
    }
    
    public static CubieBoardXmlGenerator getInstance() {
        return INSTANCE;
    }
    
}
