package layouts.controllers;

import core.ClientConnectionManager;
import core.GuiEntryPoint;

import java.io.IOException;

import java.net.URL;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protocol.ProtocolMessages;

/**
 *
 * @author Miloslav Zezulka
 */
public class RaspiController implements DeviceController, Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaspiController.class);
    
    @FXML
    private RadioButton readRadioButton;

    /**
     * String containing Button title which caused this event to happen. The
     * Button title is equivalent to the one in bulldog naming. (i.e. RaspiNames
     * etc.)
     *
     * @param event
     * @throws IllegalArgumentException in case event is not of Button instance
     */
    @FXML
    protected void sendGpioRequest(MouseEvent event) {
        String op = readRadioButton.isSelected() ? "read" : "write";
        sendRequest(event, "gpio:" + op + ":" + getButtonTitle(event));
    }
    
    @FXML
    protected void createSpiForm(MouseEvent event) {
        try {
            GuiEntryPoint.getInstance().createNewSpiForm();
        } catch (IOException ex) {
            LOGGER.error(null, ex);
        }
    }
    
    @FXML
    protected void createI2cForm(MouseEvent event) {
        try {
            GuiEntryPoint.getInstance().createNewI2cForm();
        } catch (IOException ex) {
            LOGGER.error(null, ex);
        }
    }

    private String getButtonTitle(MouseEvent event) {
        if (event == null) {
            return null;
        }
        return ((Button) event.getSource()).getText();
    }

    private void sendRequest(MouseEvent event, String msg) {
        if (event.getSource() instanceof Button) {
            ClientConnectionManager
                    .getInstance()
                    .setMessageToSend(msg);
        } else {
            LOGGER.error(ProtocolMessages.C_ERR_GUI_NOT_BUTTON.toString());
            throw new IllegalArgumentException("error in MouseEvent: entity clicked is not of Button instance ");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
