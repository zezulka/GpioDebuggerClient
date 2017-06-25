package core.gui;

import core.net.ClientNetworkManager;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import layouts.controllers.ControllerUtils;
import layouts.controllers.MasterWindowController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.BoardType;

public class App extends Application {

    private static Scene scene;
    private static Stage stage;
    private static final String FXML_EXT = ".fxml";

    public static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static URL ipPrompt;
    private static URL addListenerForm;
    private static URL masterWindow;
    private static URL raspi;
    private static URL beagleBoneBlack;
    private static URL cubieboard;

    private static final Map<Tab, InetAddress> TAB_TO_ADDR_MAPPING = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        loadScene();
        stage.setOnCloseRequest((event) -> {
            if (ClientNetworkManager.isAnyConnectionOpened()
                    && ControllerUtils.showConfirmationDialogMessage("Are you sure you want to close the whole application? All connections to devices will be closed.")) {
                ClientNetworkManager.disconnectAll();

            }
            stage.close();
            Platform.exit();
        });
        stage.setScene(scene);
        stage.setMinHeight(700);
        stage.setMinWidth(1000);
        stage.setTitle("Debugger for ARM-based devices");
        stage.show();
    }

    public static URL getUrlFromBoardType(BoardType type) {
        switch (type) {
            case RASPBERRY_PI:
                return raspi;
            case BEAGLEBONEBLACK:
                return beagleBoneBlack;
            case CUBIEBOARD:
                return cubieboard;
            default:
                throw new IllegalArgumentException("[URL loader] unsupported board type");
        }
    }

    public static void createNewIpPromptForm() {
        createNewForm(ipPrompt);
    }

    public static void createNewAddListenerPromptForm() {
        createNewForm(addListenerForm);
    }

    private static void createNewForm(URL fxml) {
        Platform.runLater(() -> {
            FXMLLoader fxmlLoader = new FXMLLoader(fxml);
            Parent newRoot;
            try {
                newRoot = (Parent) fxmlLoader.load();
            } catch (IOException ex) {
                ControllerUtils.showErrorDialogMessage(ex.getMessage());
                LOGGER.error(String.format("Invalid resource locator: %s", fxml), ex);
                return;
            }
            Stage newStage = new Stage();
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initStyle(StageStyle.DECORATED);
            newStage.setScene(new Scene(newRoot));
            newStage.showAndWait();
        });
    }

    private void loadScene() {
        LOGGER.debug("Attempting to load " + masterWindow.toString() + " ...");
        Parent newParent = null;
        try {
            newParent = (Parent) FXMLLoader.load(masterWindow);
        } catch (IOException ex) {
            LOGGER.error("Load failed: ", ex);
            Platform.exit();
        }
        LOGGER.debug("Load successful.");
        scene = new Scene(newParent);
    }

    @Override
    public void init() throws Exception {
        super.init();
        try {
            ipPrompt = getPathToFxml("IpPrompt");
            raspi = getPathToFxml("Raspi");
            masterWindow = getPathToFxml("MasterWindow");
            addListenerForm = getPathToFxml("AddListenerForm");
        } catch (MalformedURLException ex) {
            LOGGER.error(null, ex);
        }
    }

    private static URL getPathToFxml(String fxmlName) throws MalformedURLException {
        StringBuilder builder = new StringBuilder();
        builder = builder
                .append("src")
                .append(File.separator)
                .append("main")
                .append(File.separator)
                .append("resources")
                .append(File.separator)
                .append("fxml")
                .append(File.separator)
                .append(fxmlName)
                .append(FXML_EXT);

        return new File(builder.toString()).toURI().toURL();
    }

    public static void loadNewTab(InetAddress address, BoardType type) {
        LOGGER.debug("Attempting to load " + type + " controller...");
        try {
            Tab pane = FXMLLoader.load(App.getUrlFromBoardType(type));
            pane.setText(address.toString() + "\t" + type.name());
            Platform.runLater(() -> {
                ((TabPane) scene.lookup("#devicesTab")).getTabs().add(pane);
            });
            TAB_TO_ADDR_MAPPING.put(pane, address);
        } catch (IOException ex) {
            LOGGER.error("Load failed.", ex);
            Platform.exit();
        }
        LOGGER.debug("Load successful.");
    }

    public static InetAddress getIpAddressFromCurrentTab() {
        return TAB_TO_ADDR_MAPPING.get(MasterWindowController.getCurrentTab());
    }
}
