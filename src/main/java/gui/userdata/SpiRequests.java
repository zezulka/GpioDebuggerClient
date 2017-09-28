package gui.userdata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class SpiRequests
        extends AbstractXStreamListWrapper<SpiRequestValueObject> {

    public SpiRequests(List<SpiRequestValueObject> list) {
        super(list);
    }

    // empty constructor is declared only for convenience in test methods
    SpiRequests() {
        super(new ArrayList<>());
    }


    @Override
    public File getAssociatedFile() {
       return XmlUserdata.SPI_FILE;
    }
}
