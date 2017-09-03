package gui.userdata;

import java.io.File;
import java.util.List;

public final class I2cRequests
        extends AbstractXStreamListWrapper<I2cRequestValueObject> {

    public I2cRequests(List<I2cRequestValueObject> list) {
        super(list);
    }

    @Override
    public File getAssociatedFile() {
        return XmlUserdata.I2C_FILE;
    }
}
