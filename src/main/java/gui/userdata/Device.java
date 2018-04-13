package gui.userdata;

import gui.userdata.xstream.XStreamUtils;

import java.net.InetAddress;
import java.time.LocalDateTime;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import net.AgentConnection;
import protocol.BoardType;

public final class Device {

    private final InetAddress address;
    private BoardType boardType;
    private LocalDateTime timeConnected;
    private boolean dirty = false;
    private AgentConnection connection = null;
    private BooleanProperty active;

    /**
     * Constructor without BoardType input variable. If such constructor is
     * used, BoardType.UNKNOWN is used.
     */
    public Device(InetAddress address) {
        this(address, BoardType.UNKNOWN);
    }

    public Device(InetAddress address, BoardType device) {
        this(address, device, null);
    }

    public Device(InetAddress address, BoardType device,
                  LocalDateTime timeConnected) {
        this.address = address;
        this.timeConnected = timeConnected;
        this.boardType = device;
        this.active = new SimpleBooleanProperty(false);
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public boolean isDirty() {
        return dirty;
    }

    public BoardType getBoardType() {
        return boardType;
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

    public AgentConnection getConnection() {
        return connection;
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

    public void setConnection(AgentConnection connection) {
        this.connection = connection;
    }

    @Override
    public String toString() {
        return "Device{" + "address=" + address
                + ", boardType=" + boardType
                + ", timeConnected=" + timeConnected;
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
        final Device other = (Device) obj;
        return address.equals(other.address);
    }
}
