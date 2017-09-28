package gui.userdata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Devices
        extends AbstractXStreamListWrapper<DeviceValueObject> {

    public Devices(List<DeviceValueObject> list) {
        super(list);
    }

    // empty constructor is declared only for convenience in test methods
    Devices() {
        super(new ArrayList<>());
    }

    @Override
    public File getAssociatedFile() {
        return XmlUserdata.DEVICES_FILE;
    }
}
