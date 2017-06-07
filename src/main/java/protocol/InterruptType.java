package protocol;

public enum InterruptType {
    FALLING_EGDE("falling edge"),
    RISING_EDGE("rising edge"),
    HIGH_LEVEL("high level"),
    LOW_LEVEL("low level");
    
    private final String type;
    
    InterruptType(String type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return type;
    }
}
