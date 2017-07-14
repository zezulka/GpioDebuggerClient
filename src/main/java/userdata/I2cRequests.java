package userdata;

import java.util.List;
import javafx.collections.FXCollections;

public class I2cRequests {
    private final List<I2cRequestValueObject> requests;
    private boolean dirty;
    
    public I2cRequests(List<I2cRequestValueObject> requests) {
        this.requests = FXCollections.observableArrayList(requests);
        this.dirty = false;
    }

    public List<I2cRequestValueObject> getRequests() {
        return requests;
    }
    
    public void addNewRequest(I2cRequestValueObject request) {
        this.dirty = true;
        requests.add(request);
    }
    
    public boolean isDirty() {
        return dirty;
    }
    
    public boolean contains(I2cRequestValueObject request) {
        return requests.contains(request);
    }
    
}
