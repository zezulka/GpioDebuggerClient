package util;

import java.io.File;

/**
 * Prefix naming legend: F_ = formatter String ERR = String which should
 * represent error message being displayed to the user
 */
public final class StringConstants {

    private StringConstants() {
        /* Do not instantiate. */
    }

    public static final String WRITE_OK = "Write OK";
    public static final String CLOSE_WHEN_DEVICES_ACTIVE =
            "Are you sure you want to close "
                    + "the whole application?\n"
                    + "All connections to devices will be closed.";
    public static final String LISTENER_ACTIVE =
            "This interrupt listener is still active. "
                    + "It is necessary"
                    + " to stop it before removing it. Okay to continue?";
    public static final String OK_TO_DISCONNECT =
            "Really disconnect from this device?\n";
    public static final String PATH_TO_FXML_DIR =
            "src" + File.separator
                    + "main" + File.separator
                    + "resources" + File.separator
                    + "fxml";
    public static final String F_HOST_NOT_REACHABLE =
            "Host %s could not be reached.";
    public static final String ERR_ALREADY_CONNECTED =
            "Connection has already been established "
                    + "for this IP address.";

    //GUI
    public static final String TOOLTIP_HINT_CONNECT_BTN =
            "Connects to device. "
                    + "\nDevice must be selected in the device tree.";
    public static final String TOOLTIP_HINT_DISCONNECT_BTN =
            "Disconnects from device. "
                    + "\nDevice must be selected in the "
                    + "device tree and active.";
}
