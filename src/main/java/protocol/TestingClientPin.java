package protocol;

public enum TestingClientPin implements ClientPin {

    T_01("T_01", 1),
    T_02("T_02", 2),
    T_03("T_03", 3),
    T_04("T_04", 4),
    T_05("T_05", 5),
    T_06("T_06", 6),
    T_07("T_07", 7),
    T_08("T_08", 8),
    T_09("T_09", 9),
    T_10("T_10", 10);
    
    private final String id;
    private final int port;

    TestingClientPin(String id, int port) {
        this.id = id;
        this.port = port;
    }

    @Override
    public String getPinId() {
        return id;
    }

    @Override
    public String getGpioName() {
        return id;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isGpio() {
        return true;
    }

    static ClientPin getPin(String pinName) {
        for (TestingClientPin pin : TestingClientPin.values()) {
            if (pin.id.equals(pinName)) {
                return pin;
            }
        }
        throw new IllegalArgumentException("pin not found");
    }
    
}
