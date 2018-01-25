package gui.feature.factories;

import gui.controllers.ControllerUtils;
import javafx.fxml.FXMLLoader;
import protocol.BoardType;

public final class GpioFxmlLoaderFactory {

    private GpioFxmlLoaderFactory() {
    }

    public static FXMLLoader of(BoardType type) {
        switch (type) {
            case RASPBERRY_PI:
                return new FXMLLoader(ControllerUtils.RASPI_GPIO);
            case TESTING:
                return new FXMLLoader(ControllerUtils.TESTING_GPIO);
            default:
                throw new IllegalArgumentException("type");
        }
    }
}
