package gui.layouts.controllers;
import core.net.NetworkManager;
import java.net.InetAddress;

import java.net.URL;

import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


import javafx.scene.paint.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gui.userdata.I2cRequestValueObject;
import gui.userdata.UserDataUtils;

/**
 * FXML Controller class
 *
 * @author Miloslav Zezulka
 */
public final class I2cTabController
        extends AbstractInterfaceFormController implements Initializable {

    @FXML
    private Button i2cRequestButton;
    @FXML
    private TextField slaveAddressField;
    @FXML
    private ComboBox<Operation> operationList;
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
    private final InetAddress address;
    private static final Logger LOGGER
            = LoggerFactory.getLogger(I2cTabController.class);

    private static final Pattern HEX_BYTE_REGEX_PATTERN
            = Pattern.compile(HEX_BYTE_REGEX);

    public I2cTabController(InetAddress address) {
        this.address = address;
    }

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
        byteArrayTextfield.disableProperty()
                .bind(operationList
                        .getSelectionModel()
                        .selectedItemProperty()
                        .isNotEqualTo(Operation.WRITE));

        operationList.getSelectionModel().selectFirst();
        i2cRequestButton.disableProperty().bind(
                checkLengthFieldEmpty()
                        .or(isHexaByte(slaveAddressField).not())
                        .or(Bindings.when(operationList
                                .getSelectionModel()
                                .selectedItemProperty()
                                .isEqualTo(Operation.WRITE))
                                .then(super.hexValuesOnly(byteArrayTextfield)
                                        .not())
                                .otherwise(false))
        );
        i2cRequestButton.setOnAction((event) -> {
            sendI2cRequest(event);
        });
        setComponentsDisableProperty(getSelectedOperation());
        operationList
                .valueProperty()
                .addListener((ObservableValue<? extends Operation> obs,
                        Operation old, Operation newValue) -> {
                    if (newValue != null) {
                        setComponentsDisableProperty(newValue);
                    }
                });
        super.enforceHexValuesOnly(slaveAddressField);
        super.enforceNumericValuesOnly(lengthField);
    }

    private void initUserRequestsComboBox() {
        usedRequestsComboBox.setItems(UserDataUtils.getI2cRequests());
        usedRequestsComboBox
                .setCellFactory((ListView<I2cRequestValueObject> param) -> {
                    final ListCell<I2cRequestValueObject> cell
                            = new ListCell<I2cRequestValueObject>() {
                        {
                            final int prefWidth = 150;
                            super.setPrefWidth(prefWidth);
                        }

                        @Override
                        public void updateItem(I2cRequestValueObject item,
                                boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                setText(item.toString());
                            }
                        }
                    };
                    return cell;
                });
        usedRequestsComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {
                    lengthField.setText(String.valueOf(newValue.getLength()));
                    operationList
                            .getSelectionModel()
                            .select(newValue.getOperation());
                    slaveAddressField.setText(newValue.getSlaveAddress());
                });
    }

    private BooleanBinding isHexaByte(TextField textfield) {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> HEX_BYTE_REGEX_PATTERN
                        .matcher(textfield.getText()).matches(),
                textfield.textProperty());
        return Bindings.when(binding).then(true).otherwise(false);
    }

    private BooleanBinding checkLengthFieldEmpty() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> getSelectedOperation().isReadOperation(),
                operationList.getSelectionModel().selectedItemProperty());

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
            default:
                throw new RuntimeException("Illegal Operation");
        }
    }

    private void sendI2cRequest(ActionEvent evt) {
        String msg = gatherMessageFromForm();
        if (msg != null) {
            NetworkManager
                    .setMessageToSend(address, msg);
            I2cRequestValueObject request = getNewI2cRequestEntryFromForm();
            usedRequestsComboBox.getItems().add(request);
            UserDataUtils.addNewI2cRequest(request);
        }
    }

    private I2cRequestValueObject getNewI2cRequestEntryFromForm() {
        Operation op = getSelectedOperation();
        return new I2cRequestValueObject(op, slaveAddressField.getText(),
                getLengthRequestAttr(op),
                getByteArrayStr(op));
    }

    private String gatherMessageFromForm() {
        StringBuilder msgBuilder = getMessagePrefix();
        Operation op = getSelectedOperation();
        if (op.isReadOperation()) {
            msgBuilder = msgBuilder.append(lengthField.getText().trim());
            LOGGER.info(String.format("I2c request form has now "
                    + "submitted the following request:\n %s"
                    + "",
                    msgBuilder.toString()));
            return msgBuilder.toString();
        }
        msgBuilder = msgBuilder.append(getByteArrayStr(op));
        return msgBuilder.toString();
    }

    /**
     * Semantics of length in this case is the length attribute of request, not
     * the length of request itself.
     *
     * @return length of either bytes being sent or length specified by user
     */
    private int getLengthRequestAttr(Operation op) {
        if (op.isWriteOperation()) {
            return getByteArrayStr(op).length() / 2;
        }
        return Integer.parseUnsignedInt(lengthField.getText());
    }

    private String getByteArrayStr(Operation op) {
        return op.isReadOperation() ? "" : byteArrayTextfield.getText();
    }

    @Override
    protected StringBuilder getMessagePrefix() {
        StringBuilder msgBuilder = new StringBuilder("i2c");
        Operation selectedOp = getSelectedOperation();

        return msgBuilder
                .append(SEPARATOR)
                .append(selectedOp.toString())
                .append(SEPARATOR)
                .append(HEXA_PREFIX)
                .append(slaveAddressField.getText().trim())
                .append(SEPARATOR);
    }

    private Operation getSelectedOperation() {
        return operationList
                .getSelectionModel()
                .getSelectedItem();
    }
}
