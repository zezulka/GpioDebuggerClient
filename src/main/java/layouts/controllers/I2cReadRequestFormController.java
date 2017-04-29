/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.controllers;

import core.ClientConnectionManager;
import java.net.URL;
import java.util.Map;

import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Miloslav
 */
public class I2cReadRequestFormController implements Initializable {

    @FXML
    private Button i2cRequestButton;
    @FXML
    private TextField slaveAddressField;
    @FXML
    private TextField registerAddressField;
    @FXML
    private ComboBox<String> modeList;
    @FXML
    private Label statusBar;

    private static final String ERR_SLAVE_RANGE = "Slave address (%d) out of bounds - <%d;%d>";
    private static final int SLAVE_ADDR_LOWER_BOUND = 0x03;
    private static final int SLAVE_ADDR_UPPER_BOUND = 0x77;
    
    private static String cachedSlaveAddress = "";
    private static String cachedMode = null;

    private static final Map<String, Operation> MODES = FXCollections.observableHashMap();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addAllReadModes();
        slaveAddressField.setText(cachedSlaveAddress);
        if(cachedMode != null) {
             this.modeList.getSelectionModel().select(cachedMode);
        }
        this.modeList.valueProperty().addListener(
                new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.equals("read whole range")) {
                    registerAddressField.setText("");
                    registerAddressField.setDisable(true);
                } else {
                    registerAddressField.setDisable(false);
                }
            }
        }
        );
    }

    /**
     *
     * @param evt
     */
    @FXML
    public void sendI2cRequest(MouseEvent evt) {
        Stage stage = (Stage) i2cRequestButton.getScene().getWindow();
        String msgToSend = gatherMessageFromForm();
        if (msgToSend != null) {
            ClientConnectionManager
                    .getInstance()
                    .setMessageToSend(msgToSend);
            stage.close();
        }
    }

    private void addAllReadModes() {
        MODES.put("read whole range", Operation.READALL);
        MODES.put("read from specific register", Operation.READ);
        this.modeList.setItems(FXCollections.observableArrayList(MODES.keySet()));
    }

    private String gatherMessageFromForm() {
        final char SEPARATOR = ':';
        String textFieldValue = slaveAddressField.getText().trim();
        int slaveAddress;
        StringBuilder msgBuilder = new StringBuilder("i2c");
        String selectedItem = this.modeList.getSelectionModel().getSelectedItem();
        if(selectedItem == null) {
            this.statusBar.setText("Operation has not been selected");
            return null;
        } else {
            cachedMode = selectedItem;
        }
        Operation chosenOperation = MODES.get(selectedItem);
        msgBuilder = msgBuilder.append(SEPARATOR).append(chosenOperation.toString());
        try {
            slaveAddress = Integer.parseInt(textFieldValue);
            if (slaveAddress < SLAVE_ADDR_LOWER_BOUND || slaveAddress > SLAVE_ADDR_UPPER_BOUND) {
                this.statusBar.setText(String.format(ERR_SLAVE_RANGE, slaveAddress, SLAVE_ADDR_LOWER_BOUND, SLAVE_ADDR_UPPER_BOUND));
                return null;
            }
            cachedSlaveAddress = slaveAddress + "";
            msgBuilder = msgBuilder.append(SEPARATOR).append(slaveAddress);
        } catch (NumberFormatException nfe) {
            this.statusBar.setText(String.format("Slave address must be an integer"));
            return null;
        }
        if (chosenOperation.equals(Operation.READ) && !(textFieldValue = registerAddressField.getText()).isEmpty()) {
            int registerAddress;
            try {
                registerAddress = Integer.parseInt(textFieldValue);
                msgBuilder = msgBuilder.append(SEPARATOR).append(registerAddress);
            } catch (NumberFormatException nfe) {
                this.statusBar.setText(String.format("Register address must be an integer"));
                return null;
            }
        }
        return msgBuilder.toString();
    }
}
