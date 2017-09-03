package gui.feature.factories;

import gui.feature.Feature;
import gui.layouts.controllers.ControllerUtils;
import javafx.fxml.FXMLLoader;

public final class FxmlLoaderFactory {

    private FxmlLoaderFactory() {
    }

    /**
     * Note: new instance has to be always created since the following load()
     * call would throw Exception if called twice on the same object.
     */
    public static FXMLLoader of(Feature f) {
        switch (f) {
            case GPIO:
                return new FXMLLoader(ControllerUtils.GPIO);
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
