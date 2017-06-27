package core.util;

import java.time.LocalTime;
import protocol.AgentResponse;
import protocol.ClientPin;
import protocol.GpioAgentResponse;
import protocol.I2cAgentResponse;
import protocol.InterruptListenerAgentResponse;
import protocol.InterruptType;
import protocol.RaspiClientPin;
import protocol.Signal;
import protocol.SpiAgentResponse;

/**
 *
 * @author Miloslav Zezulka
 */
public class AgentResponseFactory {

    public static AgentResponse of(String agentMessage) throws IllegalResponseException {
        String[] splitResponseBody = agentMessage.split(":");
        ResponseType type = null;
        try {
            type = ResponseType.valueOf(splitResponseBody[0]);
        } catch (IllegalArgumentException ex) {
            throw new IllegalResponseException();
        }

        switch (type) {
            case GPIO:
                ClientPin pin;
                Signal signal;
                try {
                    pin = RaspiClientPin.getPin(splitResponseBody[1]);
                    signal = Signal.valueOf(splitResponseBody[2]);
                    return new GpioAgentResponse(signal, pin);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalResponseException(String.format("GPIO response %s is not valid.", agentMessage));
                }
            case SPI:
                if (splitResponseBody.length > 2) {
                    throw new IllegalResponseException(String.format("SPI:[(HEX_BYTE + ' ')*|<WRITE_REQUEST_OK>], supplied: %s", agentMessage));
                }
                return new SpiAgentResponse(splitResponseBody[1]);
            case I2C:
                if (splitResponseBody.length > 2) {
                    throw new IllegalResponseException(String.format("I2C:(HEX_BYTE + ' ')*, supplied: %s", agentMessage));
                }
                return new I2cAgentResponse(splitResponseBody[1]);
            case INTR_GENERATED:
            case INTR_STARTED:
            case INTR_STOPPED:
                try {
                    ClientPin interruptPin = RaspiClientPin.getPin(splitResponseBody[1]);
                    InterruptType intrType = InterruptType.getType(splitResponseBody[2]);
                    LocalTime timeGenerated = LocalTime.ofNanoOfDay(Long.parseLong(splitResponseBody[3]));
                    return new InterruptListenerAgentResponse(type, timeGenerated, interruptPin, intrType);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalResponseException(String.format("GPIO response %s is not valid.", agentMessage));
                }
                //[INTR_STOPPED | INTR_STARTED | INTR_GENERATED]:<PIN_NAME>:<INTERRUPT_TYPE>:<TIME>
            default:
                throw new IllegalResponseException("Parser could not find appropriate response type.");
        }
    }

}
