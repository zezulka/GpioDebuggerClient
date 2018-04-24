package protocol;

import gui.controllers.Utils;
import gui.userdata.InterruptValueObject;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.InetAddress;
import java.util.*;

public final class InterruptManager {

    private static final Map<InetAddress, ObservableList<InterruptValueObject>>
            INTERRUPTS = new HashMap<>();
    private static final IntegerProperty NUM_LISTENERS
            = new SimpleIntegerProperty(0);

    private InterruptManager() {
    }

    public static void clearAll() {
        INTERRUPTS.clear();
    }

    public static ObservableList<InterruptValueObject>
    getInterrupts(InetAddress address) {
        Objects.requireNonNull(address, "address cannot be null");
        ObservableList<InterruptValueObject> list = INTERRUPTS.get(address);
        if (list == null) {
            list = FXCollections.observableArrayList();
            INTERRUPTS.put(address, list);
        }
        return INTERRUPTS.get(address);
    }

    /**
     * @return null if no mapping to address/object combination exists,
     * InterruptValueObject instance otherwise
     */
    public static InterruptValueObject getInterrupt(InetAddress address,
                                                    ClientPin pin,
                                                    InterruptType type) {
        if (INTERRUPTS.get(address) == null) {
            return null;
        }
        for (InterruptValueObject obj : INTERRUPTS.get(address)) {
            if (obj.getClientPin().equals(pin) && obj.getType().equals(type)) {
                return obj;
            }
        }
        return null;
    }

    public static void update(InetAddress destination,
                              InterruptValueObject ivo) {
        for (InterruptValueObject curr : INTERRUPTS.get(destination)) {
            if ((ivo.getClientPin().equals(curr.getClientPin())
                    && curr.getType().equals(InterruptType.BOTH)
                    && curr.stateProperty().get().equals(ListenerState.RUNNING))
                    || curr.equals(ivo)) {
                curr.setLastIntrTime(ivo.lastIntrTimeProperty().get());
                curr.incrementIntrs();
            }
        }
    }

    public static void add(InetAddress destination, InterruptValueObject ivo) {
        INTERRUPTS.computeIfAbsent(destination,
                k -> FXCollections.observableArrayList());
        if (!INTERRUPTS.get(destination).contains(ivo)) {
            NUM_LISTENERS.set(NUM_LISTENERS.get() + 1);
            INTERRUPTS.get(destination).add(ivo);
        } else {
            Utils.showErrorDialog("Listener already exists.");
        }
    }

    public static void clearAllListeners(InetAddress inetAddress) {
        INTERRUPTS.remove(inetAddress);
    }
}
