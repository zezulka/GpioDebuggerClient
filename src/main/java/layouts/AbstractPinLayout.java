package layouts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public abstract class AbstractPinLayout implements PinLayout {
    
    private List<ClientPin> pins;
    
    protected AbstractPinLayout(List<ClientPin> pins) {
        if(pins == null) {
            throw new UnsupportedOperationException("not supported yet.");
        }
        this.pins = new ArrayList<>(pins);
    }
    
    @Override 
    public List<ClientPin> getPins() {
        return Collections.unmodifiableList(this.pins);
    }
    
    @Override
    public ClientPin getPinFromIndex(int i) {
        for(ClientPin pin : pins) {
            if(pin.getPort() == i) {
                return pin;
            }
        }
        throw new IllegalArgumentException("index not found: " + i);
    }

}
