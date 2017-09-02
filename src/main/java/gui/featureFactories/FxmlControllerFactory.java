package gui.featureFactories;

import core.util.Feature;
import gui.layouts.controllers.GpioTabController;
import gui.layouts.controllers.I2cTabController;
import gui.layouts.controllers.InterruptsTabController;
import gui.layouts.controllers.SpiTabController;
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
