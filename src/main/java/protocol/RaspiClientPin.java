package protocol;

/**
 *
 * @author Miloslav Zezulka
 */
public enum RaspiClientPin implements ClientPin {
    PWR_1("3.3V", 1),
    PWR_2("5V", 2),
    PWR_3("5V", 4),
    GND_1("GND", 6),
    GND_2("GND", 9),
    GND_3("GND", 14),
    PWR_4("3.3V", 17),
    GND_4("GND", 20),
    GND_5("GND", 25),
    ID_SD("ID_SD", 27),
    ID_SC("ID_SC", 28),
    GND_6("GND", 30),
    GND_7("GND", 34), 
    GND_8("GND", 39),
    
    P1_3("P1_3", 3),
    P1_5("P1_5", 5),
    P1_7("P1_7", 7),
    P1_8("P1_8", 8),
    P1_10("P1_10", 10),
    P1_11("P1_11", 11),
    P1_12("P1_12", 12),
    P1_13("P1_13", 13),
    P1_15("P1_15", 15),
    P1_16("P1_16", 16),
    P1_18("P1_18", 18),
    P1_19("P1_19", 19),
    P1_21("P1_21", 21),
    P1_22("P1_22", 22),
    P1_23("P1_23", 23),
    P1_24("P1_24", 24),
    P1_26("P1_26", 26),
    P1_29("P1_29", 29),
    P1_31("P1_31", 31),
    P1_32("P1_32", 32),
    P1_33("P1_33", 33),
    P1_35("P1_35", 35),
    P1_36("P1_36", 36),
    P1_37("P1_37", 37),
    P1_38("P1_38", 38),
    P1_40("P1_40", 40);
    
    private final String name;
    private final int port;

    RaspiClientPin(String name, int port) {
        this.name = name;
        this.port = port;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    public static ClientPin getPin(String pinName) {
        for(RaspiClientPin pin : RaspiClientPin.values()) {
            if(pin.name.equals(pinName)) {
                return pin;
            }
        }
        throw new IllegalArgumentException("pin with the given name has not been found");
    }

    @Override
    public int getPort() {
        return this.port;
    }  

    @Override
    public String toString() {
        return this.name; 
    }

    
    public static ClientPin[] pins() {
        return RaspiClientPin.values();
    }

    @Override
    public boolean isGpio() {
        return this.name.startsWith("P");
    }
    
}
