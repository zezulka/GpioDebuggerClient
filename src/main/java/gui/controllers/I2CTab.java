package gui.controllers;

import gui.misc.Operation;
import gui.userdata.I2cRequestValueObject;
import gui.userdata.xstream.XStreamUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import net.NetworkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.response.ByteArrayResponse;

import java.net.InetAddress;
import java.net.URL;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public final class I2CTab extends AbstractTab {

    private static final char SEPARATOR = ':';
    private static final int FIXED_CELL_SIZE = 38;
    private static final Logger LOGGER
            = LoggerFactory.getLogger(I2CTab.class);
    private static final Pattern HEX_BYTE_REGEX_PATTERN
            = Pattern.compile(HEX_BYTE_REGEX);
    private final InetAddress address;
    @FXML
    private Button requestButton;
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
    private TableColumn<ByteArrayResponse, LocalTime> timeCol;
    @FXML
    private TableColumn<ByteArrayResponse, List<String>> bytesCol;

    public I2CTab(InetAddress address) {
        Objects.requireNonNull(address, "address");
        this.address = address;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.enforceHexValuesOnly(slaveAddressField);
        super.enforceNumericValuesOnly(lengthField);
        initOperationList();
        initUserRequestsComboBox();
        initTableView();
        initByteArrayTextfield();
        initRequestButton();
        setComponentsDisableProperty(getSelectedOperation());
        byteArrayView.setPlaceholder(new Label(
                "Enter byte array data in the text"
                        + " field above to see the visualization."));
    }

    private void initOperationList() {
        super.addAllModes(operationList);
        operationList.getSelectionModel().selectFirst();
        operationList.valueProperty()
                .addListener((ObservableValue<? extends Operation> obs,
                              Operation old, Operation newValue) -> {
                    if (newValue != null) {
                        setComponentsDisableProperty(newValue);
                    }
                });
    }

    private void initRequestButton() {
        requestButton.disableProperty().bind(
                Bindings.when(operationList
                        .getSelectionModel()
                        .selectedItemProperty()
                        .isEqualTo(Operation.READ))
                        .then(isHexaByte(slaveAddressField).not())
                        .otherwise(Bindings.when(
                                operationList.getSelectionModel()
                                        .selectedItemProperty()
                                        .isEqualTo(Operation.WRITE_READ))
                                .then(hexValuesOnly(byteArrayTextfield)
                                        .not()
                                        .or(isHexaByte(slaveAddressField)
                                                .not()))
                                .otherwise(hexValuesOnly(byteArrayTextfield)
                                        .not()))
        );
        requestButton.setOnAction((event) -> sendI2cRequest());
    }

    private void initTableView() {
        initTableViewColumns();
        i2cTableView.setFixedCellSize(FIXED_CELL_SIZE);
        i2cTableView.setEditable(true);
        i2cTableView.setPlaceholder(new Label("No IIC data."));
    }

    private void initByteArrayTextfield() {
        byteArrayTextfield.textProperty().addListener((ov, t, t1) -> {
            if (t1.length() % 2 == 0 || t1.length() < t.length()) {
                byteArrayView.getItems().clear();
                byteArrayView.getItems().addAll(getBytesFromUser(t1));
            }
        });
        byteArrayTextfield.disableProperty().bind(operationList
                .getSelectionModel().selectedItemProperty()
                .isEqualTo(Operation.READ));
    }

    private void initTableViewColumns() {
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        bytesCol.setEditable(false);
        bytesCol.setCellValueFactory(new PropertyValueFactory<>("bytes"));
        bytesCol.setCellFactory(TextFieldTableCell.forTableColumn(
                new AbstractTab.BytesViewStringConverter()));
    }

    private void initUserRequestsComboBox() {
        usedRequestsComboBox.setItems(XStreamUtils.getI2cRequests());
        usedRequestsComboBox
                .setCellFactory(
                        (ListView<I2cRequestValueObject> param)
                                -> new ListCell<I2cRequestValueObject>() {
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
                        });
        usedRequestsComboBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {
                    if (!newValue.getOperation().equals(Operation.WRITE)) {
                        lengthField.setText(String
                                .valueOf(newValue.getLength()));
                    }
                    if (!newValue.getOperation().equals(Operation.READ)) {
                        byteArrayTextfield.setText(newValue.getBytes());
                    }
                    operationList.getSelectionModel()
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

        return Bindings.createBooleanBinding(()
                        -> lengthField.textProperty().isEmpty().not().get(),
                lengthField.textProperty());
    }

    private void setComponentsDisableProperty(Operation op) {
        switch (op) {
            case READ: {
                values.setTextFill(Color.LIGHTGREY);
                length.setTextFill(Color.BLACK);
                lengthField.setDisable(false);
                break;
            }
            case WRITE_READ: {
                values.setTextFill(Color.BLACK);
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

    private void sendI2cRequest() {
        String msg = gatherMessageFromForm();
        if (msg != null) {
            NetworkManager.setMessageToSend(address, msg);
            I2cRequestValueObject request = getNewI2cRequestEntryFromForm();
            usedRequestsComboBox.getItems().add(request);
            XStreamUtils.addNewI2cRequest(request);
        }
    }

    private I2cRequestValueObject getNewI2cRequestEntryFromForm() {
        Operation op = getSelectedOperation();
        return new I2cRequestValueObject(op, slaveAddressField.getText(),
                getLengthRequestAttr(op), getByteArrayStr(op));
    }

    private String gatherMessageFromForm() {
        StringBuilder msgBuilder = getMessagePrefix();
        Operation op = getSelectedOperation();
        if (!op.equals(Operation.WRITE)) {
            msgBuilder = msgBuilder.append(SEPARATOR)
                    .append(lengthField.getText().trim());
        }
        if (!op.equals(Operation.READ)) {
            msgBuilder = msgBuilder.append(SEPARATOR)
                    .append(getByteArrayStr(op));
        }
        String msg = msgBuilder.toString();
        LOGGER.info(String.format("I2C request sent:\n %s", msg));
        return msg;
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

        return msgBuilder.append(SEPARATOR).append(selectedOp.name())
                .append(SEPARATOR).append(HEXA_PREFIX)
                .append(slaveAddressField.getText().trim());
    }

    private Operation getSelectedOperation() {
        return operationList.getSelectionModel().getSelectedItem();
    }
}
