package protocol.response;

import java.net.InetAddress;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.InterruptManager;
import protocol.InterruptValueObject;

public abstract class InterruptAgentResponse implements AgentResponse {

    private final InterruptValueObject response;
    private final LocalTime generatedAt;
    private final InetAddress address;
    private static final Logger LOGGER
            = LoggerFactory.getLogger(InterruptAgentResponse.class);

    public InterruptAgentResponse(InterruptValueObject response,
            LocalTime generatedAt, InetAddress address) {
        this.response = response;
        this.generatedAt = generatedAt;
        this.address = address;
    }

    protected final InterruptValueObject getResponse() {
        return response;
    }

    protected final LocalTime getGeneratedAt() {
        return generatedAt;
    }

    protected final InetAddress getAddress() {
        return address;
    }

    @Override
    public final void react() {
        modifyInterruptValueObject();
    }

    private void modifyInterruptValueObject() {
        modifyInterruptValueObjectImpl();
        InterruptManager.updateInterruptListener(address, response);
    }

    protected abstract void modifyInterruptValueObjectImpl();
}
