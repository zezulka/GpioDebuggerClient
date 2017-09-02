package userdata;

import java.io.File;
import java.util.List;

public final class SpiRequests
        extends AbstractXStreamListWrapper<SpiRequestValueObject> {

    public SpiRequests(List<SpiRequestValueObject> list) {
        super(list);
    }

    @Override
    public File getAssociatedFile() {
       return  XmlUserdata.SPI_FILE;
    }
}
