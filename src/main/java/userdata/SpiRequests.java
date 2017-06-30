package userdata;
import java.util.Collections;
import java.util.List;

public final class SpiRequests {
    private final List<SpiRequestValueObject> requests;

    public SpiRequests(List<SpiRequestValueObject> requests) {
        this.requests = Collections.unmodifiableList(requests);
    }

    public List<SpiRequestValueObject> getRequests() {
        return requests;
    }
}
