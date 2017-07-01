package layouts.controllers;

import core.gui.App;
import core.net.ClientNetworkManager;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javafx.scene.input.MouseEvent;

import javafx.scene.layout.GridPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userdata.SpiRequestValueObject;
import userdata.UserDataUtils;

/**
 * FXML Controller class
 *
 * @author miloslav
 */
public class SpiRequestFormController extends AbstractInterfaceFormController implements Initializable {

    @FXML
    private Button spiRequestButton;
    @FXML
    private ComboBox<Operation> operationList;
    @FXML
    private ComboBox<Integer> chipSelectList;
    @FXML
    private GridPane textFieldGridPaneSpi;
    @FXML
    private TextArea spiTextArea;
    @FXML
    private Button addFieldButton;
    @FXML
    private Button removeFieldButton;
    @FXML
    private ComboBox<SpiRequestValueObject> usedRequestsComboBox;

    private final IntegerProperty numFields = new SimpleIntegerProperty(0);
    private static final char SEPARATOR = ':';
    /**
     * Highest possible index which is reasonable to set in BCM2835's CS
     * register, in the manual referred to as "SPI Master Control and Status"
     * register.
     */
    private static final int MAX_CS_INDEX = 2;

    private static final Logger LOGGER = LoggerFactory.getLogger(SpiRequestFormController.class);

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initUsedRequestsComboBox();
        spiRequestButton.disableProperty().bind(
                checkGridPaneChildrenOutOfBounds()
                        .or(createDataTextFields(textFieldGridPaneSpi).not())
        );
        addFieldButton.disableProperty().bind(assertDataFieldsSizeAtLeastMaxCap(numFields));
        removeFieldButton.disableProperty().bind(assertDataFieldsSizeNonpositive(numFields));
        super.addAllModes(operationList);
        addAllChipSelectIndexes();
        chipSelectList.getSelectionModel().selectFirst();
        operationList.getSelectionModel().selectFirst();
    }

    private void initUsedRequestsComboBox() {
        usedRequestsComboBox.setItems(FXCollections.observableArrayList(UserDataUtils.getSpiRequests()));
        usedRequestsComboBox.setCellFactory((ListView<SpiRequestValueObject> param) -> {
            final ListCell<SpiRequestValueObject> cell = new ListCell<SpiRequestValueObject>() {
                {
                    super.setPrefWidth(150);
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
        usedRequestsComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            chipSelectList.getSelectionModel().select(newValue.getChipSelect());
            operationList.getSelectionModel().select(newValue.getOperation());
            for (int i = 0; i < newValue.getBytes().size(); i++) {
                ((TextField) textFieldGridPaneSpi.getChildren().get(i)).setText(newValue.getBytes().get(i));
                ((TextField) textFieldGridPaneSpi.getChildren().get(i)).setDisable(false);
            }
            numFields.set(newValue.getBytes().size());
        });
    }

    private void addAllChipSelectIndexes() {
        List<Integer> ints = new ArrayList<>();
        for (int i = 0; i < MAX_CS_INDEX; i++) {
            ints.add(i);
        }
        this.chipSelectList.setItems(FXCollections.observableArrayList(ints));
    }

    private BooleanBinding checkGridPaneChildrenOutOfBounds() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> numFields.lessThanOrEqualTo(0)
                        .or(numFields.greaterThan(MAX_NUM_FIELDS)).get(), numFields);
        return Bindings.when(binding).then(true).otherwise(false);
    }

    @FXML
    private void sendSpiRequest(MouseEvent event) {
        StringBuilder msgToSend = getMessagePrefix();
        for (String str : super.getBytes(textFieldGridPaneSpi)) {
            msgToSend = msgToSend.append(HEXA_PREFIX).append(str).append(' ');
        }
        ClientNetworkManager.setMessageToSend(App.getIpAddressFromCurrentTab(), msgToSend.toString());
        SpiRequestValueObject request = getNewSpiRequestEntryFromCurrentData();
        usedRequestsComboBox.getItems().add(request);
        UserDataUtils.addNewSpiRequest(request);
    }

    private SpiRequestValueObject getNewSpiRequestEntryFromCurrentData() {
        Operation op = operationList.getSelectionModel().getSelectedItem();
        return new SpiRequestValueObject(chipSelectList.getValue(), op, super.getBytes(textFieldGridPaneSpi));
    }

    @FXML
    private void addTextField(MouseEvent event) {
        super.addNewTextField(textFieldGridPaneSpi, numFields);
    }

    @FXML
    private void removeTextField(MouseEvent event) {
        super.removeLastTextField(textFieldGridPaneSpi, numFields);
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
                .append(operationList.getSelectionModel().getSelectedItem().toString())
                .append(SEPARATOR)
                .append(HEXA_PREFIX)
                .append(chipSelectList.getSelectionModel().getSelectedItem())
                .append(SEPARATOR);
    }
}
