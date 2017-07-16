package layouts.controllers;

import core.gui.App;
import core.net.ClientNetworkManager;

import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.RadioButton;

import java.util.ResourceBundle;
import javafx.scene.control.Tab;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.InterruptManager;

/**
 *
 * @author Miloslav Zezulka
 */
public class RaspiController implements DeviceController, Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaspiController.class);

    @FXML
    private RadioButton readRadioButton;
    @FXML
    private Tab raspiTab;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        raspiTab.setOnCloseRequest((event) -> {
            if (ControllerUtils.showConfirmationDialogMessage("Are you sure that you want to disconnect from this device?")) {
                ClientNetworkManager.disconnect(App.getIpAddressFromCurrentTab());
                InterruptManager.clearAllInterruptListeners();
                raspiTab.getTabPane().getTabs().remove(raspiTab);
            }
            event.consume();
        });
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

    private String getButtonTitle(InputEvent event) {
        if (event == null) {
            return null;
        }
        return ((Button) event.getSource()).getId();
    }

    private void sendRequest(InputEvent event, String msg) {
        ClientNetworkManager.setMessageToSend(App.getIpAddressFromCurrentTab(), msg);
    }
}
