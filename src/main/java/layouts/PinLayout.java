package layouts;


import io.silverspoon.bulldog.core.pin.Pin;

import java.util.List;
import javax.swing.JButton;


/**
 *
 * @author Miloslav Zezulka, 2017
 */
public interface PinLayout {
    List<Pin> getPins();
    List<JButton> getButtons();
}
