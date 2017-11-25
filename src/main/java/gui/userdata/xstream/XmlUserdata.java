package gui.userdata.xstream;

import java.io.File;
import props.AppPreferencesExtractor;
import props.SystemPropertiesExtractor;

public final class XmlUserdata {

    private XmlUserdata() {
    }

    private static final String XML_EXT = ".xml";
    private static final String PATH_TO_USER_DATA
            = SystemPropertiesExtractor.USER_HOME
            + File.separatorChar
            + AppPreferencesExtractor.userDataDir();

    public static final File DEVICES_FILE
            = getFileFromRelativePath(AppPreferencesExtractor.devices());

    public static final File I2C_FILE
            = getFileFromRelativePath(AppPreferencesExtractor.i2c());

    public static final File SPI_FILE
            = getFileFromRelativePath(AppPreferencesExtractor.spi());

    public static final File USER_DATA_DIR = new File(PATH_TO_USER_DATA);

    private static File getFileFromRelativePath(String relativePath) {
        return new File(PATH_TO_USER_DATA + File.separatorChar
                + relativePath + XML_EXT);
    }

}
