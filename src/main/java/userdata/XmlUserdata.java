package userdata;

import java.io.File;

public final class XmlUserdata {

    private XmlUserdata() {
    }

    private static final char SEP_CHR = File.separatorChar;
    private static final String XML_EXT = ".xml";

    public static final File DEVICES_FILE
            = getFileFromRelativePath("devices");

    public static final File I2C_FILE
            = getFileFromRelativePath("i2c_requests");

    public static final File SPI_FILE
            = getFileFromRelativePath("spi_requests");

    private static File getFileFromRelativePath(String relativePath) {
        return new File("src" + SEP_CHR
                + "main" + SEP_CHR
                + "resources" + SEP_CHR + relativePath + XML_EXT);
    }
}
