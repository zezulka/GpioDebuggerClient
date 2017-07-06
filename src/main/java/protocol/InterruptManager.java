package protocol;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InterruptManager {
    private static final ObservableList<InterruptValueObject> INTERRUPTS = FXCollections.observableArrayList();
    public static final int MAX_INTR_LISTENER_THRESHOLD = 4;
    private static final IntegerProperty numListeners = new SimpleIntegerProperty(0);
    
    public static void clearAllInterruptListeners() {
        INTERRUPTS.clear();
    }
    
    public static ObservableList<InterruptValueObject> getListeners() {
        return INTERRUPTS;
    }
    
    public static IntegerProperty getNumListeners() {
        return numListeners;
    }
    
    public static InterruptValueObject getInterruptListenerFromValues(ClientPin pin, InterruptType type) {
        InterruptValueObject mockObj = new InterruptValueObject(pin, type);
        for(InterruptValueObject obj : INTERRUPTS) {
            if(mockObj.equals(obj)) {
                return obj;
            }
        }
        throw new IllegalArgumentException("no suitable interruptvalueobject found");
    }

    public static void updateInterruptListener(InterruptValueObject ivo) {
        for (int i = 0; i < INTERRUPTS.size(); i++) {
            InterruptValueObject curr = INTERRUPTS.get(i);
            if (curr.equals(ivo)) {
                ivo.setNumberOfInterrupts(curr.numberOfInterruptsProperty().get() + 1);
                INTERRUPTS.set(i, ivo);
                return;
            }
        }
        throw new IllegalArgumentException("illegal object");
    }
    
    public static boolean addInterruptListener(InterruptValueObject ivo) {
        if(!INTERRUPTS.contains(ivo) && INTERRUPTS.size() <= MAX_INTR_LISTENER_THRESHOLD) {
            numListeners.set(numListeners.get() + 1);
            return INTERRUPTS.add(ivo);
        }
        return false;
    }
    
    public static boolean removeInterruptListener(InterruptValueObject ivo) {
        for(InterruptValueObject interrupt : INTERRUPTS) {
            if(interrupt.equals(ivo)) {
                numListeners.set(numListeners.get() - 1);
                return INTERRUPTS.remove(interrupt);
            }
        }
        throw new IllegalArgumentException("supplied interruptvalueobject has not been found");
    }
}
