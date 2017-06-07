package layouts;

import protocol.ClientPin;
import java.util.List;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public interface PinLayout {
    List<ClientPin> getPins();
    ClientPin getPinFromIndex(int i);
}
