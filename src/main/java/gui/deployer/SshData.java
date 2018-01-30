package gui.deployer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class SshData {
    private StringProperty username = new SimpleStringProperty();
    private StringProperty ipAddress = new SimpleStringProperty();
    private StringProperty password = new SimpleStringProperty();
    private StringProperty localFile = new SimpleStringProperty();
    private StringProperty remoteFile = new SimpleStringProperty();

    public String getUsername() {
        return username.get();
    }

    public String getIpaddress() {
        return ipAddress.get();
    }

    public String getPassword() {
        return password.get();
    }

    public String getLocalFile() {
        return localFile.get();
    }

    public String getRemoteFile() {
        return remoteFile.get();
    }

    private void bindProperty(StringProperty binded, StringProperty bounder) {
        if (binded.isBound()) {
            throw new IllegalStateException("this property is bound already!");
        }
        binded.bind(bounder);
    }

    public void bindUsername(StringProperty prop) {
        bindProperty(username, prop);
    }

    public void bindIpAddress(StringProperty prop) {
        bindProperty(ipAddress, prop);
    }

    public void bindPassword(StringProperty prop) {
        bindProperty(password, prop);
    }

    public void bindLocalFile(StringProperty prop) {
        bindProperty(localFile, prop);
    }

    public void bindRemoteFile(StringProperty prop) {
        bindProperty(remoteFile, prop);
    }
}
