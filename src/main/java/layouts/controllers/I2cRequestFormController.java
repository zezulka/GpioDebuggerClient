package layouts.controllers;

import core.gui.App;
import core.net.ClientNetworkManager;

import java.net.URL;
import java.util.Collections;

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

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import javafx.scene.paint.Color;
import static layouts.controllers.AbstractInterfaceFormController.HEXA_PREFIX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import userdata.I2cRequestValueObject;
import userdata.UserDataUtils;

/**
 * FXML Controller class
 *
 * @author Miloslav Zezulka
 */
public class I2cRequestFormController extends AbstractInterfaceFormController implements Initializable {

    @FXML
    private Button i2cRequestButton;
    @FXML
    private TextField slaveAddressField;
    @FXML
    private ComboBox<Operation> operationList;
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
    @FXML
    private TextField byteArrayTextfield;
    
    private static final char SEPARATOR = ':';

    private static final Logger LOGGER = LoggerFactory.getLogger(I2cRequestFormController.class);
    private static final Pattern HEX_BYTE_REGEX_PATTERN = Pattern.compile(HEX_BYTE_REGEX);
    

    /**
     * initialises the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.addAllModes(operationList);
        initUserRequestsComboBox();
        byteArrayTextfield.disableProperty().bind(operationList.getSelectionModel().selectedItemProperty().isNotEqualTo(Operation.WRITE));
        operationList.getSelectionModel().selectFirst();
        i2cRequestButton.disableProperty().bind(
                checkLengthFieldEmpty()
                        .or(isHexaByte(slaveAddressField).not())
                        .or(Bindings.when(operationList.getSelectionModel().selectedItemProperty().isEqualTo(Operation.WRITE))
                                .then(super.createHexValuesOnlyBinding(byteArrayTextfield).not())
                                .otherwise(false))
        );
        setComponentsDisableProperty(operationList.getSelectionModel().getSelectedItem());
        this.operationList.valueProperty().addListener((ObservableValue<? extends Operation> observable, Operation oldValue, Operation newValue) -> {
            if (newValue != null) {
                setComponentsDisableProperty(newValue);
            }
        });
        super.enforceHexValuesOnly(slaveAddressField);
        super.enforceNumericValuesOnly(lengthField);
    }
    
    private void initUserRequestsComboBox() {
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
        });
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
                                         op.isWriteOperation() ? byteArrayTextfield.getText().length()/2 : Integer.parseUnsignedInt(lengthField.getText()), 
                                         op.isReadOperation() ? "" : byteArrayTextfield.getText());
    }
    
    private String gatherMessageFromForm() {
        StringBuilder msgBuilder = getMessagePrefix();
        if (operationList.getSelectionModel().getSelectedItem().isReadOperation()) {
            msgBuilder = msgBuilder.append(lengthField.getText().trim());
            LOGGER.info(String.format("I2c request form has now "
                    + "submitted the following request:\n %s"
                    + "",
                    msgBuilder.toString()));
            return msgBuilder.toString();
        }
        msgBuilder = msgBuilder.append(byteArrayTextfield.getText());
        return msgBuilder.toString();
    }

    @Override
    protected StringBuilder getMessagePrefix() {
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
}
