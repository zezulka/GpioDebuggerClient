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
 * @author Miloslav Zezulka
 */
public class I2cRequestFormController implements Initializable {

    @FXML
    private Button i2cRequestButton;
    @FXML
    private TextField slaveAddressField;
    @FXML
    private TextField registerAddressToField;
    @FXML
    private TextField registerAddressFromField;
    @FXML
    private TextField writeValue;
    @FXML
    private ComboBox<String> modeList;
    @FXML
    private Label statusBar;

    private static final String ERR_SLAVE_RANGE = "Slave address (%d) out of bounds - <%d;%d>";
    private static final int SLAVE_ADDR_LOWER_BOUND = 0x03;
    private static final int SLAVE_ADDR_UPPER_BOUND = 0x77;

    private static String cachedSlaveAddress = "";
    private static Operation cachedOp = null;

    private static final Map<String, Operation> MODES = FXCollections.observableHashMap();

    /**
     * initialises the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addAllModes();
        slaveAddressField.setText(cachedSlaveAddress);
        if (cachedOp != null) {
            this.modeList.getSelectionModel().select(cachedOp.getOp());
            initTextFieldDisableProperty(cachedOp);
        }
        this.modeList.valueProperty().addListener(
                new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Operation op = MODES.get(newValue);
                if (op != null) {
                    initTextFieldDisableProperty(op);
                }
            }
        }
        );
    }

    private void initTextFieldDisableProperty(Operation op) {
        switch (op) {
            case READ: {
                registerAddressToField.setDisable(true);
                writeValue.setDisable(true);
                break;
            }
            case READRANGE: {
                registerAddressToField.setDisable(false);
                writeValue.setDisable(true);
                break;
            }
            case WRITE: {
                registerAddressToField.setDisable(true);
                writeValue.setDisable(false);
                break;
            }
            case WRITERANGE: {
                registerAddressToField.setDisable(false);
                writeValue.setDisable(false);
                break;
            }
        }
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

    private void addAllModes() {
        for (Operation op : Operation.values()) {
            MODES.put(op.getOp(), op);
        }
        this.modeList.setItems(FXCollections.observableArrayList(MODES.keySet()));
    }

    private String gatherMessageFromForm() {
        final char SEPARATOR = ':';
        String textFieldValue = slaveAddressField.getText().trim();
        int slaveAddress;
        StringBuilder msgBuilder = new StringBuilder("i2c");
        Operation selectedOp = MODES.get(this.modeList.getSelectionModel().getSelectedItem());
        if (selectedOp == null) {
            this.statusBar.setText("Operation has not been selected");
            return null;
        } else {
            cachedOp = selectedOp;
        }
        msgBuilder = msgBuilder.append(SEPARATOR).append(selectedOp.toString());
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
        if (!(textFieldValue = registerAddressFromField.getText()).isEmpty()) {
            int registerAddress;
            try {
                registerAddress = Integer.parseInt(textFieldValue);
                msgBuilder = msgBuilder.append(SEPARATOR).append(registerAddress);
            } catch (NumberFormatException nfe) {
                this.statusBar.setText(String.format("Register address must be an integer"));
                return null;
            }
            if (Operation.isReadOperation(selectedOp)) {
                return msgBuilder.toString();
            }
            String valueToWrite = writeValue.getText();
            if (valueToWrite == null || valueToWrite.isEmpty()) {
                this.statusBar.setText(String.format("Value to write must be filled"));
            }
            msgBuilder.append(SEPARATOR).append(valueToWrite);
            System.out.println(msgBuilder.toString());
            return msgBuilder.toString();
        }
        this.statusBar.setText(String.format("Register address must be filled"));
        return null;
    }
}
