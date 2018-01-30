package gui.userdata.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import gui.misc.Operation;
import gui.userdata.DeviceValueObject;
import gui.userdata.Devices;
import gui.userdata.I2cRequestValueObject;
import gui.userdata.I2cRequests;
import gui.userdata.SpiRequestValueObject;
import gui.userdata.SpiRequests;

import java.net.InetAddress;
import java.time.LocalDateTime;

/*
 * Utility class dealing with XStream library. Initializes XStream singleton
 * to have all the appropriate aliases and other things required by client.
 */
final class XStreamWrapper {
    private static final XStream INSTANCE = new XStream(new DomDriver());

    static {
        INSTANCE.alias("operation", Operation.class);
        INSTANCE.alias("address", InetAddress.class);
        INSTANCE.alias("lastTimeConnected", LocalDateTime.class);

        INSTANCE.alias("i2cRequest", I2cRequestValueObject.class);
        INSTANCE.alias("spiRequest", SpiRequestValueObject.class);
        INSTANCE.alias("device", DeviceValueObject.class);

        INSTANCE.alias("i2cRequests", I2cRequests.class);
        INSTANCE.alias("spiRequests", SpiRequests.class);
        INSTANCE.alias("devices", Devices.class);

        INSTANCE.omitField(AbstractXStreamListWrapper.class, "dirty");
        INSTANCE.omitField(DeviceValueObject.class, "dirty");
        INSTANCE.omitField(DeviceValueObject.class, "disconnected");

        INSTANCE.addImplicitCollection(I2cRequests.class, "list",
                I2cRequestValueObject.class);
        INSTANCE.addImplicitCollection(SpiRequests.class, "list",
                SpiRequestValueObject.class);
        INSTANCE.addImplicitCollection(Devices.class, "list",
                DeviceValueObject.class);
    }

    private XStreamWrapper() {
    }

    public static XStream getInstance() {
        return INSTANCE;
    }
}
