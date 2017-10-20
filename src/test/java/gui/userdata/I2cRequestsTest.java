package gui.userdata;

import gui.misc.Operation;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class I2cRequestsTest {
    
    private I2cRequests requests;

    @Before
    public void init() {
        requests = new I2cRequests();
    }   
    
    @Test
    public void addTwoSame() {
        I2cRequestValueObject object = new I2cRequestValueObject(Operation.READ, "5", 0, "0x00");
        requests.addItem(object);
        assertThat(requests.getItems().size()).isEqualTo(1);
        requests.addItem(object);
        assertThat(requests.getItems().size()).isEqualTo(1);
    }
    
    @Test
    public void addTwoDifferent() {
        I2cRequestValueObject a = new I2cRequestValueObject(Operation.READ, "5", 0, "0x00");
        I2cRequestValueObject b = new I2cRequestValueObject(Operation.WRITE, "5", 0, "0x00");
        requests.addItem(a);
        assertThat(requests.getItems().size()).isEqualTo(1);
        requests.addItem(b);
        assertThat(requests.getItems().size()).isEqualTo(2);
    }
}
