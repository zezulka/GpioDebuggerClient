package gui.userdata;


import gui.userdata.xstream.TestXmlFiles;
import gui.userdata.xstream.AbstractXStreamListWrapper;
import java.io.File;
import java.util.List;

public class MockedDevices
        extends AbstractXStreamListWrapper<DeviceValueObject> {

    public MockedDevices(List<DeviceValueObject> list) {
        super(list);
    }

    @Override
    public File getAssociatedFile() {
        return TestXmlFiles.ONE_DEVICE;
    }

}
