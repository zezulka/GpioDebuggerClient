package gui.layouts.xmlGenerics;

import java.io.IOException;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import protocol.BoardType;

/**
 * Tool used for generating GUI (more specifically, this tool creates fxml
 * files representing user interface). DeviceXmlGenerator interface is used
 * for this purpose.
 */
public final class GeneratorTool {

    private GeneratorTool() {
    }

    public static void main(String[] args) {
        final Logger toolLogger = LoggerFactory.getLogger(GeneratorTool.class);
        try {
            DeviceXmlGeneratorFactory.from(BoardType.RASPBERRY_PI).createFxml();
        } catch (IOException ex) {
            toolLogger.error(null, ex);
        }
    }
}
