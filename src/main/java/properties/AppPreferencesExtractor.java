package properties;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AppPreferencesExtractor {

    private static final Properties DEFAULT_PROPERTIES = new Properties();
    private static final Properties USER_PROPERTIES = new Properties();
    private static final Logger LOGGER
            = LoggerFactory.getLogger(AppPreferencesExtractor.class);
    private static boolean userPropertiesAvailable = false;

    //cached properties
    private static Dimension cachedScreenResolution = null;

    static {
        try {
            DEFAULT_PROPERTIES.load(AppPreferencesExtractor.class
                    .getClassLoader()
                    .getResourceAsStream("default.properties"));
        } catch (FileNotFoundException ex) {
            LOGGER.error("Default properties file not found.", ex);
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            LOGGER.error("Error reading from the input stream.", ex);
            throw new RuntimeException(ex);
        }
        File userPropertiesFile = new File(pathToUserDataDir()
                + File.separator
                + DEFAULT_PROPERTIES.getProperty("userdata.properties.name")
        );
        if (userPropertiesFile.exists()) {
            try {
                USER_PROPERTIES.load(new FileInputStream(userPropertiesFile));
                userPropertiesAvailable = true;
            } catch (FileNotFoundException ex) {
                // Should not happen, we already checked for this
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                LOGGER.error("Error reading from the input stream.", ex);
                throw new RuntimeException(ex);
            }
        }
    }

    private AppPreferencesExtractor() {
    }

    public static String i2c() {
        return getProperty("userdata.i2c");
    }

    public static String spi() {
        return getProperty("userdata.spi");
    }

    public static String devices() {
        return getProperty("userdata.devices");
    }

    public static String userDataDir() {
        return getProperty("userdata.dir.name");
    }

    public static String pathToUserDataDir() {
        return SystemPropertiesExtractor.USER_HOME
                + File.separator
                + userDataDir();
    }

    /**
     *
     * @return Null if the property found is malformed and not a number, or
     * boxed int representing value of the property
     */
    private static Integer parseNumber(String propertyName) {
        String w = getProperty(propertyName);
        if (w == null) {
            return null;
        }
        try {
            return Integer.valueOf(w);
        } catch (NumberFormatException e) {
            LOGGER.error(propertyName
                    + " property is not numeric in the properties file");
            return null;
        }
    }

    private static Integer screenWidth() {
        return parseNumber("graphics.window.width");
    }

    private static Integer screenHeight() {
        return parseNumber("graphics.window.height");
    }

    public static Dimension screenResolution() {
        if (cachedScreenResolution == null) {
            Integer w = screenWidth();
            Integer h = screenHeight();
            if (w == null || h == null) {
                throw new RuntimeException("Properties file is malformed.");
            }
            cachedScreenResolution = new Dimension(w, h);
        }
        return cachedScreenResolution;
    }

    public static Integer defaultSocketPort() {
        return parseNumber("net.socketport");
    }

    public static Integer timeout() {
        return parseNumber("net.timeout");
    }

    /**
     * Two property files can exist in the target file system: default (this
     * always exists, otherwise Exception is thrown in static initialiser of
     * this utility class) and one (optional) which contains user specified
     * properties. Should no user property file exist or {@code propertyName} is
     * not present in this file, default value from the default property file is
     * used instead.
     */
    private static String getProperty(String propertyName) {
        String result = null;
        if (userPropertiesAvailable) {
            result = USER_PROPERTIES.getProperty(propertyName);
        }
        if (result == null) {
            result = DEFAULT_PROPERTIES.getProperty(propertyName);
        }
        return result;
    }

}
