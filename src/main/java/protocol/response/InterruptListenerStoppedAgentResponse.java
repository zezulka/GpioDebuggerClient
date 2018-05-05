package protocol.response;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gui.userdata.InterruptValueObject;
import protocol.ListenerState;

public final class InterruptListenerStoppedAgentResponse
        extends AbstractInterruptAgentResponse {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(AbstractInterruptAgentResponse.class);

    public InterruptListenerStoppedAgentResponse(InterruptValueObject response,
            InetAddress address) {
        super(response, address);
    }

    @Override
    public void react() {
        LOGGER.debug(String.format("Pin %s listener's state changed to %s",
                getResponse().getClientPin().getGpioName(),
                ListenerState.NOT_RUNNING));
        getResponse().setState(ListenerState.NOT_RUNNING);
    }
}
