package gui.userdata;

import java.net.InetAddress;
import java.net.UnknownHostException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class DevicesTest {

    private Devices devices;

    @Before
    public void testSomeMethod() {
        devices = new Devices();
    }

    @Test
    public void addTwoSame() {
        try {
            InetAddress address = InetAddress.getByAddress(new byte[]{77, 0, 0, 5});
            DeviceValueObject object = new DeviceValueObject(address);
            devices.addItem(object);
            assertThat(devices.getItems().size()).isEqualTo(1);
            devices.addItem(object);
            assertThat(devices.getItems().size()).isEqualTo(1);
        } catch (UnknownHostException ex) {
            fail();
        }
    }

    @Test
    public void addTwoDifferent() {
        try {
            InetAddress a = InetAddress.getByAddress(new byte[]{77, 0, 0, 5});
            InetAddress b = InetAddress.getByAddress(new byte[]{77, 0, 0, 10});
            DeviceValueObject aa = new DeviceValueObject(a);
            DeviceValueObject bb = new DeviceValueObject(b);
            devices.addItem(aa);
            assertThat(devices.getItems().size()).isEqualTo(1);
            devices.addItem(bb);
            assertThat(devices.getItems().size()).isEqualTo(2);
        } catch (UnknownHostException ex) {
            fail();
        }
    }
}
