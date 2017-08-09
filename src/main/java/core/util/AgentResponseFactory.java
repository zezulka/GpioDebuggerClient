package core.util;

import core.net.ConnectionValueObject;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import protocol.BoardType;
import protocol.response.AgentResponse;
import protocol.ClientPin;
import protocol.response.GpioAgentResponse;
import protocol.response.I2cAgentResponse;
import protocol.response.InterruptAgentResponse;
import protocol.InterruptManager;
import protocol.InterruptType;
import protocol.InterruptValueObject;
import protocol.RaspiClientPin;
import protocol.Signal;
import protocol.response.InitAgentResponse;
import protocol.response.InterruptGeneratedAgentResponse;
import protocol.response.InterruptListenerStartedAgentResponse;
import protocol.response.InterruptListenerStoppedAgentResponse;
import protocol.response.SpiAgentResponse;

/**
 *
 * @author Miloslav Zezulka
 */
public final class AgentResponseFactory {

    private AgentResponseFactory() {
    }

    public static AgentResponse of(ConnectionValueObject connection,
            String agentMessage) throws IllegalResponseException {

        final int typeElemIndex = 0;
        List<String> splitResponse = getMessageElems(agentMessage);
        ResponseType type = null;
        try {
            type = ResponseType.valueOf(splitResponse.get(typeElemIndex));
        } catch (IllegalArgumentException ex) {
            throw new IllegalResponseException();
        }
        splitResponse.remove(typeElemIndex);
        switch (type) {
            case INIT:
                return init(connection, splitResponse);
            case GPIO:
                return gpio(splitResponse);
            case SPI:
                return spi(splitResponse);
            case I2C:
                return i2c(splitResponse);
            case INTR_GENERATED:
                return interrupt(connection, splitResponse,
                        InterruptGeneratedAgentResponse.class);
            case INTR_STARTED:
                return interrupt(connection, splitResponse,
                        InterruptListenerStartedAgentResponse.class);
            case INTR_STOPPED:
                return interrupt(connection, splitResponse,
                        InterruptListenerStoppedAgentResponse.class);
            default:
                throw new IllegalResponseException("Illegal response type.");
        }
    }

    private static List<String> getMessageElems(String agentMessage)
            throws IllegalResponseException {

        if (agentMessage == null) {
            throw new IllegalResponseException("null message");
        }
        String[] splitResponse = agentMessage.split(":");
        if (splitResponse.length == 0) {
            throw new IllegalResponseException("message contains nothing");
        }
        for (int i = 0; i < splitResponse.length; i++) {
            splitResponse[i] = splitResponse[i].trim();
        }
        return new ArrayList<>(Arrays.asList(splitResponse));
    }

    private static AgentResponse init(ConnectionValueObject connection,
            List<String> splitMessage) throws IllegalResponseException {
        final int expectedElemSize = 1;

        if (splitMessage.size() != expectedElemSize) {
            throw new IllegalResponseException("expected elems: "
                    + expectedElemSize);
        }
        try {
            BoardType boardType;
            boardType = BoardType.parse(splitMessage.get(0));
            return new InitAgentResponse(connection, boardType);
        } catch (IllegalArgumentException e) {
            throw new IllegalResponseException(e);
        }
    }

    private static AgentResponse gpio(List<String> splitMessage)
            throws IllegalResponseException {
        final int expectedElemSize = 2;

        if (splitMessage.size() != expectedElemSize) {
            throw new IllegalResponseException("expected elems: "
                    + expectedElemSize);
        }

        ClientPin pin;
        Signal signal;
        try {
            pin = RaspiClientPin.getPin(splitMessage.get(0));
            signal = Signal.valueOf(splitMessage.get(1));
            return new GpioAgentResponse(signal, pin);
        } catch (IllegalArgumentException ex) {
            throw new IllegalResponseException("Illegal GPIO response.");
        }
    }

    private static AgentResponse i2c(List<String> splitMessage)
            throws IllegalResponseException {

        final int expectedElemSize = 1;

        if (splitMessage.size() != expectedElemSize) {
            throw new IllegalResponseException("expected elems: "
                    + expectedElemSize);
        }

        if (splitMessage.size() != expectedElemSize
                || splitMessage.get(0).isEmpty()) {
            throw new IllegalResponseException("Illegal I2C response.");
        }
        return new I2cAgentResponse(splitMessage.get(0));
    }

    private static AgentResponse spi(List<String> splitMessage)
            throws IllegalResponseException {

        final int expectedElemSize = 1;

        if (splitMessage.size() != expectedElemSize) {
            throw new IllegalResponseException("expected elems: "
                    + expectedElemSize);
        }

        if (splitMessage.size() != expectedElemSize
                || splitMessage.get(0).isEmpty()) {
            throw new IllegalResponseException("Illegal SPI response.");
        }
        return new SpiAgentResponse(splitMessage.get(0));
    }

    private static AgentResponse interrupt(ConnectionValueObject connection,
            List<String> splitResponse,
            Class<? extends InterruptAgentResponse> clazz)
            throws IllegalResponseException {
        try {
            ClientPin interruptPin
                    = RaspiClientPin.getPin(splitResponse.get(0));
            InterruptType intrType
                    = InterruptType.getType(splitResponse.get(1));
            LocalTime timeGenerated
                    = LocalTime.parse(splitResponse.get(2),
                            MessageParser.FORMATTER);
            InterruptValueObject interrupt
                    = InterruptManager.getInterruptListener(
                            connection.getDevice().getAddress(),
                            new InterruptValueObject(interruptPin, intrType)
                    );
            if (interrupt == null) {
                throw new IllegalResponseException(
                        String.format("No such combination of address '%s' "
                                + "and interrupt listener [pin=%s, type=%s]",
                                connection, interruptPin, intrType));
            }
            return clazz
                    .getConstructor(InterruptValueObject.class, LocalTime.class,
                            InetAddress.class)
                    .newInstance(interrupt, timeGenerated,
                            connection.getDevice().getAddress());
        } catch (IllegalArgumentException ex) {
            throw new IllegalResponseException("Illegal interrupt response.");
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

}
