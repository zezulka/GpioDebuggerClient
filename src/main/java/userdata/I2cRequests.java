package userdata;

import java.util.List;

public final class I2cRequests
        extends XStreamListWrapper<I2cRequestValueObject> {

    public I2cRequests(List<I2cRequestValueObject> list) {
        super(list);
    }
}
