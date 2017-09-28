package gui.userdata;

import gui.layouts.controllers.Operation;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author miloslav
 */
public class SpiRequestsTest {
    
    private SpiRequests requests;

    @Before
    public void init() {
        requests = new SpiRequests();
    }
    
    @Test
    public void addTwoSame() {
        SpiRequestValueObject object = new SpiRequestValueObject(2, Operation.READ, "0x00");
        requests.addItem(object);
        assertThat(requests.getItems().size()).isEqualTo(1);
        requests.addItem(object);
        assertThat(requests.getItems().size()).isEqualTo(1);
    }
    
    @Test
    public void addTwoDifferent() {
        SpiRequestValueObject a = new SpiRequestValueObject(2, Operation.READ, "0x00");
        SpiRequestValueObject b = new SpiRequestValueObject(2, Operation.WRITE, "0x00");
        requests.addItem(a);
        assertThat(requests.getItems().size()).isEqualTo(1);
        requests.addItem(b);
        assertThat(requests.getItems().size()).isEqualTo(2);
    }
}
