package gui.feature.factories;

import gui.feature.Feature;
import gui.controllers.ControllerUtils;
import javafx.fxml.FXMLLoader;
import protocol.BoardType;

public final class FxmlLoaderFactory {

    private FxmlLoaderFactory() {
    }

    /**
     * Note: new instance has to be always created since the following load()
     * call would throw Exception if called twice on the same object.
     */
    public static FXMLLoader of(Feature f, BoardType type) {
        switch (f) {
            case GPIO:
                return GpioFxmlLoaderFactory.of(type);
            case I2C:
                return new FXMLLoader(ControllerUtils.I2C);
            case INTERRUPTS:
                return new FXMLLoader(ControllerUtils.INTRS);
            case SPI:
                return new FXMLLoader(ControllerUtils.SPI);
            default:
                throw new UnsupportedOperationException("unsupported feature");
        }
    }

}
