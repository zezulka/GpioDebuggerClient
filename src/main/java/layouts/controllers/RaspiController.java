package layouts.controllers;

import core.ClientConnectionManager;
import core.GuiEntryPoint;
import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import protocol.ProtocolMessages;

/**
 *
 * @author Miloslav Zezulka
 */
public class RaspiController implements DeviceController, Initializable {

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
    protected void createNewInterfaceForm(MouseEvent event) {
        try {
            GuiEntryPoint.getInstance().createNewI2cForm();
        } catch (IOException ex) {
            Logger.getLogger(RaspiController.class.getName()).log(Level.SEVERE, null, ex);
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
            GuiEntryPoint.writeErrorToLoggerWithoutCause(ProtocolMessages.C_ERR_GUI_NOT_BUTTON.toString());
            throw new IllegalArgumentException("error in MouseEvent: entity clicked is not of Button instance ");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
