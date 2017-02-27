package protocol;

import io.silverspoon.bulldog.raspberrypi.RaspberryPiBoardFactory;

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
        return this.name;
    }
}
