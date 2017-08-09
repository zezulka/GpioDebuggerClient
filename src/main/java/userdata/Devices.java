package userdata;
import java.util.List;
import javafx.collections.FXCollections;

public final class Devices {
    private final List<DeviceValueObject> devices;
    private boolean shallowDirty;

    public Devices(List<DeviceValueObject> requests) {
        this.devices = FXCollections.observableArrayList(requests);
        this.shallowDirty = false;
    }

    public List<DeviceValueObject> getRequests() {
        return devices;
    }

    public void addNewRequest(DeviceValueObject request) {
        this.shallowDirty = true;
        devices.add(request);
    }

    private boolean isDeepDirty() {
        return devices.stream()
                .map((device) -> device.isDirty())
                .reduce(false, (acc, curr) -> acc || curr);
    }

    /**
     * Tells whether the set of devices has been modified. Both set
     * modifications and entries modifications themselves are taken
     * into consideration.
     * @return true if dirty, false otherwise
     */
    public boolean isDirty() {
        return shallowDirty || isDeepDirty();
    }

    public boolean contains(DeviceValueObject request) {
        return devices.contains(request);
    }
}
