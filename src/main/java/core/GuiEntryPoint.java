/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

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

    private static final Logger GUI_LOGGER = LoggerFactory.getLogger(GuiEntryPoint.class);
    private static final GuiEntryPoint INSTANCE = new GuiEntryPoint();
    private static final File PATH_TO_FXML_DIR = new File(System.getProperty("user.dir")
            + File.separator + "src"
            + File.separator + "main"
            + File.separator + "resources"
            + File.separator + "fxml");
    private static URL ipPrompt;

    private static URL i2cRequestForm;
    private static URL spiRequestForm;
    
    private static URL raspiController;
    private static URL beagleBoneBlackController;
    private static URL cubieBoardController;

    public GuiEntryPoint() {
        try {
            //TODO: refactor this ugly code, possibly create new FileLoader utility class?...
            File pathToFxml = new File(PATH_TO_FXML_DIR + File.separator + "IpPrompt" + ".fxml");
            ipPrompt = pathToFxml.toURI().toURL();
            pathToFxml = new File(PATH_TO_FXML_DIR + File.separator + "Raspi" + ".fxml");
            raspiController = pathToFxml.toURI().toURL();
            pathToFxml = new File(PATH_TO_FXML_DIR + File.separator + "I2cRequestForm" + ".fxml");
            i2cRequestForm = pathToFxml.toURI().toURL();
            pathToFxml = new File(PATH_TO_FXML_DIR + File.separator + "SpiRequestForm" + ".fxml");
            spiRequestForm = pathToFxml.toURI().toURL();
        } catch (MalformedURLException ex) {
            GUI_LOGGER.error("Malformed URL:", ex);
        }
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
            scene = new Scene(newParent, 1000, 800);
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

    
    public void switchToIpPrompt() throws IOException{
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

    private void createNewInterfaceForm(URL fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(fxml);
        Parent newRoot = (Parent) fxmlLoader.load();
        Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initStyle(StageStyle.DECORATED);
        newStage.setTitle("Interface request");
        newStage.setScene(new Scene(newRoot));
        newStage.show();
    }

    public void createNewI2cForm() throws IOException {
        createNewInterfaceForm(i2cRequestForm);
    }

    public void createNewSpiForm() throws IOException {
        createNewInterfaceForm(spiRequestForm);
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

}
