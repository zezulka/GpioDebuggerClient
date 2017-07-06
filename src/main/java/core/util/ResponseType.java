package core.util;

public enum ResponseType {
    GPIO,
    SPI,
    I2C,
    INTR_STOPPED,
    INTR_STARTED,
    INTR_GENERATED;
    
    public boolean isInterruptMessage() {
        if (this == null) {
            return false;
        }
        return this.equals(INTR_GENERATED) ||
               this.equals(INTR_STARTED) ||
               this.equals(INTR_STOPPED);
    }
}
