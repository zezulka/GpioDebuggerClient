package gui;

import java.io.IOException;
import java.net.InetAddress;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import gui.layouts.controllers.ControllerUtils;
import gui.layouts.controllers.I2cTabController;
import gui.layouts.controllers.InterruptsTabController;
import gui.layouts.controllers.SpiTabController;
import protocol.BoardType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RaspiTabLoader implements TabLoader {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(RaspiTabLoader.class);

    @Override
    public Tab loadNewTab(InetAddress address, BoardType type) {
        LOGGER.debug("Attempting to load " + type + " controller...");
        try {
            FXMLLoader raspiLoader
                    = new FXMLLoader(ControllerUtils.getUrlFromBoardType(type));
            raspiLoader.setController(ControllerUtils
                    .getControllerFromBoardType(type, address));

            // new FXMLLoader instance has to be loaded every single instance
            FXMLLoader gpioLoader = new FXMLLoader(ControllerUtils.GPIO);
            FXMLLoader i2cLoader = new FXMLLoader(ControllerUtils.I2C);
            FXMLLoader spiLoader = new FXMLLoader(ControllerUtils.SPI);
            FXMLLoader intrsLoader = new FXMLLoader(ControllerUtils.INTRS);
            i2cLoader.setController(new I2cTabController(address));
            spiLoader.setController(new SpiTabController(address));
            intrsLoader.setController(new InterruptsTabController(address));

            Tab pane = raspiLoader.load();
            pane.setId(address.getHostAddress());
            pane.setText(address.getHostAddress());

            // Load all tabs programatically since they were generated
            // dynamically
            TabPane inner = (TabPane) pane.getContent().lookup("#innerTabPane");
            inner.getTabs().addAll(i2cLoader.load(), spiLoader.load(),
                    intrsLoader.load(), gpioLoader.load());
            App.getTabPane().getTabs().add(pane);
            LOGGER.debug("Load successful.");
            return pane;
        } catch (IOException ex) {
            LOGGER.error("Load failed.", ex);
            throw new RuntimeException();
        }
    }
}
