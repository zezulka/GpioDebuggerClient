package layouts;

import protocol.ClientPin;
import java.util.List;

/**
 * Deals with graphical representation of pins on an embedded device.
 * Each implementation of this interface therefore represents visual view of the
 * device pins. List of pins is usually backed up by appropriate ClientPin enum.
 * @author Miloslav Zezulka, 2017
 */
public interface PinLayout {
    List<ClientPin> getPins();
    ClientPin getPinFromIndex(int i);
}
