package userdata;

import layouts.controllers.Operation;

public final class SpiRequestValueObject {

    private final int chipSelect;
    private final Operation operation;
    private final String bytes;

    public SpiRequestValueObject(int chipSelect, Operation operation,
            String bytes) {
        this.chipSelect = chipSelect;
        this.operation = operation;
        this.bytes = bytes;
    }

    public int getChipSelect() {
        return chipSelect;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return "operation=" + operation
                + ", chipSelect=" + chipSelect
                + ", bytes=" + bytes;
    }
}
