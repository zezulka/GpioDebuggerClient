package protocol;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public enum BoardType {
    RASPBERRY_PI("Raspberry Pi"),
    BEAGLEBONEBLACK("BeagleBone Black"),
    CUBIEBOARD("Cubieboard");

    private final String name;

    private BoardType(String name) {
        this.name = name;
    }

    private String getName() {
        return this.name;
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    public static BoardType parse(String name) {
        for(BoardType t : BoardType.values()) {
            if(t.getName().equals(name)) {
                return t;
            }
        }
        throw new IllegalArgumentException(name + " is not a BoardType!");
    }
}
