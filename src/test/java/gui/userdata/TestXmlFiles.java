package gui.userdata;

import java.io.File;

public class TestXmlFiles {

    private static final String XML_EXT = ".xml";

    static final File DEVICES_EMPTY
            = getTestFileFromRelativePath("devices_empty");
    static final File I2C_EMPTY
            = getTestFileFromRelativePath("i2c_requests_empty");
    static final File SPI_EMPTY
            = getTestFileFromRelativePath("spi_requests_empty");
    static final File UNKNOWN
            = getTestFileFromRelativePath("unknown_collection");
    static final File ONE_DEVICE
            = getTestFileFromRelativePath("devices_one");
    static final File INVALID_XML
            = getTestFileFromRelativePath("invalid_xml");
    static final File MISSING
            = getTestFileFromRelativePath("file_which_does_not_exist");
    static final File EMPTY_FILE
            = getTestFileFromRelativePath("empty_file");

    private static File getTestFileFromRelativePath(String relativePath) {
        return new File("src" + File.separator
                + "test" + File.separator
                + "resources" + File.separator
                + relativePath + XML_EXT);
    }

}
