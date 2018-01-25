package gui.feature.factories;

import gui.feature.Feature;
import gui.controllers.GpioTabController;
import gui.controllers.I2cTabController;
import gui.controllers.InterruptsTabController;
import gui.controllers.SpiTabController;
import java.net.InetAddress;
import javafx.fxml.Initializable;

public final class FxmlControllerFactory {

    private FxmlControllerFactory() {
    }

    public static Initializable of(InetAddress address, Feature f) {
        switch (f) {
            case GPIO:
                return new GpioTabController(address);
            case I2C:
                return new I2cTabController(address);
            case INTERRUPTS:
                return new InterruptsTabController(address);
            case SPI:
                return new SpiTabController(address);
            default:
                throw new UnsupportedOperationException("unsupported feature");
        }
    }

}
