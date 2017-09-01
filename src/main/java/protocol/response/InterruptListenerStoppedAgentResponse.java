package protocol.response;

import java.net.InetAddress;
import gui.layouts.controllers.InterruptsTabController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.InterruptValueObject;
import protocol.ListenerState;

public final class InterruptListenerStoppedAgentResponse
        extends InterruptAgentResponse implements AgentResponse {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(InterruptAgentResponse.class);

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
        // This block of code informs InterruptTableController that the
        // listener has been successfully deregistered via synchronization
        // object; worker Thread is interrupted and removes the appropriate
        // listener if user requested to remove listener when it was still
        // running; otherwise, nothing happens
        synchronized (InterruptsTabController.SYNC) {
            InterruptsTabController.SYNC.notify();
        }
    }
}
