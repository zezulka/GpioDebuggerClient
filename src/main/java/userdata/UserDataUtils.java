package userdata;
        
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDataUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataUtils.class);
    private static final File IP_ADDRESSES_FILE = new File("src" +  File.separatorChar + 
                                                           "main" + File.separatorChar + 
                                                           "resources" + File.separatorChar + "ip_addresses");

    private UserDataUtils() {
    }
    
    public static List<InetAddress> getAddressesFromFile() {
        List<InetAddress> addresses = new ArrayList<>();
        try {
            if(!IP_ADDRESSES_FILE.exists()) {
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
            if(getAddressesFromFile().contains(address)) {
                LOGGER.info("address already exists in the file, skipping...");
                return;
            }
            FileUtils.writeStringToFile(IP_ADDRESSES_FILE, address.getHostAddress() + '\n', "UTF-8", true);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    
}
