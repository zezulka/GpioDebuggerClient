package userdata;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import layouts.controllers.Operation;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDataUtils {

    private static final XStream X_STREAM = new XStream(new DomDriver());
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataUtils.class);

    private static final File IP_ADDRESSES_FILE = new File("src" + File.separatorChar
            + "main" + File.separatorChar
            + "resources" + File.separatorChar + "ip_addresses");
    private static final File I2C_REQUESTS_FILE = new File("src" + File.separatorChar
            + "main" + File.separatorChar
            + "resources" + File.separatorChar + "i2c_requests.xml");
    private static final File SPI_REQUESTS_FILE = new File("src" + File.separatorChar
            + "main" + File.separatorChar
            + "resources" + File.separatorChar + "spi_requests.xml");

    private static boolean IS_SETUP = false;
    private static final I2cRequests I2C_REQUESTS = new I2cRequests(initI2cRequestsFromFile());
    private static final SpiRequests SPI_REQUESTS = new SpiRequests(initSpiRequestsFromFile());

    private UserDataUtils() {
    }

    public static void saveAllRequests() {
        try {
            setupAliasesIfNecessary();
            if(I2C_REQUESTS.isDirty()) {
                X_STREAM.toXML(I2C_REQUESTS, new FileWriter(I2C_REQUESTS_FILE));
                LOGGER.info("New I2C request templates saved.");
            }
            if(SPI_REQUESTS.isDirty()) {
                X_STREAM.toXML(SPI_REQUESTS, new FileWriter(SPI_REQUESTS_FILE));
                LOGGER.info("New SPI request templates saved.");
            }
        } catch (IOException ex) {
            LOGGER.error("Could not save user data ", ex);
        }
    }

    private static void initXStream() {
        X_STREAM.alias("operation", Operation.class);
        X_STREAM.alias("i2cRequest", I2cRequestValueObject.class);
        X_STREAM.alias("spiRequest", SpiRequestValueObject.class);
        X_STREAM.alias("i2cRequests", I2cRequests.class);
        X_STREAM.alias("spiRequests", SpiRequests.class);
        X_STREAM.omitField(I2cRequests.class, "isDirty");
        X_STREAM.omitField(SpiRequests.class, "isDirty");
        X_STREAM.addImplicitCollection(I2cRequests.class, "requests", I2cRequestValueObject.class);
        X_STREAM.addImplicitCollection(SpiRequests.class, "requests", SpiRequestValueObject.class);
    }

    private static void setupAliasesIfNecessary() {
        if(!IS_SETUP) {
            initXStream();
            IS_SETUP = true;
        }
    }

    public static List<InetAddress> getAddressesFromFile() {
        List<InetAddress> addresses = new ArrayList<>();
        try {
            if (!IP_ADDRESSES_FILE.exists()) {
                return Collections.EMPTY_LIST;
            }
            List<String> lines = FileUtils.readLines(IP_ADDRESSES_FILE, "UTF-8");
            addresses = new ArrayList<>();
            for (String line : lines) {
                addresses.add(InetAddress.getByName(line));
            }
            return addresses;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return addresses;
    }

    public static void putNewAddressEntryIntoFile(InetAddress address) {
        try {
            if (getAddressesFromFile().contains(address)) {
                LOGGER.info("address already exists in the file, skipping...");
                return;
            }
            FileUtils.writeStringToFile(IP_ADDRESSES_FILE, address.getHostAddress() + '\n', "UTF-8", true);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    private static List<I2cRequestValueObject> initI2cRequestsFromFile() {
        if (!I2C_REQUESTS_FILE.exists()) {
            return new ArrayList<>();
        }
        setupAliasesIfNecessary();
        return ((I2cRequests) X_STREAM.fromXML(I2C_REQUESTS_FILE)).getRequests();
    }

    public static List<I2cRequestValueObject> getI2cRequests() {
        return I2C_REQUESTS.getRequests();
    }

    public static void addNewI2cRequest(I2cRequestValueObject request) {
        if(!I2C_REQUESTS.contains(request)) {
            I2C_REQUESTS.setDirty(true);
            I2C_REQUESTS.addNewRequest(request);
        }
    }

    private static List<SpiRequestValueObject> initSpiRequestsFromFile() {
        if (!SPI_REQUESTS_FILE.exists()) {
            return new ArrayList<>();
        }
        return ((SpiRequests) X_STREAM.fromXML(SPI_REQUESTS_FILE)).getRequests();
    }

    public static List<SpiRequestValueObject> getSpiRequests() {
        return SPI_REQUESTS.getRequests();
    }
    
    public static void addNewSpiRequest(SpiRequestValueObject request) {
        if(!SPI_REQUESTS.contains(request)) {
            SPI_REQUESTS.setDirty(true);
            SPI_REQUESTS.addNewRequest(request);
        }
    }
}
