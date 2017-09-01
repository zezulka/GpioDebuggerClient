package core.util;

import gui.AgentUserPrivileges;
import core.net.ConnectionValueObject;
import protocol.response.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.Month;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;
import protocol.*;
import userdata.DeviceValueObject;

/**
 *
 * @author miloslav
 */
public class AgentResponseFactoryTest {

    private final ClientPin clientPin = RaspiClientPin.P1_3;
    private final InterruptType intrType = InterruptType.BOTH;
    private final String mockedNow
            = LocalDateTime.of(2000, Month.JANUARY, 1, 12, 0).format(MessageParser.FORMATTER);
    private InetAddress mockedAddress;
    private ConnectionValueObject connection;

    @Before
    public void clean() {
        InterruptManager.clearAllInterruptListeners();
        try {
            mockedAddress = InetAddress.getLocalHost();
            connection = new ConnectionValueObject("", null, null, new DeviceValueObject(mockedAddress, BoardType.RASPBERRY_PI));
        } catch (UnknownHostException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testNull() {
        assertThatThrownBy(() -> AgentResponseFactory.of(null, connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void emptyString() {
        assertThatThrownBy(() -> AgentResponseFactory.of("", connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void missingArgs() {
        assertThatThrownBy(() -> AgentResponseFactory.of("::", connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void illegalInterface() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTERFACE_UNKNOWN:" + clientPin.getPinId() + ":HIGH", connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void responseWithExtraSpaces() {
        try {
            assertThat(AgentResponseFactory.of("GPIO : " + clientPin.getPinId() + ": HIGH ", connection)).isInstanceOf(AgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void happyScenarioInitResponse() {
        try {
            assertThat(AgentResponseFactory.of("INIT:" + connection.getDevice().getBoardType().toString()+ ':' + AgentUserPrivileges.ROOT_USER, connection)).isInstanceOf(InitAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void happyScenarioGpioHighVoltage() {
        try {
            assertThat(AgentResponseFactory.of("GPIO:" + clientPin.getPinId() + ":HIGH", connection)).isInstanceOf(GpioAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void happyScenarioGpioLowVoltage() {
        try {
            assertThat(AgentResponseFactory.of("GPIO:" + clientPin.getPinId() + ":LOW", connection)).isInstanceOf(GpioAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void initIlegalBoardType() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INIT:DEVICE_THAT_DOES_NOT_EXIST", connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void missingBoardType() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INIT:", connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void invalidAgentUserPrivileges() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INIT:" + connection.getDevice().getBoardType().toString() + ":MISSING" , connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void gpioInvalidVoltage() {
        assertThatThrownBy(() -> AgentResponseFactory.of("GPIO:" + clientPin.getPinId() + ":MID", connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void gpioMissingVoltage() {
        assertThatThrownBy(() -> AgentResponseFactory.of("GPIO:" + clientPin.getPinId(), connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void happyScenarioI2c() {
        try {
            assertThat(AgentResponseFactory.of("I2C: 0x68 0x78 0xFA", connection)).isInstanceOf(I2cAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void i2cNoBytes() {
        assertThatThrownBy(() -> AgentResponseFactory.of("I2C:", connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void happyScenarioSpi() {
        try {
            assertThat(AgentResponseFactory.of("SPI: 0x68 0x78 0xFA", connection)).isInstanceOf(SpiAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void spiNoBytes() {
        assertThatThrownBy(() -> AgentResponseFactory.of("SPI:", connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptGeneratedHappyScenario() {
        try {
            InterruptManager.addInterruptListener(mockedAddress, new InterruptValueObject(clientPin, intrType));
            assertThat(AgentResponseFactory.of("INTR_GENERATED:" + clientPin.getPinId()
                    + ':' + intrType.toString() + ':'
                    + mockedNow, connection)).isInstanceOf(InterruptAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void interruptStartedHappyScenario() {
        try {
            InterruptManager.addInterruptListener(mockedAddress, new InterruptValueObject(clientPin, intrType));
            assertThat(AgentResponseFactory.of("INTR_STARTED:" + clientPin.getPinId()
                    + ':' + intrType.toString() + ':'
                    + mockedNow, connection)).isInstanceOf(InterruptAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void interruptStoppedHappyScenario() {
        try {
            InterruptValueObject object = new InterruptValueObject(clientPin, intrType);
            InterruptManager.addInterruptListener(mockedAddress, object);
            assertThat(AgentResponseFactory.of("INTR_STOPPED:" + clientPin.getPinId()
                    + ':' + intrType.toString() + ':'
                    + mockedNow, connection)).isInstanceOf(InterruptAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void interruptGeneratedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_GENERATED:" + clientPin.getPinId()
                + ':' + intrType.toString() + ':'
                + mockedNow, connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptStartedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_STARTED:" + clientPin.getPinId()
                + ':' + intrType.toString() + ':'
                + mockedNow, connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptStoppedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_STOPPED:" + clientPin.getPinId()
                + ':' + intrType.toString() + ':'
                + mockedNow, connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptInvalidType() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_STOPPED:" + clientPin.getPinId()
                + ":NONE:"
                + mockedNow, connection)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptInvalidPin() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_STOPPED:PIN_THAT_REALLY_DOES_NOT_EXIST"
                + intrType.toString()
                + mockedNow, connection)).isInstanceOf(IllegalResponseException.class);
    }
}
