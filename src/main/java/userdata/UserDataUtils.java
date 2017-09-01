package userdata;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import gui.layouts.controllers.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserDataUtils {

    public static final DateTimeFormatter DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern("dd-MM, HH:mm");

    private static final char SEP_CHR = File.separatorChar;
    private static final XStream X_STREAM = new XStream(new DomDriver());
    private static final Logger LOGGER
            = LoggerFactory.getLogger(UserDataUtils.class);

    private static final File DEVICES_FILE = new File("src" + SEP_CHR
            + "main" + SEP_CHR
            + "resources" + SEP_CHR + "devices.xml");

    private static final File I2C_FILE = new File("src" + SEP_CHR
            + "main" + SEP_CHR
            + "resources" + SEP_CHR + "i2c_requests.xml");

    private static final File SPI_FILE = new File("src" + SEP_CHR
            + "main" + SEP_CHR
            + "resources" + SEP_CHR + "spi_requests.xml");

    private static boolean setup = false;
    private static final XStreamListWrapper<I2cRequestValueObject> I2C_REQUESTS
            = new I2cRequests(initI2cRequestsFromFile());
    private static final XStreamListWrapper<SpiRequestValueObject> SPI_REQUESTS
            = new SpiRequests(initSpiRequestsFromFile());
    private static final XStreamListWrapper<DeviceValueObject> DEVICES
            = new Devices(getDevicesFromFile());

    private UserDataUtils() {
    }

    public static void saveAllRequests() {
        try {
            setupAliasesIfNecessary();
            if (I2C_REQUESTS.isDirty()) {
                X_STREAM.toXML(I2C_REQUESTS, new FileWriter(I2C_FILE));
                LOGGER.info("New I2C request templates saved.");
            }
            if (SPI_REQUESTS.isDirty()) {
                X_STREAM.toXML(SPI_REQUESTS, new FileWriter(SPI_FILE));
                LOGGER.info("New SPI request templates saved.");
            }
        } catch (IOException ex) {
            LOGGER.error("Could not save user data ", ex);
        }
    }

    public static void saveAllDevices() {
        if (DEVICES.isDirty()) {
            try {
                X_STREAM.toXML(DEVICES, new FileWriter(DEVICES_FILE));
                LOGGER.info("New device info saved.");
            } catch (IOException ex) {
                LOGGER.error("Could not save user data ", ex);
            }
        }
    }

    private static void initXStream() {
        X_STREAM.alias("operation", Operation.class);
        X_STREAM.alias("address", InetAddress.class);
        X_STREAM.alias("lastTimeConnected", LocalDateTime.class);

        X_STREAM.alias("i2cRequest", I2cRequestValueObject.class);
        X_STREAM.alias("spiRequest", SpiRequestValueObject.class);
        X_STREAM.alias("device", DeviceValueObject.class);

        X_STREAM.alias("i2cRequests", I2cRequests.class);
        X_STREAM.alias("spiRequests", SpiRequests.class);
        X_STREAM.alias("devices", Devices.class);

        X_STREAM.omitField(XStreamListWrapper.class, "dirty");
        X_STREAM.omitField(DeviceValueObject.class, "dirty");
        X_STREAM.omitField(DeviceValueObject.class, "disconnected");

        X_STREAM.addImplicitCollection(I2cRequests.class, "list",
                I2cRequestValueObject.class);
        X_STREAM.addImplicitCollection(SpiRequests.class, "list",
                SpiRequestValueObject.class);
        X_STREAM.addImplicitCollection(Devices.class, "list",
                DeviceValueObject.class);
    }

    private static void setupAliasesIfNecessary() {
        if (!setup) {
            initXStream();
            setup = true;
        }
    }

    public static List<DeviceValueObject> getDevices() {
        return DEVICES.getItems();
    }

    public static void addNewDeviceToFile(DeviceValueObject address) {
        if (!DEVICES.contains(address)) {
            DEVICES.addNewItem(address);
        }
    }

    public static ObservableList<I2cRequestValueObject> getI2cRequests() {
        return FXCollections.observableArrayList(I2C_REQUESTS.getItems());
    }

    public static void addNewI2cRequest(I2cRequestValueObject request) {
        if (!I2C_REQUESTS.contains(request)) {
            I2C_REQUESTS.addNewItem(request);
        }
    }

    private static <T> List<T> initItemsFromFile(File file) {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        setupAliasesIfNecessary();
        XStreamListWrapper requests
                = (XStreamListWrapper) X_STREAM.fromXML(file);
        // Due to crippled implementation of XStream, this has to be checked...
        if (requests == null || requests.getItems() == null) {
            return new ArrayList<>();
        }
        return requests.getItems();
    }

    private static List<SpiRequestValueObject> initSpiRequestsFromFile() {
        return initItemsFromFile(SPI_FILE);
    }

    private static List<I2cRequestValueObject> initI2cRequestsFromFile() {
        return initItemsFromFile(I2C_FILE);
    }

    private static List<DeviceValueObject> getDevicesFromFile() {
        return initItemsFromFile(DEVICES_FILE);
    }

    public static ObservableList<SpiRequestValueObject> getSpiRequests() {
        return FXCollections.observableArrayList(SPI_REQUESTS.getItems());
    }

    public static void addNewSpiRequest(SpiRequestValueObject request) {
        if (!SPI_REQUESTS.contains(request)) {
            SPI_REQUESTS.addNewItem(request);
        }
    }
}
