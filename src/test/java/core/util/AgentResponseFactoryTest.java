package core.util;

import gui.misc.Feature;
import gui.userdata.DeviceValueObject;
import gui.userdata.InterruptValueObject;
import net.ConnectionValueObject;
import org.junit.Before;
import org.junit.Test;
import protocol.*;
import protocol.response.*;
import protocol.response.util.AgentResponseFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.*;

public class AgentResponseFactoryTest {

    private final ClientPin clientPin;
    private final InterruptType intrType;
    private final String mockedNow;
    private InetAddress mockedAddress;
    private DeviceValueObject device;

    public AgentResponseFactoryTest() {
        clientPin = RaspiClientPin.P1_3;
        intrType = InterruptType.BOTH;
        mockedNow = LocalDateTime.of(2000, Month.JANUARY, 1, 12, 0).format(MessageParser.FORMATTER);
    }

    @Before
    public void clean() {
        InterruptManager.clearAll();
        try {
            mockedAddress = InetAddress.getLocalHost();
            device = new DeviceValueObject(mockedAddress, BoardType.RASPBERRY_PI);
        } catch (UnknownHostException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testNull() {
        assertThatThrownBy(() -> AgentResponseFactory.of(null, device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void emptyString() {
        assertThatThrownBy(() -> AgentResponseFactory.of("", device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void missingArgs() {
        assertThatThrownBy(() -> AgentResponseFactory.of("::", device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void illegalInterface() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTERFACE_UNKNOWN:" + clientPin.getPinId() + ":HIGH", device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void responseWithExtraSpaces() {
        try {
            assertThat(AgentResponseFactory.of("GPIO : " + clientPin.getPinId() + ": HIGH ", device)).isInstanceOf(AgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void happyScenarioInitResponse() {
        try {
            assertThat(AgentResponseFactory.of("INIT:" + device.getBoardType().toString()+ ':' + Feature.GPIO, device)).isInstanceOf(InitAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void happyScenarioGpioHighVoltage() {
        try {
            assertThat(AgentResponseFactory.of("GPIO:" + clientPin.getPinId() + ":HIGH", device)).isInstanceOf(GpioAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void happyScenarioGpioLowVoltage() {
        try {
            assertThat(AgentResponseFactory.of("GPIO:" + clientPin.getPinId() + ":LOW", device)).isInstanceOf(GpioAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void initIlegalBoardType() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INIT:DEVICE_THAT_DOES_NOT_EXIST", device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void missingBoardType() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INIT:", device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void invalidFeatures() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INIT:" + device.getBoardType().toString() + ":HELLO WORLD" , device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void gpioInvalidVoltage() {
        assertThatThrownBy(() -> AgentResponseFactory.of("GPIO:" + clientPin.getPinId() + ":MID", device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void gpioMissingVoltage() {
        assertThatThrownBy(() -> AgentResponseFactory.of("GPIO:" + clientPin.getPinId(), device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void happyScenarioI2c() {
        try {
            assertThat(AgentResponseFactory.of("I2C: 0x68 0x78 0xFA", device)).isInstanceOf(I2cAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void i2cNoBytes() {
        assertThatThrownBy(() -> AgentResponseFactory.of("I2C:", device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void happyScenarioSpi() {
        try {
            assertThat(AgentResponseFactory.of("SPI: 0x68 0x78 0xFA", device)).isInstanceOf(SpiAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void spiNoBytes() {
        assertThatThrownBy(() -> AgentResponseFactory.of("SPI:", device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptGeneratedHappyScenario() {
        try {
            InterruptManager.add(mockedAddress, new InterruptValueObject(clientPin, intrType));
            assertThat(AgentResponseFactory.of("INTR_GENERATED:" + clientPin.getPinId()
                    + ':' + intrType.toString() + ':'
                    + mockedNow, device)).isInstanceOf(AbstractInterruptAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void interruptStartedHappyScenario() {
        try {
            InterruptManager.add(mockedAddress, new InterruptValueObject(clientPin, intrType));
            assertThat(AgentResponseFactory.of("INTR_STARTED:" + clientPin.getPinId()
                    + ':' + intrType.toString() + ':'
                    + mockedNow, device)).isInstanceOf(AbstractInterruptAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void interruptStoppedHappyScenario() {
        try {
            InterruptValueObject object = new InterruptValueObject(clientPin, intrType);
            InterruptManager.add(mockedAddress, object);
            assertThat(AgentResponseFactory.of("INTR_STOPPED:" + clientPin.getPinId()
                    + ':' + intrType.toString() + ':'
                    + mockedNow, device)).isInstanceOf(AbstractInterruptAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void interruptGeneratedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_GENERATED:" + clientPin.getPinId()
                + ':' + intrType.toString() + ':'
                + mockedNow, device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptStartedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_STARTED:" + clientPin.getPinId()
                + ':' + intrType.toString() + ':'
                + mockedNow, device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptStoppedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_STOPPED:" + clientPin.getPinId()
                + ':' + intrType.toString() + ':'
                + mockedNow, device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptInvalidType() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_STOPPED:" + clientPin.getPinId()
                + ":NONE:"
                + mockedNow, device)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptInvalidPin() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_STOPPED:PIN_THAT_REALLY_DOES_NOT_EXIST"
                + intrType.toString()
                + mockedNow, device)).isInstanceOf(IllegalResponseException.class);
    }
}
