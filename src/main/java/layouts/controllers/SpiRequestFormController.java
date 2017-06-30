package layouts.controllers;

import core.gui.App;
import core.net.ClientNetworkManager;

import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
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
import javafx.scene.paint.Paint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userdata.SpiRequestValueObject;
import userdata.UserDataUtils;

/**
 * FXML Controller class
 *
 * @author miloslav
 */
public class SpiRequestFormController implements Initializable {

    private Label statusBar;
    @FXML
    private Button spiRequestButton;
    @FXML
    private ComboBox<Operation> modeList;
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
    private static final int MAX_NUM_FIELDS = 16;
    private static final char SEPARATOR = ':';
    private static final String HEXA_PREFIX = "0x";
    private static final String HEX_BYTE_REGEX = "^(0?[0-9A-Fa-f]|[1-9A-Fa-f][0-9A-Fa-f])$";
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
        addAllModes();
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
            modeList.getSelectionModel().select(newValue.getOperation());
            for (int i = 0; i < newValue.getBytes().size(); i++) {
                ((TextField) textFieldGridPaneSpi.getChildren().get(i)).setText(newValue.getBytes().get(i));
                ((TextField) textFieldGridPaneSpi.getChildren().get(i)).setDisable(false);
            }
            numFields.set(newValue.getBytes().size());
        });
        spiRequestButton.disableProperty().bind(
                checkGridPaneChildrenOutOfBounds()
                        .or(createDataTextFields().not())
        );
        addFieldButton.disableProperty().bind(chechGridPaneChildrenOutOfBoundsHi());
        removeFieldButton.disableProperty().bind(chechGridPaneChildrenOutOfBoundsLo());
        addAllModes();
        addAllChipSelectIndexes();
        chipSelectList.getSelectionModel().selectFirst();
        modeList.getSelectionModel().selectFirst();
    }

    private void addAllModes() {
        this.modeList.setItems(FXCollections.observableArrayList(Operation.values()));
    }

    private void addAllChipSelectIndexes() {
        List<Integer> ints = new ArrayList<>();
        for (int i = 0; i < MAX_CS_INDEX; i++) {
            ints.add(i);
        }
        this.chipSelectList.setItems(FXCollections.observableArrayList(ints));
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
            textFieldGridPaneSpi.add(newField, i % 4, i / 4);
        }
        return bind;
    }

    private void enforceHexValuesOnly(TextField textfield) {
        textfield.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.equals("")) {
                textfield.setText("");
                return;
            }
            if (!(newValue.matches(HEX_BYTE_REGEX))) {
                textfield.setText(oldValue);
            }
        });
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

    private BooleanBinding checkGridPaneChildrenOutOfBounds() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> numFields.lessThanOrEqualTo(0)
                        .or(numFields.greaterThan(MAX_NUM_FIELDS)).get(), numFields);
        return Bindings.when(binding).then(true).otherwise(false);
    }

    @FXML
    private void sendSpiRequest(MouseEvent event) {
        String msgToSend = gatherMessageFromForm();
        if (msgToSend != null) {
            ClientNetworkManager.setMessageToSend(App.getIpAddressFromCurrentTab(), msgToSend);
            SpiRequestValueObject request = getNewSpiRequestEntryFromCurrentData();
            usedRequestsComboBox.getItems().add(request);
            UserDataUtils.addNewSpiRequest(request);
        }
    }
    
    private SpiRequestValueObject getNewSpiRequestEntryFromCurrentData() {
        Operation op = modeList.getSelectionModel().getSelectedItem();
        return new SpiRequestValueObject(chipSelectList.getValue(), op, getBytes());
    }
    
    private List<String> getBytes() {
        List<Node> enabledNodes = textFieldGridPaneSpi.getChildren().filtered((textfield) -> !textfield.isDisabled());
        List<String> resultDataArray = new ArrayList<>();
        for(Node node : enabledNodes) {
            resultDataArray.add(((TextField)node).getText());
        }
        return resultDataArray;
    }

    @FXML
    private void addTextField(MouseEvent event) {
        ((TextField) textFieldGridPaneSpi.getChildren().get(numFields.get())).setBackground(new Background(new BackgroundFill(Paint.valueOf("FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));
        ((TextField) textFieldGridPaneSpi.getChildren().get(numFields.get())).setStyle("");

        textFieldGridPaneSpi.getChildren().get(numFields.get()).setDisable(false);
        numFields.set(numFields.get() + 1);
    }

    @FXML
    private void removeTextField(MouseEvent event) {
        textFieldGridPaneSpi.getChildren().get(numFields.get() - 1).setDisable(true);
        ((TextField) textFieldGridPaneSpi.getChildren().get(numFields.get() - 1)).setText("");
        numFields.set(numFields.get() - 1);
    }

    private String gatherMessageFromForm() {
        StringBuilder resultBuilder = getMessagePrefix();
        if (textFieldGridPaneSpi.getChildren().isEmpty()) {
            ControllerUtils.showErrorDialogMessage("At least one byte must be sent!");
            return null;
        }
        for (Iterator<Node> it = textFieldGridPaneSpi.getChildren().filtered((node) -> !node.isDisabled()).iterator(); it.hasNext();) {
            TextField tf = (TextField) it.next();
            resultBuilder.append(HEXA_PREFIX).append(tf.getText().trim());
            if (it.hasNext()) {
                resultBuilder = resultBuilder.append(' ');
            }
        }
        LOGGER.info(String.format("SPI request form has now "
                + "submitted the following request:\n %s"
                + "",
                resultBuilder.toString()));
        return resultBuilder.toString();
    }

    /**
     * Generates common prefix for all SPI requests:
     * "SPI:<MODE>:0x<CHIP_INDEX>:"
     *
     * @return
     */
    private StringBuilder getMessagePrefix() {
        return (new StringBuilder())
                .append("SPI:")
                .append(modeList.getSelectionModel().getSelectedItem().toString())
                .append(SEPARATOR)
                .append(HEXA_PREFIX)
                .append(chipSelectList.getSelectionModel().getSelectedItem())
                .append(SEPARATOR);
    }
}
