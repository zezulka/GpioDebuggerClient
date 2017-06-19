package core.util;

import java.time.LocalTime;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

    public static String getMessagePrefix(String message) {
        int firstSeparatorOccurence = message.indexOf(":");
        return message.substring(0, firstSeparatorOccurence < 0 ? message.length() : firstSeparatorOccurence);
    }

    public static InterruptValueObject getInterruptValueObjectFromMessage(String agentMessage) {
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
