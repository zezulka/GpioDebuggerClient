package userdata;

import java.util.List;
import layouts.controllers.Operation;

public final class I2cRequestValueObject {
    private final Operation operation;
    private final String slaveAddress;
    private final int length;
    private final List<String> bytes;

    public I2cRequestValueObject(Operation operation, String slaveAddress, int length, List<String> bytes) {
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

    public List<String> getBytes() {
        return bytes;
    }
    
    @Override
    public String toString() {
        return "operation=" + operation + ", slaveAddress=0x" + slaveAddress + ", length=" + length + (bytes.isEmpty() ? ", bytes=" + bytes + '}' : "");
    }
    
    
}
