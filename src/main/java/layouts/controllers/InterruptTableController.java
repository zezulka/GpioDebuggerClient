package layouts.controllers;

import core.gui.App;
import core.net.NetworkManager;
import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;

import javafx.concurrent.Task;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.util.converter.IntegerStringConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protocol.ClientPin;
import protocol.InterruptManager;
import protocol.InterruptType;
import protocol.InterruptValueObject;
import protocol.ListenerState;
import protocol.RaspiClientPin;

public final class InterruptTableController implements Initializable {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(RaspiController.class);

    @FXML
    private ComboBox<InterruptType> interruptTypeComboBox;
    @FXML
    private ComboBox<ClientPin> pinComboBox;
    @FXML
    private Button addNewInterruptListenerButton;
    @FXML
    private TableColumn<InterruptValueObject, ClientPin> pinName;
    @FXML
    private TableColumn<InterruptValueObject, InterruptType> interruptType;
    @FXML
    private TableColumn<InterruptValueObject, LocalTime> timeAdded;
    @FXML
    private TableColumn<InterruptValueObject, Integer> numOfIntrs;
    @FXML
    private TableColumn<InterruptValueObject, LocalTime> lastIntrTime;
    @FXML
    private TableColumn<InterruptValueObject, ListenerState> state;
    @FXML
    private TableView<InterruptValueObject> tableView;
    @FXML
    private TableColumn<InterruptValueObject, Void> removeRowBtn;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addAllPins();
        addAllIntrTypes();
        interruptTypeComboBox.getSelectionModel().selectFirst();
        pinComboBox.getSelectionModel().selectFirst();
        initCellValueFactory();
        tableView.setItems(InterruptManager.getListeners(App.getLastAddress()));
        tableView.selectionModelProperty().set(null);
        tableView.setEditable(true);
        addNewInterruptListenerButton.setOnMouseClicked((event) -> {
            InterruptManager.addInterruptListener(
                    App.getIpFromCurrentTab(),
                    getTempIvo()
            );
        });
    }

    /**
     * Creates temporary InterruptValueObject from selected ComboBoxes.
     *
     * @return
     */
    private InterruptValueObject getTempIvo() {
        return new InterruptValueObject(
                pinComboBox.getSelectionModel().getSelectedItem(),
                interruptTypeComboBox.getSelectionModel().getSelectedItem()
        );
    }

    private void addAllPins() {
        List<ClientPin> result = new ArrayList<>();
        for (ClientPin pin : RaspiClientPin.pins()) {
            if (pin.isGpio()) {
                result.add(pin);
            }
        }
        pinComboBox.setItems(FXCollections.observableArrayList(result));
    }

    private void addAllIntrTypes() {
        interruptTypeComboBox.setItems(InterruptType.observableValues());
    }

    private void initCellValueFactory() {
        pinName.setCellValueFactory(new PropertyValueFactory<>("clientPin"));
        interruptType.setCellValueFactory(new PropertyValueFactory<>("type"));
        timeAdded.setCellValueFactory(new PropertyValueFactory<>("timeAdded"));
        numOfIntrs
                .setCellValueFactory(new PropertyValueFactory<>("numOfIntrs"));
        numOfIntrs
                .setCellFactory(TextFieldTableCell
                        .forTableColumn(new IntegerStringConverter()));
        lastIntrTime
                .setCellValueFactory(
                        new PropertyValueFactory<>("lastIntrTime"));
        state.setCellValueFactory(new PropertyValueFactory<>("state"));
        state.setCellFactory(p -> new StatePropertyButtonCell());
        removeRowBtn.setCellFactory(p -> new RemoveRowButtonCell());
    }

    private class RemoveRowButtonCell extends
            TableCell<InterruptValueObject, Void> {

        private final Button cellBtn
                = new Button(null, new ImageView(Images.REMOVE));

        RemoveRowButtonCell() {
            cellBtn.setPadding(Insets.EMPTY);
            cellBtn.setOnAction((event) -> {
                InterruptValueObject ivo = (InterruptValueObject) getTableRow()
                        .getItem();
                tableView
                        .itemsProperty()
                        .get()
                        .remove(ivo);
            });
        }

        //Display button if the row is not empty
        @Override
        protected void updateItem(Void nothing, boolean empty) {
            super.updateItem(nothing, empty);
            getTableView().refresh();
            if (!empty) {
                setGraphic(cellBtn);
            }
        }
    }

    private class StatePropertyButtonCell extends
            TableCell<InterruptValueObject, ListenerState> {

        private final Button cellBtn
                = new Button(null, new ImageView(Images.PLAY_BTN));

        StatePropertyButtonCell() {
            cellBtn.setPadding(Insets.EMPTY);
            cellBtn.setOnAction((event) -> {
                InterruptValueObject selected
                        = (InterruptValueObject) getTableRow().getItem();
                cellBtn.disableProperty().set(true);
                switch (selected.stateProperty().get()) {
                    case NOT_RUNNING: {
                        new Thread(new StartInterruptsWorker(selected)).start();
                        break;
                    }
                    case RUNNING: {
                        new Thread(new StopInterruptsWorker(selected)).start();
                        break;
                    }
                    default:
                        throw new RuntimeException("Unknown state.");
                }
                cellBtn.disableProperty().set(false);
            });
        }

        //Display button if the row is not empty
        @Override
        protected void updateItem(ListenerState t, boolean empty) {
            super.updateItem(t, empty);
            getTableView().refresh();
            if (t == null) {
                return;
            }
            switch (t) {
                case NOT_RUNNING: {
                    Platform.runLater(() -> {
                        cellBtn.setGraphic(new ImageView(Images.PLAY_BTN));
                    });
                    break;
                }
                case RUNNING: {
                    Platform.runLater(() -> {
                        cellBtn.setGraphic(new ImageView(Images.STOP_BTN));
                    });
                    break;
                }
                default:
                    throw new RuntimeException("Uknown state.");
            }
            if (!empty) {
                setGraphic(cellBtn);
            }
        }
    }

    private abstract class AbstractInterruptsWorker extends Task<Void> {

        private final InterruptValueObject selectedIntr;

        protected abstract String getMessagePrefix();

        protected AbstractInterruptsWorker(InterruptValueObject selected,
                ListenerState guardedState) {
            this.selectedIntr = selected;
        }

        @Override
        protected Void call() throws Exception {
            super.done();
            String msgToSend = gatherMessageFromSubmitted();
            if (msgToSend != null) {
                NetworkManager
                        .setMessageToSend(App.getIpFromCurrentTab(),
                                msgToSend);
                LOGGER.info(String.format("SPI request sent: %s", msgToSend));
            }
            return null;
        }

        private String gatherMessageFromSubmitted() {
            if (selectedIntr == null) {
                return null;
            }
            StringBuilder result = new StringBuilder(getMessagePrefix());
            result = result
                    .append(':')
                    .append(selectedIntr.getClientPin().getPinId())
                    .append(' ')
                    .append(selectedIntr.getType());
            return result.toString();
        }
    }

    public final class StartInterruptsWorker extends AbstractInterruptsWorker {

        public StartInterruptsWorker(InterruptValueObject selected) {
            super(selected, ListenerState.NOT_RUNNING);
        }

        @Override
        protected String getMessagePrefix() {
            return "GPIO:INTR_START";
        }

        @Override
        protected void done() {
            super.done();
        }
    }

    public final class StopInterruptsWorker extends AbstractInterruptsWorker {

        public StopInterruptsWorker(InterruptValueObject selected) {
            super(selected, ListenerState.RUNNING);
        }

        @Override
        protected String getMessagePrefix() {
            return "GPIO:INTR_STOP";
        }

        @Override
        protected void done() {
            super.done();
        }
    }
}
