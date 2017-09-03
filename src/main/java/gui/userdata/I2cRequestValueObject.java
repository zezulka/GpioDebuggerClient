package gui.userdata;

import gui.layouts.controllers.Operation;

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
}
