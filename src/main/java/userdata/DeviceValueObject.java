package userdata;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import protocol.BoardType;

public final class DeviceValueObject {
    private final InetAddress address;
    private BoardType boardType;
    private LocalDateTime timeConnected;
    private boolean dirty = false;

    public DeviceValueObject(InetAddress address, BoardType device) {
        this(address, device, null);
    }

    public DeviceValueObject(InetAddress address, BoardType device, LocalDateTime timeConnected) {
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
    
    public void setTimeConnected(LocalDateTime ldt) {
        this.dirty = true;
        this.timeConnected = ldt;
    }
    
    public void setBoardType(BoardType boardType) {
        this.boardType = boardType;
    }

    @Override
    public String toString() {
        return  "\nIP: " + address.getHostAddress() 
                + (this.getHostName().equals(address.getHostAddress()) ? "" : ("\nHostname: " + this.getHostName()))
                + (this.getBoardType() == null ? "" : "\nBoard type: " + this.getBoardType().toString())
                + "\nLast connection: " + (timeConnected == null ? "never" : timeConnected.format(DateTimeFormatter.ofPattern("dd-MM, HH:mm")));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + this.address.hashCode();
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
        return this.address.equals(other.address);
    }
    
    
}
