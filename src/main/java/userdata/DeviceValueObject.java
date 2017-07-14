package userdata;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DeviceValueObject {
    private final InetAddress address;
    private LocalDateTime timeConnected;
    private boolean dirty = false;

    public DeviceValueObject(InetAddress address) {
        this(address, null);
    }

    public DeviceValueObject(InetAddress address, LocalDateTime timeConnected) {
        this.address = address;
        this.timeConnected = timeConnected;
    }
    
    public boolean isDirty() {
        return dirty;
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

    @Override
    public String toString() {
        return  "\nIP: " + address.getHostAddress() 
                + (this.getHostName().equals(address.getHostAddress()) ? "" : ("\nHostname: " + this.getHostName()))
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
