package gui.userdata;

import gui.userdata.xstream.XStreamUtils;
import java.net.InetAddress;
import java.time.LocalDateTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import protocol.BoardType;

public final class DeviceValueObject {

    private final InetAddress address;
    private BoardType boardType;
    private LocalDateTime timeConnected;
    private BooleanProperty disconnected;
    private boolean dirty = false;

    /**
     * Constructor without BoardType input variable. If such constructor is
     * used, BoardType.UNKNOWN is used.
     */
    public DeviceValueObject(InetAddress address) {
        this(address, BoardType.UNKNOWN);
    }

    public DeviceValueObject(InetAddress address, BoardType device) {
        this(address, device, null);
    }

    public DeviceValueObject(InetAddress address, BoardType device,
            LocalDateTime timeConnected) {
        this.address = address;
        this.timeConnected = timeConnected;
        this.boardType = device;
    }

    public boolean isDirty() {
        return dirty;
    }

    public BoardType getBoardType() {
        return boardType;
    }

    public BooleanProperty disconnectedProperty() {
        if (disconnected == null) {
            disconnected = new SimpleBooleanProperty(true);
        }
        return disconnected;
    }

    public String getHostName() {
        return this.address.getHostName();
    }

    public InetAddress getAddress() {
        return address;
    }

    public LocalDateTime getTimeConnected() {
        return timeConnected;
    }

    public String getTimeConnectedStr() {
        if (timeConnected == null) {
            return "never";
        }
        return timeConnected.format(XStreamUtils.DATE_TIME_FORMATTER);
    }

    public void setTimeConnected(LocalDateTime ldt) {
        this.dirty = true;
        this.timeConnected = ldt;
    }

    public void setBoardType(BoardType boardType) {
        this.boardType = boardType;
    }

    @Override
    public String toString() {
        String ipAddress = address.getHostAddress();
        String board = getBoardType().toString();
        String lastConnection = getTimeConnectedStr();

        return "IP: " + ipAddress + '\n'
                + "Board: " + board + '\n'
                + "Connected at: " + lastConnection;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + address.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DeviceValueObject other = (DeviceValueObject) obj;
        return address.equals(other.address);
    }
}
