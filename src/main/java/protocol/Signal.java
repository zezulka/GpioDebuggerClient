package protocol;

/**
 * Minimalist signal representation
 */
public enum Signal {
    HIGH(true), LOW(false);

    private final boolean value;

    Signal(boolean value) {
        this.value = value;
    }

    public boolean getBooleanValue() {
        return this.value;
    }
}
