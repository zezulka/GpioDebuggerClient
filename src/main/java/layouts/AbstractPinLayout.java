package layouts;


import io.silverspoon.bulldog.core.pin.Pin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;



/**
 *
 * @author Miloslav Zezulka, 2017
 */
public abstract class AbstractPinLayout implements PinLayout {
    
    private List<JButton> buttons;
    private List<Pin> ports;
    
    public AbstractPinLayout(List<Pin> ports) {
        this.buttons = new ArrayList<>();
        this.ports = new ArrayList<>(ports);
    }
    
    @Override
    public List<Pin> getPins() {
        return Collections.unmodifiableList(this.ports);
    }

}
