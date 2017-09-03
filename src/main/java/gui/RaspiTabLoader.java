package gui;

import gui.feature.Feature;
import gui.feature.factories.FxmlControllerFactory;
import gui.feature.factories.FxmlLoaderFactory;
import java.io.IOException;
import java.net.InetAddress;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import gui.layouts.controllers.ControllerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import protocol.BoardType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RaspiTabLoader implements TabLoader {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(RaspiTabLoader.class);

    @Override
    public Tab loadNewTab(InetAddress address, BoardType type,
            Set<Feature> features) {
        LOGGER.debug("Attempting to load " + type + " controller...");
        try {
            FXMLLoader raspiLoader
                    = new FXMLLoader(ControllerUtils.getUrlFromBoardType(type));
            raspiLoader.setController(ControllerUtils
                    .getControllerFromBoardType(type));
            List<FXMLLoader> loaders = new ArrayList<>();
            for (Feature f : features) {
                FXMLLoader loader = FxmlLoaderFactory.of(f);
                loader.setController(FxmlControllerFactory.of(address, f));
                loaders.add(loader);
            }

            Tab pane = raspiLoader.load();
            pane.setId(address.getHostAddress());
            pane.setText(address.getHostAddress());

            // Load all tabs programatically since they were generated
            // dynamically
            TabPane inner = (TabPane) pane.getContent().lookup("#innerTabPane");
            loaders.forEach((loader) -> {
                try {
                    inner.getTabs().add(loader.load());
                } catch (IOException ex) {
                    LOGGER.debug(null, ex);
                    throw new RuntimeException(ex);
                }
            });
            LOGGER.debug("Load successful.");
            return pane;
        } catch (IOException ex) {
            LOGGER.error("Load failed.", ex);
            throw new RuntimeException();
        }
    }
}
