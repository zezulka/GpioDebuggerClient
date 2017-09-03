package gui.userdata;

import gui.userdata.AbstractXStreamListWrapper;
import java.io.File;
import java.util.ArrayList;

public class TestXStreamListWrapper extends AbstractXStreamListWrapper<String>{

    public TestXStreamListWrapper() {
        super(new ArrayList<>());
    }

    @Override
    public File getAssociatedFile() {
        return TestXmlFiles.MISSING;
    }

}
