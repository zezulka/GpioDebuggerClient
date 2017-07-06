package core.util;

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

    @Before
    public void clean() {
        InterruptManager.clearAllInterruptListeners();
    }
    
    @Test
    public void testNull() {
        assertThatThrownBy(() -> AgentResponseFactory.of(null)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void emptyString() {
        assertThatThrownBy(() -> AgentResponseFactory.of("")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void missingArgs() {
        assertThatThrownBy(() -> AgentResponseFactory.of("::")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void illegalInterface() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTERFACE_UNKNOWN:" + clientPin.getName() + ":HIGH")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void responseWithExtraSpaces() {
        try {
            assertThat(AgentResponseFactory.of("GPIO : " + clientPin.getName() + ": HIGH ")).isInstanceOf(AgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void happyScenarioGpioHighVoltage() {
        try {
            assertThat(AgentResponseFactory.of("GPIO:" + clientPin.getName() + ":HIGH")).isInstanceOf(GpioAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void happyScenarioGpioLowVoltage() {
        try {
            assertThat(AgentResponseFactory.of("GPIO:" + clientPin.getName() + ":LOW")).isInstanceOf(GpioAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void gpioInvalidVoltage() {
        assertThatThrownBy(() -> AgentResponseFactory.of("GPIO:" + clientPin.getName() + "MID")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void happyScenarioI2c() {
        try {
            assertThat(AgentResponseFactory.of("I2C: 0x68 0x78 0xFA")).isInstanceOf(I2cAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void i2cNoBytes() {
        assertThatThrownBy(() -> AgentResponseFactory.of("I2C:")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void happyScenarioSpi() {
        try {
            assertThat(AgentResponseFactory.of("SPI: 0x68 0x78 0xFA")).isInstanceOf(SpiAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void spiNoBytes() {
        assertThatThrownBy(() -> AgentResponseFactory.of("SPI:")).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptGeneratedHappyScenario() {
        try {
            InterruptManager.addInterruptListener(new InterruptValueObject(clientPin, intrType));
            assertThat(AgentResponseFactory.of("INTR_GENERATED:" + clientPin.getName()
                    + ':' + intrType.toString() + ':'
                    + mockedNow)).isInstanceOf(InterruptListenerAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void interruptStartedHappyScenario() {
        try {
            InterruptManager.addInterruptListener(new InterruptValueObject(clientPin, intrType));
            assertThat(AgentResponseFactory.of("INTR_STARTED:" + clientPin.getName()
                    + ':' + intrType.toString() + ':'
                    + mockedNow)).isInstanceOf(InterruptListenerAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void interruptStoppedHappyScenario() {
        try {
            InterruptManager.addInterruptListener(new InterruptValueObject(clientPin, intrType));
            assertThat(AgentResponseFactory.of("INTR_STOPPED:" + clientPin.getName()
                    + ':' + intrType.toString() + ':'
                    + mockedNow)).isInstanceOf(InterruptListenerAgentResponse.class);
        } catch (IllegalResponseException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void interruptGeneratedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_GENERATED:" + clientPin.getName()
                + ':' + intrType.toString() + ':'
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptStartedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_STARTED:" + clientPin.getName()
                + ':' + intrType.toString() + ':'
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptStoppedMissing() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_STOPPED:" + clientPin.getName()
                + ':' + intrType.toString() + ':'
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptInvalidType() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_STOPPED:" + clientPin.getName()
                + ":NONE:"
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }

    @Test
    public void interruptInvalidPin() {
        assertThatThrownBy(() -> AgentResponseFactory.of("INTR_STOPPED:PIN_THAT_REALLY_DOES_NOT_EXIST"
                + intrType.toString()
                + mockedNow)).isInstanceOf(IllegalResponseException.class);
    }
}
