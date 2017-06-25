package core.util;

import java.io.IOException;
import java.time.LocalTime;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javafx.application.Platform;
import layouts.controllers.InterruptTableController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protocol.ClientPin;
import protocol.InterruptListenerStatus;
import protocol.InterruptType;
import protocol.InterruptValueObject;
import protocol.ListenerState;
import protocol.RaspiClientPin;

/**
 *
 * @author Miloslav Zezulka
 */
public class MessageParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageParser.class);
    private static final Set<InterruptListenerStatus> SPECIAL_PREFIXES
            = new HashSet<>(Arrays.asList(InterruptListenerStatus.values()));

    
    /**
     * Utility method for parsing incoming messages from agent. The main focus 
     * of this method is to determine whether the message deals with interrupt-oriented
     * events. If not, the output is simply put in the TextArea of the user UI.
     * @param agentMessage
     * @throws IOException 
     * @throws IllegalArgumentException {@code agentMessage} is null
     */
    public static void parseAgentMessage(String agentMessage) throws IOException {
        if (agentMessage == null) {
            throw new IllegalArgumentException("Agent message cannot be null.");
        }
        InterruptValueObject object;
        if ((object = MessageParser.getInterruptValueObjectFromMessage(agentMessage)) != null) {
            InterruptTableController.updateInterruptListener(object);
        } else {
            //this is just a normal message, print it out
            System.out.println(agentMessage);
            //Platform.runLater(() -> GuiEntryPoint.provideFeedback(agentMessage));
        }
    }

    private static boolean isInterruptMessage(String messagePrefix) {
        if (messagePrefix == null) {
            return false;
        }
        try {
            return SPECIAL_PREFIXES.contains(InterruptListenerStatus.valueOf(messagePrefix));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static String getMessagePrefix(String message) {
        int firstSeparatorOccurence = message.indexOf(":");
        return message.substring(0, firstSeparatorOccurence < 0 ? message.length() : firstSeparatorOccurence);
    }

    /**
     * 
     * @param agentMessage
     * @return 
     */
    private static InterruptValueObject getInterruptValueObjectFromMessage(String agentMessage) {
        if (agentMessage == null) {
            throw new IllegalArgumentException("agent message cannot be null");
        }
        if (!MessageParser.isInterruptMessage(MessageParser.getMessagePrefix(agentMessage))) {
            return null;
        }
        LOGGER.debug(String.format("Special interrupt message from agent is about to get processed: %s", agentMessage));
        String[] splitMessage = agentMessage.split(":");
        //bound to Raspi only!!!!
        ClientPin pin;
        InterruptType type;
        InterruptListenerStatus status;
        LocalTime localTime;
        try {
            status = InterruptListenerStatus.valueOf(splitMessage[0]);
            pin = RaspiClientPin.getPin(splitMessage[1]);
            type = InterruptType.getType(splitMessage[2]);
            localTime = LocalTime.ofNanoOfDay(Long.valueOf(splitMessage[3].replace("\n", "")));
        } catch (IllegalArgumentException ex) {
            return null;
        }
        InterruptValueObject result = new InterruptValueObject(pin, type);
        modifyInterruptValueObject(result, status, localTime);
        return result;
    }

    private static void modifyInterruptValueObject(InterruptValueObject object, InterruptListenerStatus status, LocalTime locTime) {
        switch (status) {
            case INTR_GENERATED: {
                object.setLatestInterruptTime(locTime);
                break;
            }
            case INTR_STARTED: {
                object.setState(ListenerState.RUNNING);
                break;
            }
            case INTR_STOPPED: {
                object.setState(ListenerState.NOT_RUNNING);
                break;
            }
        }
    }
}
