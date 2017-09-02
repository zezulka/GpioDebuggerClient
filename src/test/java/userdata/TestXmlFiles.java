package userdata;

import java.io.File;

public class TestXmlFiles {

    private static final char SEP_CHR = File.separatorChar;
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
        return new File("src" + SEP_CHR
                + "main" + SEP_CHR
                + "resources" + SEP_CHR
                + "testUserdata" + SEP_CHR
                + relativePath + XML_EXT);
    }

}
