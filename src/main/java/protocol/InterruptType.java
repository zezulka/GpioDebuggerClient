package protocol;

public enum InterruptType {
    FALLING_EGDE("Falling"),
    RISING_EDGE("Rising"),
    HIGH_LEVEL("High-level"),
    LOW_LEVEL("Low-level");
    
    private final String type;
    
    InterruptType(String type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return this.type;
    }
}
