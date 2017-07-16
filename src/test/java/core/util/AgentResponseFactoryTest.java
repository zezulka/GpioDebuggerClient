package core.util;

import core.net.AgentConnectionValueObject;
import protocol.response.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private final String mockedNow = LocalTime.of(12, 0, 0).format(DateTimeFormatter.ISO_LOCAL_TIME);
    private InetAddress mockedAddress;
    private AgentConnectionValueObject connection;

    @Before
    public void clean() {
        InterruptManager.clearAllInterruptListeners();
        try {
            mockedAddress = InetAddress.getLocalHost();
            connection = new AgentConnectionValueObject("", null, null, new DeviceValueObject(mockedAddress, BoardType.RASPBERRY_PI));
        } catch (UnknownHostException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void testNull() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, null)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void emptyString() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, "")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void missingArgs() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, "::")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void illegalInterface() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, "INTERFACE_UNKNOWN:" + clientPin.getPinId() + ":HIGH")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void responseWithExtraSpaces() {
        try {
            assertThat(AgentResponseFactory.of(connection, "GPIO : " + clientPin.getPinId() + ": HIGH ")).isInstanceOf(AgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void happyScenarioInitResponse() {
        try {
            assertThat(AgentResponseFactory.of(connection, "INIT:" + connection.getDevice().getBoardType().toString())).isInstanceOf(InitAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void happyScenarioGpioHighVoltage() {
        try {
            assertThat(AgentResponseFactory.of(connection, "GPIO:" + clientPin.getPinId() + ":HIGH")).isInstanceOf(GpioAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void happyScenarioGpioLowVoltage() {
        try {
            assertThat(AgentResponseFactory.of(connection, "GPIO:" + clientPin.getPinId() + ":LOW")).isInstanceOf(GpioAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void initIlegalBoardType() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, "INIT:DEVICE_THAT_DOES_NOT_EXIST")).isInstanceOf(IllegalResponseException.class);
    }
    
    @Test
    public void missingBoardType() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, "INIT:")).isInstanceOf(IllegalResponseException.class);
    }
    
    @Test
    public void gpioInvalidVoltage() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, "GPIO:" + clientPin.getPinId() + "MID")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void happyScenarioI2c() {
        try {
            assertThat(AgentResponseFactory.of(connection, "I2C: 0x68 0x78 0xFA")).isInstanceOf(I2cAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void i2cNoBytes() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, "I2C:")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void happyScenarioSpi() {
        try {
            assertThat(AgentResponseFactory.of(connection, "SPI: 0x68 0x78 0xFA")).isInstanceOf(SpiAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void spiNoBytes() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, "SPI:")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptGeneratedHappyScenario() {
        try {
            InterruptManager.addInterruptListener(mockedAddress, new InterruptValueObject(clientPin, intrType));
            assertThat(AgentResponseFactory.of(connection, "INTR_GENERATED:" + clientPin.getPinId()
                    + ':' + intrType.toString() + ':'
                    + mockedNow)).isInstanceOf(InterruptListenerAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void interruptStartedHappyScenario() {
        try {
            InterruptManager.addInterruptListener(mockedAddress, new InterruptValueObject(clientPin, intrType));
            assertThat(AgentResponseFactory.of(connection, "INTR_STARTED:" + clientPin.getPinId()
                    + ':' + intrType.toString() + ':'
                    + mockedNow)).isInstanceOf(InterruptListenerAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void interruptStoppedHappyScenario() {
        try {
            InterruptValueObject object = new InterruptValueObject(clientPin, intrType);
            InterruptManager.addInterruptListener(mockedAddress, object);
            assertThat(AgentResponseFactory.of(connection, "INTR_STOPPED:" + clientPin.getPinId()
                    + ':' + intrType.toString() + ':'
                    + mockedNow)).isInstanceOf(InterruptListenerAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void interruptGeneratedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, "INTR_GENERATED:" + clientPin.getPinId()
                + ':' + intrType.toString() + ':'
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptStartedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, "INTR_STARTED:" + clientPin.getPinId()
                + ':' + intrType.toString() + ':'
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptStoppedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, "INTR_STOPPED:" + clientPin.getPinId()
                + ':' + intrType.toString() + ':'
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptInvalidType() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, "INTR_STOPPED:" + clientPin.getPinId()
                + ":NONE:"
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptInvalidPin() {
        assertThatThrownBy(() -> AgentResponseFactory.of(connection, "INTR_STOPPED:PIN_THAT_REALLY_DOES_NOT_EXIST"
                + intrType.toString()
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }
}
