package protocol;

import gui.userdata.DeviceValueObject;
import protocol.response.IllegalResponseException;
import protocol.response.util.AgentResponseFactory;
import java.time.format.DateTimeFormatter;

import gui.controllers.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MessageParser {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(MessageParser.class);
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("HH.mm.ss.S");

    private MessageParser() {
    }

    /**
     * Utility method for parsing incoming messages from agent. The main focus
     * of this method is to parse agent response using AgentResponseFactory and
     * then call react() method should the message be valid. Possible response
     * types:
     *
     * INIT:[BOARD_TYPE] GPIO:[PIN_NUMBER]:[HIGH|LOW] SPI:[BYTE_IN_HEX]*
     * SPI:[WRITE_REQUEST_OK] I2C:[BYTE_IN_HEX]* [INTR_STOPPED | INTR_STARTED |
     * INTR_GENERATED] :[PIN_NAME]:[INTERRUPT_TYPE]:[TIME] [ILLEGAL_REQUEST]
     * @throws IllegalArgumentException {@code agentMessage} is null
     */
    public static void parseAgentMessage(DeviceValueObject connection,
            String agentMessage) {
        if (agentMessage == null) {
            throw new IllegalArgumentException("Agent message cannot be null.");
        }
        try {
            AgentResponseFactory.of(agentMessage, connection).react();
            LOGGER.debug(String
                    .format("Received agent message: %s", agentMessage));
        } catch (IllegalResponseException ex) {
            LOGGER.error(String.format(
                    "Agent response '%s' could not be recognized, error: %s",
                    agentMessage, ex.getMessage())
            );
            Utils.showInfoDialog(String.format(
                    "Unrecognized message has been received: %s",
                    agentMessage)
            );
        }
    }
}
