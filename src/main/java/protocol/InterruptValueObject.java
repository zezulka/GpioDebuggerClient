package protocol;

import java.time.LocalTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class InterruptValueObject {
    private final BooleanProperty selected;
    private final ClientPin clientPin;
    private final InterruptType type;
    private final LocalTime timeAdded;
    private final IntegerProperty numberOfInterrupts;
    private final ObjectProperty<State> state;
    private final ObjectProperty<LocalTime> latestInterruptTime;
    
    public InterruptValueObject(ClientPin clientPin, InterruptType type) {
        this.clientPin = clientPin;
        this.type = type;
        this.timeAdded = LocalTime.now();
        this.numberOfInterrupts = new SimpleIntegerProperty(0);
        this.latestInterruptTime = new SimpleObjectProperty<>();
        this.selected = new SimpleBooleanProperty(false);
        this.state = new SimpleObjectProperty<>(State.NOT_STARTED);
    }

    public ObjectProperty<State> stateProperty() {
        return this.state;
    }
    
    public BooleanProperty selectedProperty() {
        return selected;
    }

    public ClientPin getClientPin() {
        return clientPin;
    }

    public InterruptType getType() {
        return type;
    }

    public LocalTime getTimeAdded() {
        return timeAdded;
    }

    public IntegerProperty numberOfInterruptsProperty() {
        return numberOfInterrupts;
    }

    public ObjectProperty<LocalTime> latestInterruptTimeProperty() {
        return latestInterruptTime;
    }

    public void incrementNumberOfInterrupts() {
        this.numberOfInterrupts.set(numberOfInterrupts.get() + 1);
    }

    public void setLatestInterruptTime(LocalTime latestInterruptTime) {
        this.latestInterruptTime.setValue(LocalTime.from(latestInterruptTime));
    }

    public void setSelected(Boolean selected) {
        this.selected.setValue(selected);
    }
    
    public void setState(State state) {
        this.state.setValue(state);
    }
    
    public static enum State {
        RUNNING, NOT_STARTED, STOPPED;
    }
}
