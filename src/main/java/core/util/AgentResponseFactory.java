package core.util;

import core.net.AgentConnectionValueObject;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import protocol.BoardType;
import protocol.response.AgentResponse;
import protocol.ClientPin;
import protocol.response.GpioAgentResponse;
import protocol.response.I2cAgentResponse;
import protocol.response.InterruptListenerAgentResponse;
import protocol.InterruptManager;
import protocol.InterruptType;
import protocol.InterruptValueObject;
import protocol.RaspiClientPin;
import protocol.Signal;
import protocol.response.InitAgentResponse;
import protocol.response.SpiAgentResponse;

/**
 *
 * @author Miloslav Zezulka
 */
public class AgentResponseFactory {

    public static AgentResponse of(AgentConnectionValueObject connection, String agentMessage) throws IllegalResponseException {
        if (agentMessage == null) {
            throw new IllegalResponseException();
        }
        String[] splitResponseBody = agentMessage.split(":", 4);
        if (splitResponseBody.length <= 1) {
            throw new IllegalResponseException();
        }
        for (int i = 0; i < splitResponseBody.length; i++) {
            splitResponseBody[i] = splitResponseBody[i].trim();
        }
        ResponseType type = null;
        try {
            type = ResponseType.valueOf(splitResponseBody[0]);
        } catch (IllegalArgumentException ex) {
            throw new IllegalResponseException();
        }
        switch (type) {
            case INIT:
                try {
                    BoardType boardType;
                    boardType = BoardType.parse(splitResponseBody[1]);
                    return new InitAgentResponse(connection, boardType);
                } catch (IllegalArgumentException e) {
                    throw new IllegalResponseException(e);
                }
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
                    InterruptValueObject interrupt = InterruptManager.getInterruptListener(connection.getDevice().getAddress(), new InterruptValueObject(interruptPin, intrType));
                    if (interrupt == null) {
                        throw new IllegalResponseException(
                                String.format("There is no such combination of address '%s' and interrupt listener [pin=%s, type=%s]", connection, interruptPin, intrType));
                    }
                    return new InterruptListenerAgentResponse(interrupt, type, timeGenerated, connection.getDevice().getAddress());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalResponseException(String.format("Interrupt listener response %s is not valid.", agentMessage));
                }
            default:
                throw new IllegalResponseException("Parser could not find appropriate response type.");
        }
    }

}
