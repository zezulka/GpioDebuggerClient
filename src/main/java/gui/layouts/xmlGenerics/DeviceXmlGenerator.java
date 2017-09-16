package gui.layouts.xmlGenerics;

import java.io.IOException;

public interface DeviceXmlGenerator {
    /**
     * Takes care of generating fxml file for a specific device.
     * @throws IOException
     */
    void createFxml() throws IOException;
}
