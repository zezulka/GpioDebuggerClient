package protocol.response;

import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.InterruptValueObject;

public abstract class InterruptAgentResponse implements AgentResponse {

    private final InterruptValueObject response;
    private final InetAddress address;
    private static final Logger LOGGER
            = LoggerFactory.getLogger(InterruptAgentResponse.class);

    public InterruptAgentResponse(InterruptValueObject response,
            InetAddress address) {
        this.response = response;
        this.address = address;
    }

    protected final InterruptValueObject getResponse() {
        return response;
    }

    protected final InetAddress getAddress() {
        return address;
    }
}
