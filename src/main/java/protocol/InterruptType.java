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
    
    public static InterruptType getType(String typeName) {
        for(InterruptType t : InterruptType.values()) {
            if(t.type.equals(typeName)) {
                return t;
            }
        }
        throw new IllegalArgumentException("pin with the given name has not been found");
    }
}
