package core.gui;

import core.net.NetworkManager;

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
import javafx.stage.Stage;

import javafx.util.Duration;

import layouts.controllers.ControllerUtils;

import core.util.StringConstants;
import javafx.scene.Node;
import layouts.controllers.I2cRequestFormController;
import layouts.controllers.InterruptTableController;
import layouts.controllers.RaspiController;
import layouts.controllers.SpiRequestFormController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userdata.UserDataUtils;

import protocol.BoardType;
import protocol.ClientPin;
import protocol.Signal;

public final class App extends Application {

    private static Scene scene;
    private static Stage stage;
    private static final String FXML_EXT = ".fxml";

    private static final int WINDOW_HEIGHT = 800;
    private static final int WINDOW_WIDTH = 1000;

    public static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static URL masterWindow;
    private static URL raspi;
    private static URL beagleBoneBlack;
    private static URL cubieboard;
    private static FXMLLoader spiLoader;
    private static FXMLLoader i2cLoader;
    private static FXMLLoader interruptsLoader;

    private static final Set<TabAddressPair> TAB_ADDR_PAIRS = new HashSet<>();

    private static final App INSTANCE = new App();

    public static App getInstance() {
        return INSTANCE;
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        loadScene();
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
        UserDataUtils.saveAllDevices();
        super.stop();
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
                throw new IllegalArgumentException("unsupported board type");
        }
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
            raspi = getPathToFxml("Raspi");
            masterWindow = getPathToFxml("MasterWindow");
            i2cLoader = getFxmlLoaderFromPath("I2cRequestForm");
            spiLoader = getFxmlLoaderFromPath("SpiRequestForm");
            interruptsLoader = getFxmlLoaderFromPath("InterruptTable");
        } catch (MalformedURLException ex) {
            LOGGER.error(null, ex);
            Platform.exit();
        }
    }

    private URL getPathToFxml(String fxmlName)
            throws MalformedURLException {
        return getClass().getResource(File.separator + "fxml"
                + File.separator + fxmlName + FXML_EXT);
    }

    private FXMLLoader getFxmlLoaderFromPath(String fxmlName)
            throws MalformedURLException {
        return new FXMLLoader(getPathToFxml(fxmlName));
    }

    public static TabPane getDevicesTab() {
        return (TabPane) scene.lookup("#devicesTab");
    }

    public void loadNewTab(InetAddress address, BoardType type) {
        LOGGER.debug("Attempting to load " + type + " controller...");
        try {
            FXMLLoader raspiLoader = new FXMLLoader(getUrlFromBoardType(type));
            RaspiController raspiContr = new RaspiController(address);
            raspiLoader.setController(raspiContr);

            I2cRequestFormController i2cContr
                    = new I2cRequestFormController(address);
            i2cLoader.setController(i2cContr);

            SpiRequestFormController spiContr
                    = new SpiRequestFormController(address);
            spiLoader.setController(spiContr);

            InterruptTableController intrContr
                    = new InterruptTableController(address);
            interruptsLoader.setController(intrContr);

            Tab pane = raspiLoader.load();
            pane.setId(address.getHostAddress());
            pane.setText(address.getHostAddress());


            // Load all tabs programatically since they were generated
            // dynamically
            TabPane inner = (TabPane) pane.getContent().lookup("#innerTabPane");
            inner.getTabs().addAll(i2cLoader.load(),
                                   spiLoader.load(),
                                   interruptsLoader.load());

            TAB_ADDR_PAIRS.add(new TabAddressPair(pane, address));
            Platform.runLater(() -> {
                getDevicesTab().getTabs().add(pane);
                getDevicesTab().getSelectionModel().select(pane);
            });
        } catch (IOException ex) {
            LOGGER.error("Load failed.", ex);
            Platform.exit();
        }
        LOGGER.debug("Load successful.");
    }

    public static void displayI2cResponse(String response, InetAddress source) {
        updateTextArea("#i2cTextArea", response, source);
    }

    public static void displaySpiResponse(String response, InetAddress source) {
        updateTextArea("#spiTextArea", response, source);
    }

    private static void updateTextArea(String lookupIdPrefix, String response,
            InetAddress source) {
        TextArea ta
                = ((TextArea) scene.lookup(lookupIdPrefix
                        + ':' + source.getHostAddress()));
        ta.setText(LocalTime.now().toString()
                + '\n' + response
                + '\n' + ta.getText());
    }

    public static void setPinButtonColourFromSignal(ClientPin pin,
            Signal signal, InetAddress source) {
        Node n = getDevicesTab();

        TabPane pane = (TabPane) n;
        for (Tab t : pane.getTabs()) {
            if (t.getId().equals(source.getHostAddress())) {
                Button btn = (Button) t.getContent()
                        .lookup("#" + pin.getPinId());
                btn.setStyle("");
                String color = signal.getBooleanValue() ? "55FF55" : "FF5555";
                btn.setStyle("-fx-background-color: #" + color);

                playFadeTransition(btn);
                break;
            }
        }
    }

    private static void playFadeTransition(Button btn) {
        final double startVal = 1.0f;
        final double endVal = 0.8f;
        final int playDurationMilis = 500;

        FadeTransition fadeTransition
                = new FadeTransition(Duration.millis(playDurationMilis), btn);
        fadeTransition.setFromValue(startVal);
        fadeTransition.setToValue(endVal);
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
}
