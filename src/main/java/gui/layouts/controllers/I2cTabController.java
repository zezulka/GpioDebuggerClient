package gui.layouts.controllers;

import core.net.NetworkManager;
import core.util.StringConstants;
import java.net.InetAddress;

import java.net.URL;

import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import javafx.scene.paint.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gui.userdata.I2cRequestValueObject;
import gui.userdata.UserDataUtils;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public final class I2cTabController
        extends AbstractInterfaceFormController {

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
    private ComboBox<I2cRequestValueObject> usedRequestsComboBox;
    @FXML
    private TextField byteArrayTextfield;
    @FXML
    private ListView<String> byteArrayView;
    @FXML
    private TableView<ByteArrayResponse> i2cTableView;
    @FXML
    private TableColumn<ByteArrayResponse, LocalTime> time;
    @FXML
    private TableColumn<ByteArrayResponse, List<String>> bytes;

    private static final char SEPARATOR = ':';
    private static final int FIXED_CELL_SIZE = 38;
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
        time.setCellValueFactory(new PropertyValueFactory<>("time"));
        bytes.setEditable(false);
        bytes.setCellValueFactory(new PropertyValueFactory<>("bytes"));
        bytes.setCellFactory(TextFieldTableCell
                .forTableColumn(new StringConverter<List<String>>() {
                    @Override
                    public String toString(List<String> t) {
                        if (t.size() == 1 && t.get(0)
                                .equals(StringConstants.WRITE_OK.toString())) {
                            return StringConstants.WRITE_OK.toString();
                        }
                        final int hexaRadix = 16;
                        StringBuilder b = new StringBuilder();
                        for (String s : t) {
                            b.append(Integer.toHexString(Integer
                                    .parseInt(s, hexaRadix)))
                                    .append(' ');
                        }
                        return b.toString();
                    }

                    @Override
                    public List<String> fromString(String string) {
                        if (string.equals(StringConstants.WRITE_OK
                                .toString())) {
                            return Arrays.asList("WRITE REQUEST");
                        }
                        List<String> result = new ArrayList<>();
                        for (String s : string.split(" ")) {
                            result.add(s);
                        }
                        return result;
                    }
                }));
        byteArrayTextfield.textProperty().addListener((ov, t, t1) -> {
            if (t1.length() % 2 == 0 || t1.length() < t.length()) {
                byteArrayView.getItems().clear();
                byteArrayView.getItems().addAll(getBytesFromUser(t1));
            }
        });
        byteArrayTextfield.disableProperty()
                .bind(operationList
                        .getSelectionModel()
                        .selectedItemProperty()
                        .isEqualTo(Operation.READ));

        operationList.getSelectionModel().selectFirst();
        i2cRequestButton.disableProperty().bind(
                Bindings.when(operationList
                        .getSelectionModel()
                        .selectedItemProperty()
                        .isEqualTo(Operation.READ))
                        .then(isHexaByte(slaveAddressField).not())
                        .otherwise(super.hexValuesOnly(byteArrayTextfield)
                                .not())
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
        i2cTableView.setFixedCellSize(FIXED_CELL_SIZE);
        i2cTableView.setEditable(true);
        i2cTableView.setPlaceholder(new Label("No IIC data."));
        byteArrayView.setPlaceholder(new Label(
                "Enter byte array data in the text"
                + " field above to see the visualization."));
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
                    if (newValue.getOperation().equals(Operation.READ)) {
                        lengthField.setText(String
                                .valueOf(newValue.getLength()));
                        byteArrayTextfield.setText("");
                    } else {
                        byteArrayTextfield.setText(newValue.getBytes());
                        lengthField.setText("");
                    }
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
        return binding.and(checkLengthFieldNonEmpty());
    }

    private BooleanBinding checkLengthFieldNonEmpty() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> lengthField.textProperty().isEmpty().not().get(),
                lengthField.textProperty());

        return binding;
    }

    private void setComponentsDisableProperty(Operation op) {
        switch (op) {
            case READ: {
                values.setTextFill(Color.LIGHTGREY);
                length.setTextFill(Color.BLACK);
                lengthField.setDisable(false);
                break;
            }
            case WRITE_READ:
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
            NetworkManager.setMessageToSend(address, msg);
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
        if (op.equals(Operation.READ)) {
            msgBuilder = msgBuilder.append(lengthField.getText().trim());
            LOGGER.info(String.format("I2c request form has now "
                    + "submitted the following request:\n %s",
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
        if (!op.equals(Operation.READ)) {
            return getByteArrayStr(op).length() / 2;
        }
        return Integer.parseUnsignedInt(lengthField.getText());
    }

    private String getByteArrayStr(Operation op) {
        return op.equals(Operation.READ) ? "" : byteArrayTextfield.getText();
    }

    @Override
    protected StringBuilder getMessagePrefix() {
        StringBuilder msgBuilder = new StringBuilder("i2c");
        Operation selectedOp = getSelectedOperation();

        return msgBuilder
                .append(SEPARATOR)
                .append(selectedOp.name())
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
