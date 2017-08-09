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
import layouts.controllers.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserDataUtils {

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM, HH:mm");

    private static final char SEP_CHR = File.separatorChar;
    private static final XStream X_STREAM = new XStream(new DomDriver());
    private static final Logger LOGGER =
            LoggerFactory.getLogger(UserDataUtils.class);

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
    private static final I2cRequests I2C_REQUESTS =
            new I2cRequests(initI2cRequestsFromFile());
    private static final SpiRequests SPI_REQUESTS =
            new SpiRequests(initSpiRequestsFromFile());
    private static final Devices DEVICES = new Devices(getDevicesFromFile());

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
                LOGGER.info("New devices saved.");
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

        X_STREAM.omitField(I2cRequests.class, "dirty");
        X_STREAM.omitField(SpiRequests.class, "dirty");
        X_STREAM.omitField(Devices.class, "dirty");
        X_STREAM.omitField(DeviceValueObject.class, "dirty");

        X_STREAM.addImplicitCollection(I2cRequests.class, "requests",
                I2cRequestValueObject.class);
        X_STREAM.addImplicitCollection(SpiRequests.class, "requests",
                SpiRequestValueObject.class);
        X_STREAM.addImplicitCollection(Devices.class, "devices",
                DeviceValueObject.class);
    }

    private static void setupAliasesIfNecessary() {
        if (!setup) {
            initXStream();
            setup = true;
        }
    }

    public static List<DeviceValueObject> getDevices() {
       return DEVICES.getRequests();
    }

    private static List<DeviceValueObject> getDevicesFromFile() {
        if (!DEVICES_FILE.exists()) {
            return new ArrayList<>();
        }
        setupAliasesIfNecessary();
        return ((Devices) X_STREAM.fromXML(DEVICES_FILE)).getRequests();
    }

    public static void addNewDeviceToFile(DeviceValueObject address) {
        if (!DEVICES.contains(address)) {
            DEVICES.addNewRequest(address);
        }
    }

    private static List<I2cRequestValueObject> initI2cRequestsFromFile() {
        if (!I2C_FILE.exists()) {
            return new ArrayList<>();
        }
        setupAliasesIfNecessary();
        return ((I2cRequests) X_STREAM.fromXML(I2C_FILE)).getRequests();
    }

    public static ObservableList<I2cRequestValueObject> getI2cRequests() {
        return FXCollections.observableArrayList(I2C_REQUESTS.getRequests());
    }

    public static void addNewI2cRequest(I2cRequestValueObject request) {
        if (!I2C_REQUESTS.contains(request)) {
            I2C_REQUESTS.addNewRequest(request);
        }
    }

    private static List<SpiRequestValueObject> initSpiRequestsFromFile() {
        if (!SPI_FILE.exists()) {
            return new ArrayList<>();
        }
        setupAliasesIfNecessary();
        return ((SpiRequests) X_STREAM.fromXML(SPI_FILE)).getRequests();
    }

    public static ObservableList<SpiRequestValueObject> getSpiRequests() {
        return FXCollections.observableArrayList(SPI_REQUESTS.getRequests());
    }

    public static void addNewSpiRequest(SpiRequestValueObject request) {
        if (!SPI_REQUESTS.contains(request)) {
            SPI_REQUESTS.addNewRequest(request);
        }
    }
}
