package layouts.controllers;

import core.ClientConnectionManager;

import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.TextInputControl;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protocol.BoardType;
import protocol.ProtocolMessages;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public final class GuiEntryPoint extends Application {

    private static Stage stage;
    private static final int RESPONSE_STRING_CAP_LEN = 1 << 12;
    private static final String FXML_EXT = ".fxml";

    private static final Logger GUI_LOGGER = LoggerFactory.getLogger(GuiEntryPoint.class);
    private static final GuiEntryPoint INSTANCE = new GuiEntryPoint();

    private static URL ipPrompt;
    private static URL i2cRequestForm;
    private static URL spiRequestForm;
    private static URL addInterruptForm;

    private static URL raspiController;
    private static URL beagleBoneBlackController;
    private static URL cubieBoardController;

    public GuiEntryPoint() {
        try {
            initControllerPaths();
        } catch (MalformedURLException ex) {
            GUI_LOGGER.error("Malformed URL. ", ex);
        }
    }

    private void initControllerPaths() throws MalformedURLException {
        ipPrompt = getPathToController("IpPrompt");
        raspiController = getPathToController("Raspi");
        i2cRequestForm = getPathToController("I2cRequestForm");
        spiRequestForm = getPathToController("SpiRequestForm");
        addInterruptForm = getPathToController("AddListenerForm");
    }

    private URL getPathToController(String controllerName) throws MalformedURLException {
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
                .append(controllerName)
                .append(FXML_EXT);

        return new File(builder.toString()).toURI().toURL();
    }

    public static void provideFeedback(String msg) {
        TextInputControl text = (TextInputControl) stage.getScene().lookup("#feedbackArea");

        text.setText(
                '<' + LocalTime.now().format(DateTimeFormatter.ISO_TIME)
                + "> "
                + msg
                + '\n'
                + (text.getText().length() < RESPONSE_STRING_CAP_LEN ? text.getText()
                : ""));
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            stage = primaryStage;
            switchScene(ipPrompt);
            stage.setTitle("Debugger for ARM-based devices");
            stage.show();
        } catch (IOException ex) {
            GUI_LOGGER.error(null, ex);
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        Platform.exit();
        System.exit(0);
    }

    public static GuiEntryPoint getInstance() {
        return INSTANCE;
    }

    private void switchScene(URL fxml) throws IOException {
        GUI_LOGGER.debug("Attempting to load " + fxml.toString() + " ...");
        Parent newParent = (Parent) FXMLLoader.load(fxml);
        GUI_LOGGER.debug("Load successful.");
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(newParent);
            stage.setMaximized(true);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(newParent);
        } 
    }

    private void switchToRaspi() throws IOException {
        switchScene(raspiController);
    }

    private void switchToBBB() throws IOException {
        switchScene(beagleBoneBlackController);
    }

    private void switchToCubieBoard() throws IOException {
        switchScene(cubieBoardController);
    }

    public void switchToIpPrompt() throws IOException {
        switchScene(ipPrompt);
    }

    public void switchToCurrentDevice() throws IOException {
        if (ClientConnectionManager.getInstance() == null) {
            GUI_LOGGER.debug("manager not ready!");
            return;
        }
        BoardType board = ClientConnectionManager.getInstance().getBoardType();

        if (board == null) {
            GUI_LOGGER.debug("cannot view device controller, no device available");
            return;
        }
        switch (board) {
            case BEAGLEBONEBLACK: {
                switchToBBB();
                break;
            }
            case CUBIEBOARD: {
                switchToCubieBoard();
                break;
            }
            case RASPBERRY_PI: {
                switchToRaspi();
                break;
            }
            default:
                throw new IllegalStateException(
                        ProtocolMessages.C_ERR_NO_BOARD.toString());
        }
    }

    private void createNewForm(URL fxml) throws IOException {
        Platform.runLater(() -> {
            FXMLLoader fxmlLoader = new FXMLLoader(fxml);
            Parent newRoot;
            try {
                newRoot = (Parent) fxmlLoader.load();
            } catch (IOException ex) {
                ControllerUtils.showErrorDialogMessage(ex.getMessage());
                GUI_LOGGER.error(String.format("Invalid resource locator: %s", fxml), ex);
                return;
            }
            Stage newStage = new Stage();
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initStyle(StageStyle.DECORATED);
            newStage.setScene(new Scene(newRoot));
            newStage.show();
        });
    }

    public void createNewI2cForm() throws IOException {
        createNewForm(i2cRequestForm);
    }

    public void createNewSpiForm() throws IOException {
        createNewForm(spiRequestForm);
    }

    public void createNewAddListenerForm() throws IOException {
        createNewForm(addInterruptForm);
    }
}
