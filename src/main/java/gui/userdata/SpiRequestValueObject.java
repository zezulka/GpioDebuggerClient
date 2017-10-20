package gui.userdata;

import gui.misc.Operation;
import java.util.Objects;

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
        return "op=" + operation
                + ", CS=" + chipSelect
                + ", bytes=" + bytes;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 7 * hash + this.chipSelect;
        hash = 11 * hash + Objects.hashCode(this.operation);
        hash = 13 * hash + Objects.hashCode(this.bytes);
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
        final SpiRequestValueObject other = (SpiRequestValueObject) obj;
        if (this.chipSelect != other.chipSelect) {
            return false;
        }
        if (!Objects.equals(this.bytes, other.bytes)) {
            return false;
        }
        return this.operation.equals(other.operation);
    }
}
