package protocol;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InterruptManager {

    private static final Map<InetAddress, ObservableList<InterruptValueObject>> INTERRUPTS = new HashMap<>();
    public static final int MAX_INTR_LISTENER_THRESHOLD = 4;
    private static final IntegerProperty NUM_LISTENERS = new SimpleIntegerProperty(0);

    public static void clearAllInterruptListeners() {
        INTERRUPTS.clear();
    }

    public static ObservableList<InterruptValueObject> getListeners(InetAddress address) {
        if(address == null) {
            throw new IllegalArgumentException("address cannot be null");
        }
        ObservableList<InterruptValueObject> list = INTERRUPTS.get(address);
        if(list == null) {
            list = FXCollections.observableArrayList();
            INTERRUPTS.put(address, list);
        }
        return INTERRUPTS.get(address);
    }

    public static IntegerProperty getNumListeners() {
        return NUM_LISTENERS;
    }

    /**
     * 
     * @param address
     * @param object
     * @return null if no mapping to address/object combination exists, InterruptValueObject instance otherwise
     */
    public static InterruptValueObject getInterruptListener(InetAddress address, InterruptValueObject object) {
        if(INTERRUPTS.get(address) == null) {
            return null;
        }
        for (InterruptValueObject obj : INTERRUPTS.get(address)) {
            if (object.equals(obj)) {
                return obj;
            }
        }
        return null;
    }

    public static void updateInterruptListener(InetAddress destination, InterruptValueObject ivo) {
        List<InterruptValueObject> interrupts = INTERRUPTS.get(destination);
        for (int i = 0; i < interrupts.size(); i++) {
            InterruptValueObject curr = interrupts.get(i);
            if (curr.equals(ivo)) {
                interrupts.set(i, ivo);
                return;
            }
        }
        throw new IllegalArgumentException("illegal object");
    }

    public static boolean addInterruptListener(InetAddress destination, InterruptValueObject ivo) {
        if (INTERRUPTS.get(destination) == null) {
            INTERRUPTS.put(destination, FXCollections.observableArrayList());
        }
        if (!INTERRUPTS.get(destination).contains(ivo) && INTERRUPTS.size() <= MAX_INTR_LISTENER_THRESHOLD) {
            NUM_LISTENERS.set(NUM_LISTENERS.get() + 1);

            return INTERRUPTS.get(destination).add(ivo);
        }
        return false;
    }

    public static boolean removeInterruptListener(InetAddress destination, InterruptValueObject ivo) {
        for (InterruptValueObject interrupt : INTERRUPTS.get(destination)) {
            if (interrupt.equals(ivo)) {
                NUM_LISTENERS.set(NUM_LISTENERS.get() - 1);
                return INTERRUPTS.get(destination).remove(interrupt);
            }
        }
        throw new IllegalArgumentException("supplied interruptvalueobject has not been found");
    }

    public static void clearAllListeners(InetAddress inetAddress) {
        INTERRUPTS.remove(inetAddress);
    }
}
