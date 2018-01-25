package core.util;

import gui.userdata.InterruptValueObject;
import protocol.BoardType;
import protocol.ClientPin;
import protocol.InterruptManager;
import protocol.InterruptType;
import protocol.MessageParser;
import protocol.RaspiClientPin;
import protocol.response.AbstractInterruptAgentResponse;
import protocol.response.AgentResponse;
import protocol.response.GpioAgentResponse;
import protocol.response.I2cAgentResponse;
import protocol.response.IllegalResponseException;
import protocol.response.InitAgentResponse;
import protocol.response.SpiAgentResponse;
import protocol.response.util.AgentResponseFactory;
import gui.feature.Feature;
import net.ConnectionValueObject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.Month;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;
import gui.userdata.DeviceValueObject;

public class AgentResponseFactoryTest {

    private final ClientPin clientPin;
    private final InterruptType intrType;
    private final String mockedNow;
    private InetAddress mockedAddress;
    private ConnectionValueObject connection;

    public AgentResponseFactoryTest() {
        clientPin = RaspiClientPin.P1_3;
        intrType = InterruptType.BOTH;
        mockedNow = LocalDateTime.of(2000, Month.JANUARY, 1, 12, 0).format(MessageParser.FORMATTER);
    }

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
            assertThat(AgentResponseFactory.of("INIT:" + connection.getDevice().getBoardType().toString()+ ':' + Feature.GPIO, connection)).isInstanceOf(InitAgentResponse.class);
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
    public void invalidFeatures() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INIT:" + connection.getDevice().getBoardType().toString() + ":HELLO WORLD" , connection)).isInstanceOf(IllegalResponseException.class);
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
                    + mockedNow, connection)).isInstanceOf(AbstractInterruptAgentResponse.class);
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
                    + mockedNow, connection)).isInstanceOf(AbstractInterruptAgentResponse.class);
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
                    + mockedNow, connection)).isInstanceOf(AbstractInterruptAgentResponse.class);
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
