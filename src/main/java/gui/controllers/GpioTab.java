package gui.controllers;

import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import net.NetworkManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GpioTab implements Initializable {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(Device.class);

    @FXML
    private Tab gpioTab;
    @FXML
    private GridPane gpioGridPane;
    @FXML
    private RadioButton writeRadioButton;
    @FXML
    private ToggleGroup op;
    @FXML
    private RadioButton readRadioButton;

    private final InetAddress address;

    public GpioTab(InetAddress address) {
        if (address == null) {
            throw new IllegalArgumentException("address cannot be null");
        }
        this.address = address;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for (Node node : gpioGridPane.getChildren()) {
            if (node.getClass().equals(Button.class)) {
                Button btn = (Button) node;
                btn.setOnAction(this::handleGpioButton);
            }
        }
    }

    private void handleGpioButton(ActionEvent event) {
        String operation = readRadioButton.isSelected() ? "read" : "write";
        sendRequest("gpio:" + operation + ":" + getButtonTitle(event));
        LOGGER.info(String.format("GPIO request sent: pin %s, operation: %s",
                operation,
                getButtonTitle(event)));
    }

    private String getButtonTitle(ActionEvent event) {
        if (event == null) {
            return null;
        }
        return ((Button) event.getSource()).getId().split(":")[0];
    }

    private void sendRequest(String msg) {
        NetworkManager.setMessageToSend(address, msg);
    }

}
