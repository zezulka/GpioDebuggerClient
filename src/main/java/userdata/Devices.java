package userdata;
import java.util.List;

public final class Devices extends XStreamListWrapper<DeviceValueObject>  {

    public Devices(List<DeviceValueObject> list) {
        super(list);
    }
}
