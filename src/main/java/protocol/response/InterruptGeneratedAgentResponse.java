package protocol.response;

import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.InterruptManager;
import gui.userdata.InterruptValueObject;
import protocol.ListenerState;

public final class InterruptGeneratedAgentResponse
        extends AbstractInterruptAgentResponse {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(InterruptGeneratedAgentResponse.class);

    public InterruptGeneratedAgentResponse(InterruptValueObject response,
            InetAddress address) {
        super(response, address);
    }

    @Override
    public void react() {
        if (getResponse().stateProperty().get().equals(ListenerState.RUNNING)) {
            InterruptManager
                    .updateInterruptListener(getAddress(), getResponse());
        } else {
            LOGGER.debug("Message about interrupt has been received, "
                    + "but this listener is not active.");
        }
    }
}
