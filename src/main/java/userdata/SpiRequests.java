package userdata;

import java.util.List;
import javafx.collections.FXCollections;

public final class SpiRequests {

    private final List<SpiRequestValueObject> requests;
    private boolean isDirty;

    public SpiRequests(List<SpiRequestValueObject> requests) {
        this.requests = FXCollections.observableArrayList(requests);
        this.isDirty = false;
    }

    public List<SpiRequestValueObject> getRequests() {
        return requests;
    }

    public void addNewRequest(SpiRequestValueObject request) {
        requests.add(request);
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean val) {
        this.isDirty = val;
    }

    public boolean contains(SpiRequestValueObject request) {
        return requests.contains(request);
    }
}
