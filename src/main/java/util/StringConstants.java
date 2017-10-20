package util;

import java.io.File;

/**
 * Prefix naming legend: F_ = formatter String ERR = String which should
 * represent error message being displayed to the user
 *
 */
public enum StringConstants {

    USER_INFO_PARAM("UserInfo"),
    /**
     * This string is sent from agent and signalises successful write operation.
     */
    WRITE_OK("Write OK"),
    CONNECTION_OK("Connection to server OK"),
    CONNECTION_NOK("I/O error while trying to communicate with server"),
    SERVER_READY("The system is ready to accept requests."),
    RESPONSE_WAIT("Waiting for server to response..."),
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
    ERR_NOT_CONNECTED("Connection has not been established with server."),
    ERR_CANNOT_CONNECT("Cannot connect to device.\nMake sure "
            + "agent is running on the specified address."),
    ERR_NO_BOARD("No device is currently binded to this session!"),
    ERR_NOT_BUTTON("The clicked entity is not of Button type, ignoring..."),
    ERR_ALREADY_CLOSED("Cannot close connection to server: already closed"),
    ERR_ALREADY_CONNECTED("Connection has already been established "
            + "for this IP address."),
    ERR_GUI("GUI error"),
    ERR_GUI_FXML("cannot load fxml file");

    private final String msg;

    StringConstants(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return this.msg;
    }
}
