package net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import properties.AppPreferencesExtractor;


/**
 *
 * One of the main motivations behind creating this util class was to get
 * rid of catching networking related exceptions in JavaFX controllers which
 * cluttered up the code.
 */
public final class NetworkingUtils {

    private NetworkingUtils() {
        //do not construct instances
    }

    /**
     *
     * @return null if hostname could not be resolved
     */
    public static InetAddress getAddressFromHostname(String hostname) {
        Objects.requireNonNull(hostname, "hostname");
        try {
            return InetAddress.getByName(hostname);
        } catch (SecurityException | UnknownHostException e) {
            return null;
        }
    }

    public static boolean isNotReachable(InetAddress address) {
        Objects.requireNonNull(address);
        try {
            return !address.isReachable(AppPreferencesExtractor.timeout());
        } catch (IOException e) {
            return true;
        }
    }

}
