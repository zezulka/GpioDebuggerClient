package protocol.response;

import java.net.InetAddress;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.InterruptValueObject;
import protocol.ListenerState;

public final class InterruptListenerStartedAgentResponse
        extends InterruptAgentResponse implements AgentResponse {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(InterruptAgentResponse.class);

    public InterruptListenerStartedAgentResponse(InterruptValueObject response,
            LocalTime generatedAt, InetAddress address) {
        super(response, generatedAt, address);
    }

    @Override
    protected void modifyInterruptValueObjectImpl() {
        LOGGER.debug(String.format("Pin %s listener's state changed to %s",
                getResponse().getClientPin().getGpioName(),
                ListenerState.RUNNING));
        getResponse().setState(ListenerState.RUNNING);
    }
}