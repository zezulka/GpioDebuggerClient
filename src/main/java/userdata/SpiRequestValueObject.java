package userdata;

import layouts.controllers.Operation;

public final class SpiRequestValueObject {
    private final int chipSelect;
    private final Operation operation;
    private final short[] bytes;

    public SpiRequestValueObject(int chipSelect, Operation operation, short[] bytes) {
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

    public short[] getBytes() {
        return bytes;
    }
}
