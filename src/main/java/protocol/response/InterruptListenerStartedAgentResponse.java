package protocol.response;

import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gui.userdata.InterruptValueObject;
import protocol.ListenerState;

public final class InterruptListenerStartedAgentResponse
        extends AbstractInterruptAgentResponse implements AgentResponse {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(AbstractInterruptAgentResponse.class);

    public InterruptListenerStartedAgentResponse(InterruptValueObject response,
            InetAddress address) {
        super(response, address);
    }

    @Override
    public void react() {
        LOGGER.debug(String.format("Pin %s listener's state changed to %s",
                getResponse().getClientPin().getGpioName(),
                ListenerState.RUNNING));
        getResponse().setState(ListenerState.RUNNING);
    }
}
