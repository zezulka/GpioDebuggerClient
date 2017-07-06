package core.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import protocol.AgentResponse;
import protocol.ClientPin;
import protocol.GpioAgentResponse;
import protocol.I2cAgentResponse;
import protocol.InterruptListenerAgentResponse;
import protocol.InterruptManager;
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
        if(agentMessage == null) {
            throw new IllegalResponseException();
        }
        String[] splitResponseBody = agentMessage.split(":",4);
        if(splitResponseBody.length <= 1) {
            throw new IllegalResponseException();
        }
        for(int i = 0; i < splitResponseBody.length; i++) {
            splitResponseBody[i] = splitResponseBody[i].trim();
        }
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
                if (splitResponseBody.length != 2 || splitResponseBody[1].equals("")) {
                    throw new IllegalResponseException(String.format("SPI:[((HEX_BYTE)(' ' + HEX_BYTE)*)|<WRITE_REQUEST_OK>], supplied: %s", agentMessage));
                }
                return new SpiAgentResponse(splitResponseBody[1]);
            case I2C:
                if (splitResponseBody.length != 2 || splitResponseBody[1].equals("")) {
                    throw new IllegalResponseException(String.format("I2C:(HEX_BYTE + (HEX_BYTE + ' ')*), supplied: %s", agentMessage));
                }
                return new I2cAgentResponse(splitResponseBody[1]);
            case INTR_GENERATED:
            case INTR_STARTED:
            case INTR_STOPPED:
                try {
                    ClientPin interruptPin = RaspiClientPin.getPin(splitResponseBody[1]);
                    InterruptType intrType = InterruptType.getType(splitResponseBody[2]);
                    LocalTime timeGenerated = LocalTime.parse(splitResponseBody[3], DateTimeFormatter.ISO_LOCAL_TIME);
                    return new InterruptListenerAgentResponse(InterruptManager.getInterruptListenerFromValues(interruptPin, intrType), type, timeGenerated);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalResponseException(String.format("Interrupt listener response %s is not valid.", agentMessage));
                }
            default:
                throw new IllegalResponseException("Parser could not find appropriate response type.");
        }
    }

}
