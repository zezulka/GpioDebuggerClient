package gui.tab.loader;

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
import java.util.Collection;
import java.util.List;
import protocol.BoardType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TabLoaderImpl implements TabLoader {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(TabLoaderImpl.class);

    @Override
    public Tab loadNewTab(InetAddress address, BoardType type,
            Collection<Feature> features) {
        LOGGER.debug("Attempting to load root controller...");
        try {
            FXMLLoader raspiLoader
                    = new FXMLLoader(ControllerUtils.getBoardUrl());
            raspiLoader.setController(ControllerUtils.getDeviceController());
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
}
