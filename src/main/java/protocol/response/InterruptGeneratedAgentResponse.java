package protocol.response;

import java.net.InetAddress;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.InterruptValueObject;
import protocol.ListenerState;

public final class InterruptGeneratedAgentResponse
        extends InterruptAgentResponse {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(InterruptGeneratedAgentResponse.class);

    public InterruptGeneratedAgentResponse(InterruptValueObject response,
            LocalTime generatedAt, InetAddress address) {
        super(response, generatedAt, address);
    }

    @Override
    protected void modifyInterruptValueObjectImpl() {
        if (getResponse().stateProperty().get().equals(ListenerState.RUNNING)) {
            final int newNumIntrs
                    = getResponse().numberOfInterruptsProperty().get() + 1;
            getResponse().setNumberOfInterrupts(newNumIntrs);
            getResponse().setLatestInterruptTime(getGeneratedAt());
        } else {
            LOGGER.debug("Message about interrupt has been received, "
                    + "but this listener is not active.");
        }
    }
}
