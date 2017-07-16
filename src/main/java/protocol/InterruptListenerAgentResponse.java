package protocol;

import core.util.ResponseType;
import java.net.InetAddress;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterruptListenerAgentResponse implements AgentResponse {

    private final InterruptValueObject response;
    private final ResponseType type;
    private final LocalTime generatedAt;
    private final InetAddress source;
    private static final Logger LOGGER = LoggerFactory.getLogger(InterruptListenerAgentResponse.class);

    public InterruptListenerAgentResponse(InterruptValueObject response, ResponseType type, LocalTime generatedAt, InetAddress address) {
        this.response = response;
        this.type = type;
        this.generatedAt = generatedAt;
        this.source = address;
    }

    @Override
    public void react() {
        modifyInterruptValueObject();
    }

    private void modifyInterruptValueObject() {
        switch (type) {
            case INTR_GENERATED: {
                if (response.stateProperty().get().equals(ListenerState.RUNNING)) {
                    response.setNumberOfInterrupts(response.numberOfInterruptsProperty().get() + 1);
                    response.setLatestInterruptTime(generatedAt);
                } else {
                    LOGGER.debug("Message about interrupt has been received, but this listener is not active.");
                }
                break;
            }
            case INTR_STARTED: {
                LOGGER.debug(String.format("Pin %s listener's state changed to %s", response.getClientPin().getGpioName(), ListenerState.RUNNING));
                response.setState(ListenerState.RUNNING);
                break;
            }
            case INTR_STOPPED: {
                LOGGER.debug(String.format("Pin %s listener's state changed to %s", response.getClientPin().getGpioName(), ListenerState.NOT_RUNNING));
                response.setState(ListenerState.NOT_RUNNING);
                break;
            }
            default:
                throw new IllegalArgumentException();
        }
        InterruptManager.updateInterruptListener(source, response);
    }

}
