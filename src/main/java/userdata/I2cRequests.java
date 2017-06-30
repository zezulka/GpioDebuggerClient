package userdata;
import java.util.List;
import javafx.collections.FXCollections;

public class I2cRequests {
    private final List<I2cRequestValueObject> requests;
    private boolean isDirty;
    
    public I2cRequests(List<I2cRequestValueObject> requests) {
        this.requests = FXCollections.observableArrayList(requests);
        this.isDirty = false;
    }

    public List<I2cRequestValueObject> getRequests() {
        return requests;
    }
    
    public void addNewRequest(I2cRequestValueObject request) {
        requests.add(request);
    }
    
    public boolean isDirty() {
        return isDirty;
    }
    
    public void setDirty(boolean val) {
        this.isDirty = val;
    }
    
    public boolean contains(I2cRequestValueObject request) {
        return requests.contains(request);
    }
    
}
