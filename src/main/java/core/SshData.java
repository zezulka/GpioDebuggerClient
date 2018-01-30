package core;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Simple placeholder class for the customer entered survey response.
 */
public class SshData {
    public static SshData instance = new SshData();
    public StringProperty username = new SimpleStringProperty();
    public StringProperty ipAddress = new SimpleStringProperty();
    public StringProperty password = new SimpleStringProperty();
    public StringProperty localFile = new SimpleStringProperty();
    public StringProperty remoteFile = new SimpleStringProperty();
}
