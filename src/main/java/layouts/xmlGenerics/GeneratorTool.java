/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.xmlGenerics;

import java.io.IOException;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import protocol.BoardType;

/**
 * Tool used for generating GUI (more specifically, this tool creates fxml files representing
 * user interface). DeviceXmlGenerator interface is used for this purpose.
 * @author Miloslav Zezulka, 2017
 */
public class GeneratorTool {
    public static void main(String[] args) {
        final Logger toolLogger = LoggerFactory.getLogger(GeneratorTool.class);
        try {
            DeviceXmlGeneratorFactory.from(BoardType.RASPBERRY_PI).createFxml();
            //Stream.of(BoardType.values()).forEach((t) -> DeviceXmlGeneratorFactory.from(t).createXml());
        } catch (IOException ex) {
            toolLogger.error(null, ex);
        }
    }
}
