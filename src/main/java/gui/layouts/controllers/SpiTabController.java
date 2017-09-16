package gui.layouts.controllers;

import core.net.NetworkManager;
import java.net.InetAddress;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gui.userdata.SpiRequestValueObject;
import gui.userdata.UserDataUtils;

public final class SpiTabController
        extends AbstractInterfaceFormController implements Initializable {

    @FXML
    private Button spiRequestButton;
    @FXML
    private ComboBox<Operation> operationList;
    @FXML
    private ComboBox<Integer> chipSelectList;
    @FXML
    private TextArea spiTextArea;
    @FXML
    private ComboBox<SpiRequestValueObject> usedRequestsComboBox;
    @FXML
    private TextField byteArrayTextfield;

    private final InetAddress address;

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
