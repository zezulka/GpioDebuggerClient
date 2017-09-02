package userdata;

import java.io.File;
import java.util.List;

public class MockedDevices extends AbstractXStreamListWrapper<DeviceValueObject> {

    public MockedDevices(List<DeviceValueObject> list) {
        super(list);
    }

    @Override
    public File getAssociatedFile() {
        return TestXmlFiles.ONE_DEVICE;
    }

}
