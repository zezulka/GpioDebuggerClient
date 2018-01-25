package util;

import java.io.File;

/**
 * Prefix naming legend: F_ = formatter String ERR = String which should
 * represent error message being displayed to the user
 *
 */
public enum StringConstants {

    /**
     * This string is sent from agent and signalises successful write operation.
     */
    WRITE_OK("Write OK"),
    CLOSE_WHEN_DEVICES_ACTIVE("Are you sure you want to close "
            + "the whole application?\n"
            + "All connections to devices will be closed."),
    LISTENER_ACTIVE("This interrupt listener is still active. It is necessary"
            + " to stop it before removing it. Okay to continue?"),
    OK_TO_DISCONNECT("Really disconnect from this device?\n"),
    PATH_TO_FXML_DIR(
            "src" + File.separator
            + "main" + File.separator
            + "resources" + File.separator
            + "fxml"),
    F_HOST_NOT_REACHABLE("Host %s could not be reached."),
    ERR_ALREADY_CONNECTED("Connection has already been established "
            + "for this IP address.");

    private final String msg;

    StringConstants(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return this.msg;
    }
}
