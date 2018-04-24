package gui.userdata;

import javafx.beans.property.*;
import protocol.ClientPin;
import protocol.InterruptType;
import protocol.ListenerState;

import java.time.LocalTime;
import java.util.Objects;

public final class InterruptValueObject {

    private final ClientPin clientPin;
    private final InterruptType type;
    private final LocalTime timeAdded;
    private final IntegerProperty numOfIntrs;
    private final ObjectProperty<ListenerState> state;
    private final ObjectProperty<LocalTime> lastIntrTime;
    private final BooleanProperty destroy;

    public InterruptValueObject(ClientPin clientPin, InterruptType type) {
        this.clientPin = clientPin;
        this.type = type;
        this.timeAdded = LocalTime.now();
        this.numOfIntrs = new SimpleIntegerProperty(0);
        this.lastIntrTime = new SimpleObjectProperty<>();
        this.state = new SimpleObjectProperty<>(ListenerState.NOT_RUNNING);
        this.destroy = new SimpleBooleanProperty(false);
    }

    public ObjectProperty<ListenerState> stateProperty() {
        return this.state;
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

    public IntegerProperty numOfIntrsProperty() {
        return numOfIntrs;
    }

    public BooleanProperty destroyProperty() {
        return destroy;
    }

    public ObjectProperty<LocalTime> lastIntrTimeProperty() {
        return lastIntrTime;
    }

    public void setNumOfIntrs(int num) {
        this.numOfIntrs.set(num);
    }

    public void incrementIntrs() {
        setNumOfIntrs(this.numOfIntrs.get() + 1);
    }

    public void setLastIntrTime(LocalTime latestInterruptTime) {
        this.lastIntrTime.setValue(LocalTime.from(latestInterruptTime));
    }

    public void setState(ListenerState state) {
        this.state.setValue(state);
    }

    public void destroy(){
        this.destroy.setValue(true);
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
        return Objects.equals(this.clientPin, other.clientPin)
                && this.type.equals(other.type);
    }
}
