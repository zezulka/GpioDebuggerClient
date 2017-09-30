package core.util;

import gui.feature.Feature;
import core.net.ConnectionValueObject;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import protocol.BoardType;
import protocol.response.AgentResponse;
import protocol.ClientPin;
import protocol.ClientPinFactory;
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

public final class AgentResponseFactory {
    
    private AgentResponseFactory() {
    }

    public static AgentResponse of(String agentMessage,
            ConnectionValueObject connection) throws IllegalResponseException {

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
                return init(splitResponse, connection);
            case GPIO:
                return gpio(splitResponse, connection);
            case SPI:
                return spi(splitResponse, connection);
            case I2C:
                return i2c(splitResponse, connection);
            case INTR_GENERATED:
                return interrupt(splitResponse, connection,
                        InterruptGeneratedAgentResponse.class);
            case INTR_STARTED:
                return interrupt(splitResponse, connection,
                        InterruptListenerStartedAgentResponse.class);
            case INTR_STOPPED:
                return interrupt(splitResponse, connection,
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

    private static AgentResponse init(List<String> splitMessage,
            ConnectionValueObject connection) throws IllegalResponseException {
        final int expectedElemSize = 2;

        if (splitMessage.size() != expectedElemSize) {
            throw new IllegalResponseException("expected elems: "
                    + expectedElemSize);
        }
        try {
            BoardType boardType = BoardType.parse(splitMessage.get(0));
            List<Feature> features = new ArrayList<>();
            for (String feat : splitMessage.get(1).split("\\s+")) {
                features.add(Feature.valueOf(feat));
            }
            return new InitAgentResponse(connection.getDevice(),
                    boardType, features);
        } catch (IllegalArgumentException e) {
            throw new IllegalResponseException(e);
        }
    }

    private static AgentResponse gpio(List<String> splitMessage,
            ConnectionValueObject connection)
            throws IllegalResponseException {
        final int expectedElemSize = 2;

        if (splitMessage.size() != expectedElemSize) {
            throw new IllegalResponseException("expected elems: "
                    + expectedElemSize);
        }

        ClientPin pin;
        Signal signal;
        try {
            pin = ClientPinFactory.getPin(splitMessage.get(0));
            signal = Signal.valueOf(splitMessage.get(1));
            return new GpioAgentResponse(signal, pin,
                    connection.getDevice().getAddress());
        } catch (IllegalArgumentException ex) {
            throw new IllegalResponseException("Illegal GPIO response.");
        }
    }

    private static AgentResponse i2c(List<String> splitMessage,
            ConnectionValueObject connection)
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
        return new I2cAgentResponse(splitMessage.get(0),
                connection.getDevice().getAddress());
    }

    private static AgentResponse spi(List<String> splitMessage,
            ConnectionValueObject connection)
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
        return new SpiAgentResponse(splitMessage.get(0),
                connection.getDevice().getAddress());
    }

    private static AgentResponse interrupt(List<String> splitMessage,
            ConnectionValueObject connection,
            Class<? extends InterruptAgentResponse> clazz)
            throws IllegalResponseException {
        try {
            ClientPin interruptPin
                    = ClientPinFactory.getPin(splitMessage.get(0));
            InterruptType intrType
                    = InterruptType.getType(splitMessage.get(1));
            LocalTime timeGenerated
                    = LocalTime.parse(splitMessage.get(2),
                            MessageParser.FORMATTER);
            InterruptValueObject interrupt
                    = InterruptManager.getInterruptListener(
                            connection.getDevice().getAddress(),
                            interruptPin,
                            intrType
                    );
            if (interrupt == null) {
                throw new IllegalResponseException(
                        String.format("No such combination of address '%s' "
                                + "and interrupt listener [pin=%s, type=%s]",
                                connection, interruptPin, intrType));
            }
            interrupt.setLastIntrTime(timeGenerated);
            return clazz
                    .getConstructor(InterruptValueObject.class,
                            InetAddress.class)
                    .newInstance(interrupt,
                            connection.getDevice().getAddress());
        } catch (IllegalArgumentException ex) {
            throw new IllegalResponseException("Illegal interrupt response.");
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

}
