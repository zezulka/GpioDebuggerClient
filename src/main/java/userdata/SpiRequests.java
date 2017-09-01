package userdata;

import java.util.List;

public final class SpiRequests
        extends XStreamListWrapper<SpiRequestValueObject> {

    public SpiRequests(List<SpiRequestValueObject> list) {
        super(list);
    }
}
