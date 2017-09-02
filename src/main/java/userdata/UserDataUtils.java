package userdata;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
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
    private static final XStream X_STREAM = new XStream(new DomDriver());
    private static final Logger LOGGER
            = LoggerFactory.getLogger(UserDataUtils.class);

    private static boolean setup = false;
    private static final XStreamListWrapper<I2cRequestValueObject> I2C_REQUESTS
            = new I2cRequests(initI2cRequestsFromFile());
    private static final XStreamListWrapper<SpiRequestValueObject> SPI_REQUESTS
            = new SpiRequests(initSpiRequestsFromFile());
    private static final XStreamListWrapper<DeviceValueObject> DEVICES
            = new Devices(getDevicesFromFile());

    private UserDataUtils() {
    }

    static <T> void saveCollectionsToAssociatedFiles(
            XStreamListWrapper... colls) {

        setupAliasesIfNecessary();
        for (XStreamListWrapper col : colls) {
            if (col.isDirty()) {
                try {
                    File f = col.getAssociatedFile();
                    f.createNewFile();
                    X_STREAM.toXML(col, new FileWriter(f));
                    LOGGER.info("New data saved to: " + f);
                } catch (IOException ex) {
                    throw new XStreamException(ex);
                }
            }
        }
    }

    public static void saveAllRequests() {
        saveCollectionsToAssociatedFiles(I2C_REQUESTS, SPI_REQUESTS);
    }

    public static void saveAllDevices() {
        if (DEVICES.isDirty()) {
            try {
                X_STREAM.toXML(DEVICES,
                        new FileWriter(XmlUserdata.DEVICES_FILE));
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

        X_STREAM.omitField(AbstractXStreamListWrapper.class, "dirty");
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

    public static ObservableList<I2cRequestValueObject> getI2cRequests() {
        return FXCollections.observableArrayList(I2C_REQUESTS.getItems());
    }

    static <T> void addNewItemToCollection(T item,
            XStreamListWrapper<T> collection) {
        collection.addItem(item);
    }

    public static void addNewDeviceToFile(DeviceValueObject address) {
        addNewItemToCollection(address, DEVICES);
    }

    public static void addNewI2cRequest(I2cRequestValueObject request) {
        addNewItemToCollection(request, I2C_REQUESTS);
    }

    public static void addNewSpiRequest(SpiRequestValueObject request) {
        addNewItemToCollection(request, SPI_REQUESTS);
    }

    static <T> List<T> initItemsFromFile(File file) {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        setupAliasesIfNecessary();
        // Uncaught exceptions might be thrown, deal with this in a sane way
        // CannotResolveClassException - this is demonstrated
        //     on uknown_collection.xml
        try {
            XStreamListWrapper requests
                    = (XStreamListWrapper) X_STREAM.fromXML(file);
            if (requests == null || requests.getItems() == null) {
                return new ArrayList<>();
            }
            return requests.getItems();
        } catch (CannotResolveClassException | NoClassDefFoundError ex) {
            LOGGER.error("Corrupted file found: " + file.getAbsoluteFile());
            throw new XStreamException(ex);
        }
    }

    private static List<SpiRequestValueObject> initSpiRequestsFromFile() {
        return initItemsFromFile(XmlUserdata.SPI_FILE);
    }

    private static List<I2cRequestValueObject> initI2cRequestsFromFile() {
        return initItemsFromFile(XmlUserdata.I2C_FILE);
    }

    private static List<DeviceValueObject> getDevicesFromFile() {
        return initItemsFromFile(XmlUserdata.DEVICES_FILE);
    }

    public static ObservableList<SpiRequestValueObject> getSpiRequests() {
        return FXCollections.observableArrayList(SPI_REQUESTS.getItems());
    }
}
