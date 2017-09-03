package gui.userdata;

import java.io.File;
import java.util.List;

public final class Devices
        extends AbstractXStreamListWrapper<DeviceValueObject> {

    public Devices(List<DeviceValueObject> list) {
        super(list);
    }

    @Override
    public File getAssociatedFile() {
        return XmlUserdata.DEVICES_FILE;
    }
}
