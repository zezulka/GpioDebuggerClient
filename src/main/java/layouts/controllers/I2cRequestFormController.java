package layouts.controllers;

import core.gui.App;
import core.net.ClientNetworkManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;

import javafx.scene.Node;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

import javafx.scene.layout.GridPane;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userdata.I2cRequestValueObject;
import userdata.UserDataUtils;

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
    @FXML
    private ComboBox<I2cRequestValueObject> usedRequestsComboBox;

    private final IntegerProperty numFields = new SimpleIntegerProperty(0);
    private static final int MAX_NUM_FIELDS = 16;

    private static final char SEPARATOR = ':';
    private static final String HEXA_PREFIX = "0x";

    private static final Logger LOGGER = LoggerFactory.getLogger(I2cRequestFormController.class);
    private static final String HEX_BYTE_REGEX = "^(0?[0-9A-Fa-f]|[1-9A-Fa-f][0-9A-Fa-f])$";
    private static final Pattern HEX_BYTE_REGEX_PATTERN = Pattern.compile(HEX_BYTE_REGEX);

    /**
     * initialises the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addAllModes();
        usedRequestsComboBox.setItems(FXCollections.observableArrayList(UserDataUtils.getI2cRequests()));
        usedRequestsComboBox.setCellFactory((ListView<I2cRequestValueObject> param) -> {
            final ListCell<I2cRequestValueObject> cell = new ListCell<I2cRequestValueObject>() {
                {
                    super.setPrefWidth(150);
                }
                
                @Override public void updateItem(I2cRequestValueObject item,
                        boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.toString());
                    }
                }
            };
            return cell;
        });
        usedRequestsComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            lengthField.setText(String.valueOf(newValue.getLength()));
            operationList.getSelectionModel().select(newValue.getOperation());
            slaveAddressField.setText(newValue.getSlaveAddress());
            for(int i = 0; i < newValue.getBytes().size(); i++) {
                ((TextField)textFieldGridPane.getChildren().get(i)).setText(newValue.getBytes().get(i));
                ((TextField)textFieldGridPane.getChildren().get(i)).setDisable(false);
            }
            numFields.set(newValue.getBytes().size());
        });
        textFieldGridPane.disableProperty().bind(operationList.getSelectionModel().selectedItemProperty().isNotEqualTo(Operation.WRITE));
        operationList.getSelectionModel().selectFirst();
        i2cRequestButton.disableProperty().bind(
                checkLengthFieldEmpty()
                        .or(isHexaByte(slaveAddressField).not())
                        .or(checkGridPaneChildrenOutOfBounds())
                        .or(createDataTextFields().not())
        );
        addFieldButton.disableProperty().bind(chechGridPaneChildrenOutOfBoundsHi()
                .or(operationList.getSelectionModel().selectedItemProperty().isEqualTo(Operation.READ)));
        removeFieldButton.disableProperty().bind(chechGridPaneChildrenOutOfBoundsLo()
                .or(operationList.getSelectionModel().selectedItemProperty().isEqualTo(Operation.READ)));
        setComponentsDisableProperty(operationList.getSelectionModel().getSelectedItem());
        this.operationList.valueProperty().addListener((ObservableValue<? extends Operation> observable, Operation oldValue, Operation newValue) -> {
            if (newValue != null) {
                setComponentsDisableProperty(newValue);
            }
        });
        assertThatContainsByteValuesOnly(slaveAddressField);
        enforceNumericValuesOnly(lengthField);
    }

    private BooleanBinding createDataTextFields() {
        BooleanBinding bind = Bindings.createBooleanBinding(() -> {return true;});
        for (int i = 0; i < MAX_NUM_FIELDS; i++) {
            TextField newField = new TextField();
            newField.setDisable(true);
            newField.setPrefSize(50, 35);
            newField.setMaxSize(50, 35);
            bind = Bindings.when(newField.disabledProperty()).then(true).otherwise(Bindings.isNotEmpty(newField.textProperty())).and(bind);
            enforceHexValuesOnly(newField);
            textFieldGridPane.add(newField, i % 4, i / 4);
        }
        return bind;
    }

    private BooleanBinding isHexaByte(TextField textfield) {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> HEX_BYTE_REGEX_PATTERN.matcher(textfield.getText()).matches(), textfield.textProperty());
        return Bindings.when(binding).then(true).otherwise(false);
    }

    private BooleanBinding checkLengthFieldEmpty() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> operationList.getSelectionModel().getSelectedItem().isReadOperation(), operationList.getSelectionModel().selectedItemProperty());
        return Bindings.isEmpty(lengthField.textProperty())
                .and(Bindings.when(binding).then(true).otherwise(false));
    }

    private void assertThatContainsByteValuesOnly(TextField textfield) {
        textfield.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) {
                if (textfield.getText().matches(HEX_BYTE_REGEX) && !textfield.getText().isEmpty()) {
                    textfield.setBackground(new Background(new BackgroundFill(Paint.valueOf("eeffee"), CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    textfield.setBackground(new Background(new BackgroundFill(Paint.valueOf("ff5555"), CornerRadii.EMPTY, Insets.EMPTY)));
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
    
    private void enforceHexValuesOnly(TextField textfield) {
        textfield.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if(newValue.equals("")) {
                textfield.setText("");
                return;
            }
            if (!(newValue.matches(HEX_BYTE_REGEX))) {
                textfield.setText(oldValue);
            }
        });
    }

    private void setComponentsDisableProperty(Operation op) {
        switch (op) {
            case READ: {
                values.setTextFill(Color.LIGHTGREY);
                length.setTextFill(Color.BLACK);
                lengthField.setDisable(false);
                break;
            }
            case WRITE: {
                values.setTextFill(Color.BLACK);
                length.setTextFill(Color.LIGHTGREY);
                lengthField.setDisable(true);
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
            I2cRequestValueObject request = getNewI2cRequestEntryFromCurrentData();
            usedRequestsComboBox.getItems().add(request);
            UserDataUtils.addNewI2cRequest(request);
        }
    }
    
    private I2cRequestValueObject getNewI2cRequestEntryFromCurrentData() {
        Operation op = operationList.getSelectionModel().getSelectedItem();
        return new I2cRequestValueObject(op, slaveAddressField.getText(), 
                                         op.isWriteOperation() ? textFieldGridPane.getChildren().filtered((textfield) -> !textfield.isDisabled()).size() : Integer.parseUnsignedInt(lengthField.getText()), 
                                         op.isReadOperation() ? Collections.EMPTY_LIST : getBytes());
    }
    
    private List<String> getBytes() {
        List<Node> enabledNodes = textFieldGridPane.getChildren().filtered((textfield) -> !textfield.isDisabled());
        List<String> resultDataArray = new ArrayList<>();
        for(Node node : enabledNodes) {
            resultDataArray.add(((TextField)node).getText());
        }
        return resultDataArray;
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
        return msgBuilder
                .append(SEPARATOR)
                .append(selectedOp.toString())
                .append(SEPARATOR)
                .append(HEXA_PREFIX)
                .append(slaveAddressField.getText().trim())
                .append(SEPARATOR);
    }

    private String gatherMessageArrayFromField() {
        StringBuilder resultBuilder = new StringBuilder();
        for (Iterator<Node> it = textFieldGridPane.getChildren().iterator(); it.hasNext();) {
            String t = ((TextField) it.next()).getText().trim();
            if (t == null || t.isEmpty()) {
                return resultBuilder.toString();
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
        ((TextField) textFieldGridPane.getChildren().get(numFields.get())).setBackground(new Background(new BackgroundFill(Paint.valueOf("FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));
        ((TextField) textFieldGridPane.getChildren().get(numFields.get())).setStyle("");

        textFieldGridPane.getChildren().get(numFields.get()).setDisable(false);
        numFields.set(numFields.get() + 1);
    }

    @FXML
    private void removeLastTextField(MouseEvent event) {
        textFieldGridPane.getChildren().get(numFields.get() - 1).setDisable(true);
        ((TextField)textFieldGridPane.getChildren().get(numFields.get() - 1)).setText("");
        numFields.set(numFields.get() - 1);
    }

    private BooleanBinding checkGridPaneChildrenOutOfBounds() {

        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> numFields.lessThanOrEqualTo(0)
                        .or(numFields.greaterThan(MAX_NUM_FIELDS)).get(), numFields)
                .and(operationList.getSelectionModel().selectedItemProperty().isEqualTo(Operation.WRITE));
        return Bindings.when(binding).then(true).otherwise(false);
    }

    private BooleanBinding chechGridPaneChildrenOutOfBoundsLo() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> numFields.lessThanOrEqualTo(0).get(), numFields);
        return Bindings.when(binding).then(true).otherwise(false);
    }

    private BooleanBinding chechGridPaneChildrenOutOfBoundsHi() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> numFields.greaterThanOrEqualTo(MAX_NUM_FIELDS).get(), numFields);
        return Bindings.when(binding).then(true).otherwise(false);
    }
}
