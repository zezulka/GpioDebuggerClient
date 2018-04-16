package gui.controllers;

import gui.misc.Operation;
import gui.userdata.SpiRequestValueObject;
import gui.userdata.xstream.XStreamUtils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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
import net.NetworkManager;
import protocol.response.ByteArrayResponse;

import java.net.InetAddress;
import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public final class SpiTab
        extends AbstractTab {

    private static final int FIXED_CELL_SIZE = 38;
    private static final char SEPARATOR = ':';
    /**
     * Highest possible index which is reasonable to set in BCM2835's CS
     * register, in the manual referred to as "SPI Master Control and Status"
     * register.
     */
    private static final int MAX_CS_INDEX = 2;
    private final InetAddress address;
    @FXML
    private Button requestButton;
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
    private TableView<ByteArrayResponse> spiTableView;
    @FXML
    private TableColumn<ByteArrayResponse, LocalTime> timeCol;
    @FXML
    private TableColumn<ByteArrayResponse, List<String>> bytesCol;

    public SpiTab(InetAddress address) {
        this.address = address;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.enforceHexValuesOnly(byteArrayTextfield);
        initUsedRequestsComboBox();
        initTableView();
        initRequestButton();
        initOperationList();
        initChipSelectList();
        byteArrayView.setPlaceholder(new Label(
                "Enter byte array data in the text"
                        + " field above to see the visualization."));
        byteArrayTextfield.textProperty().addListener((ov, t, t1) -> {
            if (t1.length() % 2 == 0 || t1.length() < t.length()) {
                byteArrayView.getItems().clear();
                byteArrayView.getItems().addAll(getBytesFromUser(t1));
            }
        });
    }

    private void initChipSelectList() {
        addAllChipSelectIndexes();
        chipSelectList.getSelectionModel().selectFirst();
    }

    private void addAllChipSelectIndexes() {
        List<Integer> ints = new ArrayList<>();
        for (int i = 0; i < MAX_CS_INDEX; i++) {
            ints.add(i);
        }
        this.chipSelectList.setItems(FXCollections.observableArrayList(ints));
    }

    private void initOperationList() {
        super.addAllModes(operationList);
        operationList.getSelectionModel().selectFirst();
    }

    private void initRequestButton() {
        requestButton.disableProperty().bind(
                super.hexValuesOnly(byteArrayTextfield).not());
        requestButton.setOnAction(this::sendSpiRequest);
    }

    private void initTableView() {
        initTableViewColumns();
        spiTableView.setFixedCellSize(FIXED_CELL_SIZE);
        spiTableView.setEditable(true);
        spiTableView.setPlaceholder(new Label("No SPI data."));
    }

    private void initTableViewColumns() {
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        bytesCol.setEditable(false);
        bytesCol.setCellValueFactory(new PropertyValueFactory<>("bytes"));
        bytesCol.setCellFactory(TextFieldTableCell.forTableColumn(
                new AbstractTab.BytesViewStringConverter()));
    }

    private void initUsedRequestsComboBox() {
        usedRequestsComboBox.setItems(XStreamUtils.getSpiRequests());
        usedRequestsComboBox
                .setCellFactory((ListView<SpiRequestValueObject> param)
                        -> new ListCell<SpiRequestValueObject>() {
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
                });
        usedRequestsComboBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    chipSelectList.getSelectionModel()
                            .select(newValue.getChipSelect());
                    operationList.getSelectionModel()
                            .select(newValue.getOperation());
                    byteArrayTextfield.setText(newValue.getBytes());
                });
    }

    private void sendSpiRequest(ActionEvent ignored) {
        StringBuilder msgToSend = getMessagePrefix();
        msgToSend = msgToSend.append(byteArrayTextfield.getText());
        NetworkManager.setMessageToSend(address, msgToSend.toString());
        SpiRequestValueObject request = getNewSpiRequestEntryFromCurrentData();
        if (!usedRequestsComboBox.getItems().contains(request)) {
            usedRequestsComboBox.getItems().add(request);
        }
        XStreamUtils.addNewSpiRequest(request);
    }

    private SpiRequestValueObject getNewSpiRequestEntryFromCurrentData() {
        Operation op = operationList.getSelectionModel().getSelectedItem();
        return new SpiRequestValueObject(chipSelectList.getValue(), op,
                byteArrayTextfield.getText());
    }

    /**
     * Generates common prefix for all SPI requests:
     * "SPI:<MODE>:0x<CHIP_INDEX>:"
     */
    @Override
    protected StringBuilder getMessagePrefix() {
        return (new StringBuilder()).append("SPI:").append(operationList
                .getSelectionModel().getSelectedItem().name())
                .append(SEPARATOR).append(HEXA_PREFIX)
                .append(chipSelectList.getSelectionModel().getSelectedItem())
                .append(SEPARATOR);
    }
}
