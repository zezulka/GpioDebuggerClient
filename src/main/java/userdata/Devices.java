package userdata;

import java.util.ArrayList;
import java.util.List;

public class Devices {
    private final List<DeviceValueObject> devices;
    private boolean dirty;

    public Devices(List<DeviceValueObject> requests) {
        this.devices = new ArrayList<>(requests);
        this.dirty = false;
    }

    public List<DeviceValueObject> getRequests() {
        return devices;
    }

    public void addNewRequest(DeviceValueObject request) {
        this.dirty = true;
        devices.add(request);
    }
    /**
     * Tells whether the set of devices has been modified. Both set modifications and
     * entries modifications themselves are taken into consideration.
     * @return true if dirty, false otherwise
     */
    public boolean isDirty() {
        return dirty || devices.stream().map((device) -> device.isDirty()).reduce(false, (acc, curr) -> acc || curr);
    }
    
    public boolean contains(DeviceValueObject request) {
        return devices.contains(request);
    }
}
