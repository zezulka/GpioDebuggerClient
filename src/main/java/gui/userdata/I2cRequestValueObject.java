package gui.userdata;

import gui.layouts.controllers.Operation;
import java.util.Objects;

public final class I2cRequestValueObject {

    private final Operation operation;
    private final String slaveAddress;
    private final int length;
    private final String bytes;

    public I2cRequestValueObject(Operation operation, String slaveAddress,
            int length, String bytes) {
        this.operation = operation;
        this.slaveAddress = slaveAddress;
        this.length = length;
        this.bytes = bytes;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getSlaveAddress() {
        return slaveAddress;
    }

    public int getLength() {
        return length;
    }

    public String getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        String byteArray = "";
        if (operation.isWriteOperation()) {
            byteArray = ", bytes=" + bytes;
        }
        return "operation=" + operation
                + ", slaveAddress=0x" + slaveAddress
                + ", length=" + length
                + byteArray;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.operation);
        hash = 13 * hash + Objects.hashCode(this.slaveAddress);
        hash = 17 * hash + this.length;
        hash = 19 * hash + Objects.hashCode(this.bytes);
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
        final I2cRequestValueObject other = (I2cRequestValueObject) obj;
        if (this.length != other.length) {
            return false;
        }
        if (!Objects.equals(this.slaveAddress, other.slaveAddress)) {
            return false;
        }
        if (!Objects.equals(this.bytes, other.bytes)) {
            return false;
        }
        return this.operation.equals(other.operation);
    }


}
