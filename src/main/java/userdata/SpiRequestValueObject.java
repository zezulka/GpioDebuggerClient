package userdata;

import java.util.List;
import layouts.controllers.Operation;

public final class SpiRequestValueObject {
    private final int chipSelect;
    private final Operation operation;
    private final List<String> bytes;

    public SpiRequestValueObject(int chipSelect, Operation operation, List<String> bytes) {
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

    public List<String> getBytes() {
        return bytes;
    }
    
    @Override
    public String toString() {
        return "operation=" + operation + ", chipSelect=" + chipSelect + ", bytes=" + bytes + '}';
    }
}
