package core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;
import protocol.*;

/**
 *
 * @author miloslav
 */
public class AgentResponseFactoryTest {

    private final ClientPin clientPin = RaspiClientPin.P1_3;
    private final InterruptType intrType = InterruptType.BOTH;
    private final String mockedNow = LocalTime.of(12, 0, 0).format(DateTimeFormatter.ISO_LOCAL_TIME);
    private InetAddress mockedAddress;

    @Before
    public void clean() {
        InterruptManager.clearAllInterruptListeners();
        try {
            mockedAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void testNull() {
        assertThatThrownBy(() -> AgentResponseFactory.of(mockedAddress, null)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void emptyString() {
        assertThatThrownBy(() -> AgentResponseFactory.of(mockedAddress, "")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void missingArgs() {
        assertThatThrownBy(() -> AgentResponseFactory.of(mockedAddress, "::")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void illegalInterface() {
        assertThatThrownBy(() -> AgentResponseFactory.of(mockedAddress, "INTERFACE_UNKNOWN:" + clientPin.getName() + ":HIGH")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void responseWithExtraSpaces() {
        try {
            assertThat(AgentResponseFactory.of(mockedAddress, "GPIO : " + clientPin.getName() + ": HIGH ")).isInstanceOf(AgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void happyScenarioGpioHighVoltage() {
        try {
            assertThat(AgentResponseFactory.of(mockedAddress, "GPIO:" + clientPin.getName() + ":HIGH")).isInstanceOf(GpioAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void happyScenarioGpioLowVoltage() {
        try {
            assertThat(AgentResponseFactory.of(mockedAddress, "GPIO:" + clientPin.getName() + ":LOW")).isInstanceOf(GpioAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void gpioInvalidVoltage() {
        assertThatThrownBy(() -> AgentResponseFactory.of(mockedAddress, "GPIO:" + clientPin.getName() + "MID")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void happyScenarioI2c() {
        try {
            assertThat(AgentResponseFactory.of(mockedAddress, "I2C: 0x68 0x78 0xFA")).isInstanceOf(I2cAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void i2cNoBytes() {
        assertThatThrownBy(() -> AgentResponseFactory.of(mockedAddress, "I2C:")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void happyScenarioSpi() {
        try {
            assertThat(AgentResponseFactory.of(mockedAddress, "SPI: 0x68 0x78 0xFA")).isInstanceOf(SpiAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void spiNoBytes() {
        assertThatThrownBy(() -> AgentResponseFactory.of(mockedAddress, "SPI:")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptGeneratedHappyScenario() {
        try {
            InterruptManager.addInterruptListener(mockedAddress, new InterruptValueObject(clientPin, intrType));
            assertThat(AgentResponseFactory.of(mockedAddress, "INTR_GENERATED:" + clientPin.getName()
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
            assertThat(AgentResponseFactory.of(mockedAddress, "INTR_STARTED:" + clientPin.getName()
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
            assertThat(AgentResponseFactory.of(mockedAddress, "INTR_STOPPED:" + clientPin.getName()
                    + ':' + intrType.toString() + ':'
                    + mockedNow)).isInstanceOf(InterruptListenerAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void interruptGeneratedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of(mockedAddress, "INTR_GENERATED:" + clientPin.getName()
                + ':' + intrType.toString() + ':'
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptStartedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of(mockedAddress, "INTR_STARTED:" + clientPin.getName()
                + ':' + intrType.toString() + ':'
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptStoppedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of(mockedAddress, "INTR_STOPPED:" + clientPin.getName()
                + ':' + intrType.toString() + ':'
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptInvalidType() {
        assertThatThrownBy(() -> AgentResponseFactory.of(mockedAddress, "INTR_STOPPED:" + clientPin.getName()
                + ":NONE:"
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptInvalidPin() {
        assertThatThrownBy(() -> AgentResponseFactory.of(mockedAddress, "INTR_STOPPED:PIN_THAT_REALLY_DOES_NOT_EXIST"
                + intrType.toString()
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }
}
