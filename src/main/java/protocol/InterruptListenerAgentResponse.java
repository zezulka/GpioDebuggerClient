package protocol;

import core.util.ResponseType;
import java.time.LocalTime;
import layouts.controllers.InterruptTableController;

public class InterruptListenerAgentResponse implements AgentResponse {

    private final ResponseType responseType;
    private final LocalTime timeGenerated;
    private final ClientPin pin;
    private final InterruptType intrType;
    
    public InterruptListenerAgentResponse(ResponseType responseType, LocalTime timeGenerated, ClientPin pin, InterruptType intrType) {
        if(!responseType.isInterruptMessage()) {
           throw new IllegalArgumentException("Response is not of interrupt type."); 
        }
        this.responseType = responseType;
        this.timeGenerated = timeGenerated;
        this.pin = pin;
        this.intrType = intrType;
    }
    
    @Override
    public void react() {
        modifyInterruptValueObject();
    }
    
    private void modifyInterruptValueObject() {
        InterruptValueObject found = new InterruptValueObject(pin, intrType);
        switch (responseType) {
            case INTR_GENERATED: {
                found.setLatestInterruptTime(this.timeGenerated);
                break;
            }
            case INTR_STARTED: {
                found.setState(ListenerState.RUNNING);
                break;
            }
            case INTR_STOPPED: {
                found.setState(ListenerState.NOT_RUNNING);
                break;
            }
            default: throw new IllegalArgumentException();
        }
        InterruptTableController.updateInterruptListener(found);
    }
    
}
