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
    private List<Pin> pins;
    
    public AbstractPinLayout(List<Pin> pins) {
        this.buttons = new ArrayList<>();
        this.pins = new ArrayList<>(pins);
        this.createButtonsFromPins();
    }
    
    private void createButtonsFromPins() {
        for(Pin p : pins) {
            buttons.add(new JButton(p.getName()));
        }
    }
    
    @Override
    public List<Pin> getPins() {
        return Collections.unmodifiableList(this.pins);
    }
    
    @Override 
    public List<JButton> getButtons() {
        return Collections.unmodifiableList(this.buttons);
    }

}
