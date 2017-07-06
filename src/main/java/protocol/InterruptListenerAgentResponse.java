package protocol;


import core.util.ResponseType;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterruptListenerAgentResponse implements AgentResponse {

    private final InterruptValueObject response;
    private final ResponseType type;
    private final LocalTime generatedAt;
    private static final Logger LOGGER = LoggerFactory.getLogger(InterruptListenerAgentResponse.class);
    
    public InterruptListenerAgentResponse(InterruptValueObject response, ResponseType type, LocalTime generatedAt) {
        this.response = response;
        this.type = type;
        this.generatedAt = generatedAt;
    }
    
    @Override
    public void react() {
        modifyInterruptValueObject();
    }
    
    private void modifyInterruptValueObject() {
        switch (type) {
            case INTR_GENERATED: {
                response.setLatestInterruptTime(generatedAt);
                break;
            }
            case INTR_STARTED: {
                LOGGER.debug(String.format("Pin %s listener's state changed to %s", response.getClientPin().getName(), ListenerState.RUNNING));
                response.setState(ListenerState.RUNNING);
                break;
            }
            case INTR_STOPPED: {
                LOGGER.debug(String.format("Pin %s listener's state changed to %s", response.getClientPin().getName(), ListenerState.NOT_RUNNING));
                response.setState(ListenerState.NOT_RUNNING);
                break;
            }
            default: throw new IllegalArgumentException();
        }
        InterruptManager.updateInterruptListener(response);
    }
    
}
