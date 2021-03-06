package gui.tab.loader;

import gui.controllers.Utils;
import gui.controllers.GpioTab;
import gui.controllers.I2CTab;
import gui.controllers.InterruptsTab;
import gui.controllers.SpiTab;
import gui.misc.Feature;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.BoardType;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TabLoaderImpl implements TabLoader {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(TabLoaderImpl.class);

    @Override
    public Tab loadNewTab(InetAddress address, BoardType type,
                          Collection<Feature> features) {
        LOGGER.debug("Attempting to load root controller...");
        try {
            FXMLLoader raspiLoader
                    = new FXMLLoader(Utils.getBoardUrl());
            raspiLoader.setController(Utils.getDeviceController());
            List<FXMLLoader> loaders = new ArrayList<>();
            for (Feature f : features) {
                FXMLLoader loader = FxmlLoaderFactory.of(f, type);
                loader.setController(FxmlControllerFactory.of(address, f));
                loaders.add(loader);
            }

            Tab pane = raspiLoader.load();
            pane.setId(address.getHostAddress());
            pane.setText(address.getHostAddress());

            // Load all tabs programatically since they were generated
            // dynamically
            TabPane inner = (TabPane) pane.getContent().lookup("#innerTabPane");
            for (FXMLLoader l : loaders) {
                inner.getTabs().add(l.load());
            }
            LOGGER.debug("Load successful.");
            return pane;
        } catch (IOException ex) {
            LOGGER.error("Load failed.", ex);
            throw new RuntimeException(ex);
        }
    }

    private static final class FxmlControllerFactory {
        private FxmlControllerFactory() {  // Do not instantiate factory class.
        }

        public static Initializable of(InetAddress address, Feature f) {
            switch (f) {
                case GPIO:
                    return new GpioTab(address);
                case I2C:
                    return new I2CTab(address);
                case INTERRUPTS:
                    return new InterruptsTab(address);
                case SPI:
                    return new SpiTab(address);
                default:
                    throw new UnsupportedOperationException("unsupported");
            }
        }
    }

    private static final class FxmlLoaderFactory {
        private FxmlLoaderFactory() { // Do not instantiate factory class.
        }

        public static FXMLLoader of(Feature f, BoardType type) {
            switch (f) {
                case GPIO:
                    return GpioFxmlLoaderFactory.of(type);
                case I2C:
                    return new FXMLLoader(Utils.I2C);
                case INTERRUPTS:
                    return new FXMLLoader(Utils.INTRS);
                case SPI:
                    return new FXMLLoader(Utils.SPI);
                default:
                    throw new UnsupportedOperationException("unsupported");
            }
        }
    }

    private static final class GpioFxmlLoaderFactory {
        private GpioFxmlLoaderFactory() { // Do not instantiate factory class.
        }

        public static FXMLLoader of(BoardType type) {
            switch (type) {
                case RASPBERRY_PI:
                    return new FXMLLoader(Utils.RASPI_GPIO);
                case TESTING:
                    return new FXMLLoader(Utils.TESTING_GPIO);
                default:
                    throw new IllegalArgumentException("type");
            }
        }
    }
}
