package layouts.controllers;

import core.gui.App;
import core.net.ClientNetworkManager;

import java.net.URL;

import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;

import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;

import javafx.scene.Node;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

import javafx.scene.layout.GridPane;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

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
    private ComboBox<Operation> operationList;
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
    @FXML
    private TextArea i2cTextArea;

    private static int numFields;
    private static final int MAX_NUM_FIELDS = 16;

    private static final char SEPARATOR = ':';
    private static final String HEXA_PREFIX = "0x";

    private static final Logger LOGGER = LoggerFactory.getLogger(I2cRequestFormController.class);
    private static final String HEX_SEVEN_BIT_REGEX = "^(0?[3-9A-F]|[1-7][0-9A-F])$";
    private static final Pattern HEX_SEVEN_BIT_REGEX_PATTERN = Pattern.compile(HEX_SEVEN_BIT_REGEX);
    private static final String HEX_BYTE_REGEX = "^(0?[0-9A-F]|[1-9A-F][0-9A-F])$";
    private static final Pattern HEX_BYTE_REGEX_PATTERN = Pattern.compile(HEX_SEVEN_BIT_REGEX);

    /**
     * initialises the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addAllModes();
        operationList.getSelectionModel().selectFirst();
        i2cRequestButton.disableProperty().bind(
                checkLengthFieldEmpty()
                    .or(isSlaveAddressFieldHexadecimalUpmostSevenBitValue().not())
        );
        setComponentsDisableProperty(operationList.getSelectionModel().getSelectedItem());
        this.operationList.valueProperty().addListener((ObservableValue<? extends Operation> observable, Operation oldValue, Operation newValue) -> {
            if (newValue != null) {
                setComponentsDisableProperty(newValue);
            }
        });
        checkByteValuesOnly(slaveAddressField);
        enforceNumericValuesOnly(lengthField);
    }
    
    private BooleanBinding isSlaveAddressFieldHexadecimalUpmostSevenBitValue() {
         BooleanBinding binding = Bindings.createBooleanBinding(()
                -> HEX_SEVEN_BIT_REGEX_PATTERN.matcher(slaveAddressField.getText()).matches(), slaveAddressField.textProperty());
        return Bindings.when(binding).then(true).otherwise(false);
    }
    
    private BooleanBinding checkLengthFieldEmpty() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> operationList.getSelectionModel().getSelectedItem().isReadOperation(), operationList.getSelectionModel().selectedItemProperty());
        return Bindings.isEmpty(lengthField.textProperty())
                       .and(Bindings.when(binding).then(true).otherwise(false));
    }
    
    private void checkByteValuesOnly(TextField textfield) {
        textfield.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue && !textfield.getText().isEmpty()) {
                if (!textfield.getText().matches(HEX_SEVEN_BIT_REGEX)) {
                    textfield.setBackground(new Background(new BackgroundFill(Paint.valueOf("ff5555"), CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    textfield.setBackground(new Background(new BackgroundFill(Paint.valueOf("eeffee"), CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }

        });
    }
    
    private void enforceNumericValuesOnly(TextField textfield) {
        textfield.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!(newValue.matches("\\d*"))) {
                textfield.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void setComponentsDisableProperty(Operation op) {
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
        String msgToSend = gatherMessageFromForm();
        if (msgToSend != null) {
            ClientNetworkManager.setMessageToSend(App.getIpAddressFromCurrentTab(), msgToSend);
        }
    }

    private void addAllModes() {
        this.operationList
                .setItems(FXCollections.observableArrayList(Operation.values()));
    }

    private String gatherMessageFromForm() {
        StringBuilder msgBuilder = getMessagePrefix();
        if (msgBuilder == null) {
            return null;
        }
        if (operationList.getSelectionModel().getSelectedItem().isReadOperation()) {
            if (!assertTextFieldContainsDecNumericContents(lengthField, 1)) {
                ControllerUtils.showErrorDialogMessage("Len must be a positive integer");
                return null;
            }
            msgBuilder = msgBuilder.append(lengthField.getText().trim());
            LOGGER.info(String.format("I2c request form has now "
                    + "submitted the following request:\n %s"
                    + "",
                    msgBuilder.toString()));
            return msgBuilder.toString();
        }
        String valueToWrite = gatherMessageArrayFromField();
        if (valueToWrite == null || valueToWrite.isEmpty()) {
            ControllerUtils.showErrorDialogMessage("Value to write must be filled correctly");
            return null;
        }
        msgBuilder = msgBuilder.append(valueToWrite);
        return msgBuilder.toString();
    }

    private StringBuilder getMessagePrefix() {
        StringBuilder msgBuilder = new StringBuilder("i2c");
        Operation selectedOp = this.operationList.getSelectionModel().getSelectedItem();
        if (selectedOp == null) {
            ControllerUtils.showErrorDialogMessage("Operation has not been selected");
            return null;
        }
        if (!assertTextFieldContainsHexNumericContents(slaveAddressField, 0)) {
            ControllerUtils.showErrorDialogMessage("Slave address must be a positive integer");
            return null;
        }
        return msgBuilder
                .append(SEPARATOR)
                .append(selectedOp.toString())
                .append(SEPARATOR)
                .append(HEXA_PREFIX)
                .append(slaveAddressField.getText().trim())
                .append(SEPARATOR);
    }

    private boolean assertTextFieldContainsHexNumericContents(TextField textInput, int lowBound) {
        return assertTextFieldContainsNumericContents(textInput, lowBound, 16);
    }

    private boolean assertTextFieldContainsDecNumericContents(TextField textInput, int lowBound) {
        return assertTextFieldContainsNumericContents(textInput, lowBound, 10);
    }

    private boolean assertTextFieldContainsNumericContents(TextInputControl textInput, int lowBound, int radix) {
        try {
            if (textInput == null) {
                return false;
            }
            String result = textInput.getText();
            if (result == null) {
                return false;
            }
            return Short.parseShort(result.trim(), radix) >= lowBound;
        } catch (NumberFormatException nfe) {
            return false;
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
            ControllerUtils.showErrorDialogMessage(String.format("Maximum number of rows is %d", MAX_NUM_FIELDS));
            return;
        }

        int size = textFieldGridPane.getChildren().size();
        TextField tf = new TextField();
        tf.setMaxHeight(20.0);
        tf.setMaxWidth(100.0);

        textFieldGridPane.add(tf, numFields % 2 == 1 ? 1 : 0, size - (numFields % 2 == 1 ? 1 : 0));
        numFields++;
    }

    @FXML
    private void removeLastTextField(MouseEvent event) {
        int index = textFieldGridPane.getChildren().size() - 1;
        if (index < 0) {
            return;
        }
        textFieldGridPane.getChildren().remove(index);
        numFields--;
    }
}
