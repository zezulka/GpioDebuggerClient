package gui;

import core.net.NetworkManager;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import gui.layouts.controllers.ControllerUtils;

import core.util.StringConstants;
import javafx.scene.control.TabPane;
import gui.layouts.controllers.MasterWindowController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userdata.UserDataUtils;

public final class App extends Application {

    private static final int WINDOW_HEIGHT = 800;
    private static final int WINDOW_WIDTH = 1000;

    public static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private Scene scene;

    private static TabPane tabPane;

    static TabPane getTabPane() {
        return tabPane;
    }

    @Override
    public void init() throws Exception {
        scene = loadScene();
    }

    @Override
    public void start(Stage stage) {

        stage.setOnCloseRequest((event) -> {
            if (NetworkManager.isAnyConnectionOpened()) {
                if (ControllerUtils.showConfirmDialog(
                        StringConstants.CLOSE_WHEN_DEVICES_ACTIVE)) {
                    NetworkManager.disconnectAll();
                } else {
                    event.consume();
                    return;
                }
            }
            UserDataUtils.saveAllRequests();
            stage.close();
            Platform.exit();
        });
        stage.setScene(scene);
        stage.setMinHeight(WINDOW_HEIGHT);
        stage.setMinWidth(WINDOW_WIDTH);
        stage.setTitle("Debugger for RaspberryPi");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        UserDataUtils.saveAllDevices();
    }

    private Scene loadScene() {
        LOGGER.debug("Attempting to load scene...");
        try {
            FXMLLoader masterWindowLoader
                    = new FXMLLoader(ControllerUtils.MASTER);
            masterWindowLoader.setController(new MasterWindowController());
            Parent newParent = (Parent) masterWindowLoader.load();
            tabPane = (TabPane) newParent.lookup("#devicesTab");
            LOGGER.debug("Load successful.");
            return new Scene(newParent);
        } catch (IOException ex) {
            LOGGER.error("Load failed: ", ex);
            throw new RuntimeException(ex);
        }
    }
}
