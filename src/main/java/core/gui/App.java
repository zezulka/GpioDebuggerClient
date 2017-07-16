package core.gui;

import core.net.ClientNetworkManager;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import layouts.controllers.ControllerUtils;
import layouts.controllers.MasterWindowController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.*;
import userdata.UserDataUtils;

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

    private static final Set<TabAddressPair> TAB_ADDR_PAIRS = new HashSet<>();
    private static InetAddress lastAddedAddress;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        loadScene();
        stage.setOnCloseRequest((event) -> {
            if (ClientNetworkManager.isAnyConnectionOpened()
                    && ControllerUtils.showConfirmationDialogMessage("Are you sure you want to close the whole application? All connections to devices will be closed.")) {
                ClientNetworkManager.disconnectAll();
            } else if(ClientNetworkManager.isAnyConnectionOpened()) {
                event.consume();
                return;
            }
            UserDataUtils.saveAllRequests();
            stage.close();
            Platform.exit();

        });
        stage.setScene(scene);
        stage.setMinHeight(800);
        stage.setResizable(false);
        stage.setMinWidth(1000);
        stage.setTitle("Debugger for RaspberryPi");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        UserDataUtils.saveAllDevices();
        super.stop();
    }
   
    public static InetAddress getLastAddress() {
        return lastAddedAddress;
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
            Platform.exit();
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

    public static TabPane getDevicesTab() {
        return (TabPane) scene.lookup("#devicesTab");
    }

    public static void loadNewTab(InetAddress address, BoardType type) {
        InetAddress previous = lastAddedAddress;
        lastAddedAddress = address;
        LOGGER.debug("Attempting to load " + type + " controller...");
        try {
            Tab pane = FXMLLoader.load(App.getUrlFromBoardType(type));
            pane.setText(address.getHostAddress() + '(' + type.toString() + ")\t");
            TAB_ADDR_PAIRS.add(new TabAddressPair(pane, address));
            Platform.runLater(() -> {
                getDevicesTab().getTabs().add(pane);
                getDevicesTab().getSelectionModel().select(pane);
            });
        } catch (IOException ex) {
            lastAddedAddress = previous;
            LOGGER.error("Load failed.", ex);
            Platform.exit();
        }
        LOGGER.debug("Load successful.");
    }

    public static void writeI2cResponseIntoTextArea(String response) {
        writeResponseIntoTextArea("#i2cTextArea", response);
    }

    public static void writeSpiResponseIntoTextArea(String response) {
        writeResponseIntoTextArea("#spiTextArea", response);
    }

    private static void writeResponseIntoTextArea(String lookupId, String response) {
        TextArea ta = ((TextArea) scene.lookup(lookupId));
        ta.setText(LocalTime.now().toString() + '\n' + response + '\n' + ta.getText());
    }

    public static void setPinButtonColourFromSignal(ClientPin pin, Signal signal) {
        Button btn = (Button) scene.lookup("#" + pin.getPinId());
        btn.setStyle("");
        btn.setStyle("-fx-background-color: #" + (signal.getBooleanValue() ? "55FF55" : "FF5555"));

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), btn);
        fadeTransition.setFromValue(1.0f);
        fadeTransition.setToValue(0.8f);
        fadeTransition.setCycleCount(1);
        fadeTransition.play();
    }

    public static Tab getTabFromInetAddress(InetAddress address) {
        for (TabAddressPair pair : TAB_ADDR_PAIRS) {
            if (pair.getAddress().equals(address)) {
                return pair.getTab();
            }
        }
        throw new IllegalArgumentException("no such address is registered");
    }

    public static InetAddress getIpAddressFromCurrentTab() {
        for (TabAddressPair pair : TAB_ADDR_PAIRS) {
            if (pair.getTab().equals(MasterWindowController.getCurrentTab())) {
                return pair.getAddress();
            }
        }
        throw new IllegalArgumentException();
    }
}
