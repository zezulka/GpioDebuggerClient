package protocol;

import java.time.LocalTime;

import java.util.Objects;

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
    private final ObjectProperty<ListenerState> state;
    private final ObjectProperty<LocalTime> latestInterruptTime;
    
    public InterruptValueObject(ClientPin clientPin, InterruptType type) {
        this.clientPin = clientPin;
        this.type = type;
        this.timeAdded = LocalTime.now();
        this.numberOfInterrupts = new SimpleIntegerProperty(0);
        this.latestInterruptTime = new SimpleObjectProperty<>();
        this.selected = new SimpleBooleanProperty(false);
        this.state = new SimpleObjectProperty<>(ListenerState.NOT_RUNNING);
    }

    public ObjectProperty<ListenerState> stateProperty() {
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
    
    public void setNumberOfInterrupts(int num) {
        this.numberOfInterrupts.set(num);
    }

    public void incrementNumberOfInterrupts() {
        setNumberOfInterrupts(this.numberOfInterrupts.get() + 1);
    }

    public void setLatestInterruptTime(LocalTime latestInterruptTime) {
        this.latestInterruptTime.setValue(LocalTime.from(latestInterruptTime));
    }

    public void setSelected(Boolean selected) {
        this.selected.setValue(selected);
    }
    
    public void setState(ListenerState state) {
        this.state.setValue(state);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.clientPin);
        hash = 23 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InterruptValueObject other = (InterruptValueObject) obj;
        if (!Objects.equals(this.clientPin, other.clientPin)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }
}
