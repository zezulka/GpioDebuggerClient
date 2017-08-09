package userdata;

import java.net.InetAddress;
import java.time.LocalDateTime;
import protocol.BoardType;

public final class DeviceValueObject {
    private final InetAddress address;
    private BoardType boardType;
    private LocalDateTime timeConnected;
    private boolean dirty = false;

    /**
     * Constructor without BoardType input variable. If such constructor is
     * used, {@link BoardType.UNKNOWN} is used.
     * @param address
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
        return timeConnected.format(UserDataUtils.DATE_TIME_FORMATTER);
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
        String hostname = getHostName();
        String board = "Board type: " + getBoardType();
        String lastConnection = "Last connection: " + getTimeConnectedStr();

        return  ipAddress + '\n'
                + hostname + '\n'
                + board + '\n'
                + lastConnection;
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
