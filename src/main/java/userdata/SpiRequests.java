package userdata;

import java.util.List;
import javafx.collections.FXCollections;

public final class SpiRequests {

    private final List<SpiRequestValueObject> requests;
    private boolean dirty;

    public SpiRequests(List<SpiRequestValueObject> requests) {
        this.requests = FXCollections.observableArrayList(requests);
        this.dirty = false;
    }

    public List<SpiRequestValueObject> getRequests() {
        return requests;
    }

    public void addNewRequest(SpiRequestValueObject request) {
        this.dirty = true;
        requests.add(request);
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean contains(SpiRequestValueObject request) {
        return requests.contains(request);
    }
}
