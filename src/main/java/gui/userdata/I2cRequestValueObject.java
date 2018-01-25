package gui.userdata;

import gui.misc.Operation;

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
        String len = "";
        String byteArray = "";
        if (!operation.equals(Operation.READ)) {
            byteArray = ", bytes=" + bytes;
        }
        if (!operation.equals(Operation.WRITE)) {
            len = ", length=" + length;
        }
        return "op=" + operation
                + ", slave=0x" + slaveAddress
                + len
                + byteArray;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(operation);
        hash = 13 * hash + Objects.hashCode(slaveAddress);
        hash = 17 * hash + length;
        hash = 19 * hash + Objects.hashCode(bytes);
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
        return length == other.length
                && Objects.equals(slaveAddress, other.slaveAddress)
                && Objects.equals(bytes, other.bytes)
                && operation.equals(other.operation);
    }

}
