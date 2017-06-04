package layouts.controllers;

import core.ClientConnectionManager;

import java.net.URL;

import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.Node;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

import javafx.scene.input.MouseEvent;

import javafx.scene.layout.GridPane;

import javafx.scene.paint.Color;

import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private TextField registerAddressFromField;
    @FXML
    private ComboBox<String> modeList;
    @FXML
    private GridPane textFieldGridPane;
    @FXML
    private Button addFieldButton;
    @FXML
    private Button removeFieldButton;
    @FXML
    private TextField lengthField;
    @FXML
    private Label values;
    @FXML
    private Label length;

    private static int numFields;
    private static final int MAX_NUM_FIELDS = 16;

    private static final char SEPARATOR = ':';
    private static final String HEXA_PREFIX = "0x";

    private static final Map<String, Operation> MODES = FXCollections.observableHashMap();
    private static final Logger LOGGER = LoggerFactory.getLogger(I2cRequestFormController.class);

    /**
     * initialises the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addAllModes();
        modeList.getSelectionModel().selectFirst();
        configTextFieldDisableProperty(MODES.get(modeList.getSelectionModel().getSelectedItem()));
        this.modeList.valueProperty().addListener(
                new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Operation op = MODES.get(newValue);
                if (op != null) {
                    configTextFieldDisableProperty(op);
                }
            }
        }
        );
    }

    private void configTextFieldDisableProperty(Operation op) {
        switch (op) {
            case READ: {
                values.setTextFill(Color.LIGHTGREY);
                length.setTextFill(Color.BLACK);
                lengthField.setDisable(false);
                removeFieldButton.setDisable(true);
                addFieldButton.setDisable(true);
                break;
            }
            case WRITE: {
                values.setTextFill(Color.BLACK);
                length.setTextFill(Color.LIGHTGREY);
                lengthField.setDisable(true);
                removeFieldButton.setDisable(false);
                addFieldButton.setDisable(false);
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

    private static void showErrorDialogMessage(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("ERROR MESSAGE");
        alert.setHeaderText("There has been an error processing the user input:");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String gatherMessageFromForm() {
        StringBuilder msgBuilder = new StringBuilder("i2c");
        Operation selectedOp = MODES.get(this.modeList.getSelectionModel().getSelectedItem());
        if (selectedOp == null) {
            showErrorDialogMessage("Operation has not been selected");
            return null;
        }
        String slave = getTextFieldNumericContents(slaveAddressField, 0);
        if (slave == null) {
            showErrorDialogMessage("Slave address must be a positive integer");
            return null;
        }
        String register = getTextFieldNumericContents(registerAddressFromField, 0);
        if (register == null) {
            showErrorDialogMessage("Register address must be a positive integer");
            return null;
        }
        msgBuilder = msgBuilder
                .append(SEPARATOR)
                .append(selectedOp.toString())
                .append(SEPARATOR)
                .append(slave)
                .append(SEPARATOR)
                .append(register)
                .append(SEPARATOR);

        if (Operation.isReadOperation(selectedOp)) {
            String len = getTextFieldNumericContents(lengthField, 1);
            if (len == null) {
                showErrorDialogMessage("Len must be a positive integer");
                return null;
            }
            msgBuilder = msgBuilder.append(len);
            LOGGER.info(String.format("I2c request form has now "
                    + "submitted the following request:\n %s"
                    + "",
                    msgBuilder.toString()));
            return msgBuilder.toString();
        }
        String valueToWrite = gatherMessageArrayFromField();
        if (valueToWrite == null || valueToWrite.isEmpty()) {
            showErrorDialogMessage("Value to write must be filled correctly");
            return null;
        }
        msgBuilder = msgBuilder.append(valueToWrite);
        LOGGER.info(String.format("I2c request form has now "
                + "submitted the following request:\n %s"
                + "",
                msgBuilder.toString()));
        return msgBuilder.toString();
    }

    private String getTextFieldNumericContents(TextInputControl textInput, int lowBound) {
        try {
            if (textInput == null) {
                return null;
            }
            String result = textInput.getText().trim();
            return Short.decode(result) >= lowBound ? HEXA_PREFIX + result : null;
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    private String gatherMessageArrayFromField() {
        StringBuilder resultBuilder = new StringBuilder();
        for (Iterator<Node> it = textFieldGridPane.getChildren().iterator(); it.hasNext();) {
            String t = ((TextField) it.next()).getText().trim();
            if (t == null || t.isEmpty()) {
                return null;
            }
            resultBuilder = resultBuilder.append(HEXA_PREFIX).append(t);
            if (it.hasNext()) {
                resultBuilder = resultBuilder.append(' ');
            }
        }
        return resultBuilder.toString();
    }

    @FXML
    private void addNewTextField(MouseEvent event) {
        if (numFields >= MAX_NUM_FIELDS) {
            showErrorDialogMessage(String.format("Maximum number of rows is %d", MAX_NUM_FIELDS));
            return;
        }

        int size = textFieldGridPane.getChildren().size();
        TextField tf = new TextField();
        tf.setMaxHeight(20.0);
        tf.setMaxWidth(100.0);

        textFieldGridPane.add(tf, numFields % 2 == 1 ? 1 : 0, size - (numFields % 2 == 1 ? 1 : 0));
        ++numFields;
    }

    @FXML
    private void removeLastTextField(MouseEvent event) {
        int index = textFieldGridPane.getChildren().size() - 1;
        if (index < 0) {
            return;
        }
        textFieldGridPane.getChildren().remove(index);
        --numFields;
    }
}
