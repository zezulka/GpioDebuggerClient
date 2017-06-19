package layouts.controllers;

import core.ClientConnectionManager;

import java.io.IOException;

import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.RadioButton;

import java.util.ResourceBundle;

import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    protected void mouseClickedHandler(MouseEvent event) {
        String op = readRadioButton.isSelected() ? "read" : "write";
        sendRequest(event, "gpio:" + op + ":" + getButtonTitle(event));
        LOGGER.info(String.format("GPIO request has been sent : pin %s, operation : %s", op, getButtonTitle(event)));
    }

    @FXML
    protected void keyPressedHandler(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            String op = readRadioButton.isSelected() ? "read" : "write";
            sendRequest(event, "gpio:" + op + ":" + getButtonTitle(event));
            LOGGER.info(String.format("GPIO request has been sent : pin %s, operation : %s", op, getButtonTitle(event)));
        }
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

    private String getButtonTitle(InputEvent event) {
        if (event == null) {
            return null;
        }
        return ((Button) event.getSource()).getText();
    }

    private void sendRequest(InputEvent event, String msg) {
        if (event.getSource() instanceof Button) {
            ClientConnectionManager
                    .getInstance()
                    .setMessageToSend(msg);
        } else {
            LOGGER.error(ProtocolMessages.C_ERR_GUI_NOT_BUTTON.toString());
            throw new IllegalArgumentException("error in MouseEvent: entity clicked is not of Button instance ");
        }
    }
}
