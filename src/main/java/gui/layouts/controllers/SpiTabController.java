package gui.layouts.controllers;

import core.net.NetworkManager;
import core.util.StringConstants;
import java.net.InetAddress;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gui.userdata.SpiRequestValueObject;
import gui.userdata.UserDataUtils;
import java.time.LocalTime;
import java.util.Arrays;
import javafx.event.EventType;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public final class SpiTabController
        extends AbstractInterfaceFormController implements Initializable {

    @FXML
    private Button spiRequestButton;
    @FXML
    private ComboBox<Operation> operationList;
    @FXML
    private ComboBox<Integer> chipSelectList;
    @FXML
    private ComboBox<SpiRequestValueObject> usedRequestsComboBox;
    @FXML
    private TextField byteArrayTextfield;
    @FXML
    private ListView<String> byteArrayView;
    @FXML
    private TableView<SpiResponse> tableView;
    @FXML
    private TableColumn<SpiResponse, LocalTime> time;
    @FXML
    private TableColumn<SpiResponse, List<String>> bytes;

    private final InetAddress address;
    private static final int FIXED_CELL_SIZE = 38;
    private static final char SEPARATOR = ':';
    /**
     * Highest possible index which is reasonable to set in BCM2835's CS
     * register, in the manual referred to as "SPI Master Control and Status"
     * register.
     */
    private static final int MAX_CS_INDEX = 2;

    private static final Logger LOGGER
            = LoggerFactory.getLogger(SpiTabController.class);

    public SpiTabController(InetAddress address) {
        this.address = address;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initUsedRequestsComboBox();
        time.setCellValueFactory(new PropertyValueFactory<>("time"));
        bytes.setEditable(false);
        bytes.setCellValueFactory(new PropertyValueFactory<>("bytes"));
        bytes.setCellFactory(TextFieldTableCell
                .forTableColumn(new StringConverter<List<String>>() {
                    @Override
                    public String toString(List<String> t) {
                        if (t.size() == 1
                                && t.get(0).equals(StringConstants.WRITE_OK.toString())) {
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
                        if (string.equals(StringConstants.WRITE_OK)) {
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
        spiRequestButton.disableProperty().bind(
                super.hexValuesOnly(byteArrayTextfield).not()
        );
        super.enforceHexValuesOnly(byteArrayTextfield);
        super.addAllModes(operationList);
        addAllChipSelectIndexes();
        chipSelectList.getSelectionModel().selectFirst();
        operationList.getSelectionModel().selectFirst();
        spiRequestButton.setOnAction((event) -> {
            sendSpiRequest(event);
        });
        tableView.setFixedCellSize(FIXED_CELL_SIZE);
        tableView.setEditable(true);
    }

    private void initUsedRequestsComboBox() {
        usedRequestsComboBox.setItems(UserDataUtils.getSpiRequests());
        usedRequestsComboBox
                .setCellFactory((ListView<SpiRequestValueObject> param) -> {
                    final ListCell<SpiRequestValueObject> cell
                            = new ListCell<SpiRequestValueObject>() {
                        {
                            final int prefWidth = 150;
                            super.setPrefWidth(prefWidth);
                        }

                        @Override
                        public void updateItem(SpiRequestValueObject item,
                                boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                setText(item.toString());
                            }
                        }
                    };
                    return cell;
                });
        usedRequestsComboBox
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    chipSelectList
                            .getSelectionModel()
                            .select(newValue.getChipSelect());
                    operationList
                            .getSelectionModel()
                            .select(newValue.getOperation());
                    byteArrayTextfield.setText(newValue.getBytes());
                });
    }

    private void addAllChipSelectIndexes() {
        List<Integer> ints = new ArrayList<>();
        for (int i = 0; i < MAX_CS_INDEX; i++) {
            ints.add(i);
        }
        this.chipSelectList.setItems(FXCollections.observableArrayList(ints));
    }

    private void sendSpiRequest(ActionEvent event) {
        StringBuilder msgToSend = getMessagePrefix();
        msgToSend = msgToSend.append(byteArrayTextfield.getText());
        NetworkManager
                .setMessageToSend(address,
                        msgToSend.toString());
        SpiRequestValueObject request = getNewSpiRequestEntryFromCurrentData();
        usedRequestsComboBox.getItems().add(request);
        UserDataUtils.addNewSpiRequest(request);
    }

    private SpiRequestValueObject getNewSpiRequestEntryFromCurrentData() {
        Operation op = operationList.getSelectionModel().getSelectedItem();
        return new SpiRequestValueObject(chipSelectList.getValue(),
                op,
                byteArrayTextfield.getText());
    }

    /**
     * Generates common prefix for all SPI requests:
     * "SPI:<MODE>:0x<CHIP_INDEX>:"
     *
     * @return
     */
    @Override
    protected StringBuilder getMessagePrefix() {
        return (new StringBuilder())
                .append("SPI:")
                .append(operationList
                        .getSelectionModel().getSelectedItem().toString())
                .append(SEPARATOR)
                .append(HEXA_PREFIX)
                .append(chipSelectList.getSelectionModel().getSelectedItem())
                .append(SEPARATOR);
    }
}
