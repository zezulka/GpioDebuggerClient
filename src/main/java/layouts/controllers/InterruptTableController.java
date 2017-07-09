package layouts.controllers;

import core.gui.App;
import core.net.ClientNetworkManager;
import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;

import javafx.concurrent.Task;
import javafx.event.EventType;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.converter.IntegerStringConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protocol.ClientPin;
import protocol.InterruptManager;
import protocol.InterruptType;
import protocol.InterruptValueObject;
import protocol.ListenerState;

/**
 * FXML Controller class
 *
 * @author miloslav
 */
public class InterruptTableController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaspiController.class);
    private static final Image PLAY_BTN = new Image("play-button.jpg", 30, 30, true, true);
    private static final Image STOP_BTN = new Image("stop-button.jpg", 30, 30, true, true);

    @FXML
    private Button addNewInterruptListenerButton;
    @FXML
    private TableColumn<InterruptValueObject, ClientPin> pinName;
    @FXML
    private TableColumn<InterruptValueObject, InterruptType> interruptType;
    @FXML
    private TableColumn<InterruptValueObject, LocalTime> timeAdded;
    @FXML
    private TableColumn<InterruptValueObject, Integer> numberOfInterrupts;
    @FXML
    private TableColumn<InterruptValueObject, LocalTime> latestInterruptTime;
    @FXML
    private TableColumn<InterruptValueObject, ListenerState> state;
    @FXML
    private TableView<InterruptValueObject> tableView;
    @FXML
    private Button submitButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initCellValueFactory();
        tableView.setItems(InterruptManager.getListeners(App.getLastAddress()));
        tableView.selectionModelProperty().set(null);
        tableView.setEditable(true);
        addNewInterruptListenerButton.disableProperty().bind(assertNumListeners());
        addNewInterruptListenerButton.setOnMouseClicked((event) -> {
            App.createNewAddListenerPromptForm();
        });
    }

    protected BooleanBinding assertNumListeners() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> InterruptManager.getNumListeners().greaterThanOrEqualTo(InterruptManager.MAX_INTR_LISTENER_THRESHOLD).get(), InterruptManager.getNumListeners());
        return Bindings.when(binding).then(true).otherwise(false);
    }

    private void initCellValueFactory() {
        pinName.setCellValueFactory(new PropertyValueFactory<>("clientPin"));
        interruptType.setCellValueFactory(new PropertyValueFactory<>("type"));
        timeAdded.setCellValueFactory(new PropertyValueFactory<>("timeAdded"));
        numberOfInterrupts.setCellValueFactory(new PropertyValueFactory<>("numberOfInterrupts"));
        numberOfInterrupts.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        latestInterruptTime.setCellValueFactory(new PropertyValueFactory<>("latestInterruptTime"));
        state.setCellValueFactory(new PropertyValueFactory<>("state"));
        state.setCellFactory(p -> new ButtonCell());
    }

    private class ButtonCell extends TableCell<InterruptValueObject, ListenerState> {

        final Button cellButton = new Button(null, new ImageView(PLAY_BTN));

        public ButtonCell() {
            cellButton.setPadding(Insets.EMPTY);
            cellButton.getChildrenUnmodifiable().size();
            cellButton.setOnAction((event) -> {
                InterruptValueObject selected = (InterruptValueObject) getTableRow().getItem();
                cellButton.disableProperty().set(true);
                switch (selected.stateProperty().get()) {
                    case NOT_RUNNING: {
                        new Thread(new StartInterruptsWorker(selected)).start();
                        break;
                    }
                    case RUNNING: {
                        new Thread(new StopInterruptsWorker(selected)).start();
                        break;
                    }
                }
                cellButton.disableProperty().set(false);
            });
        }

        //Display button if the row is not empty
        @Override
        protected void updateItem(ListenerState t, boolean empty) {
            super.updateItem(t, empty);
            getTableView().refresh();
            if(t == null) {
                return;
            }
            switch (t) {
                case NOT_RUNNING: {
                    Platform.runLater(() -> cellButton.setGraphic(new ImageView(PLAY_BTN)));
                    break;
                }
                case RUNNING: {
                    Platform.runLater(() -> cellButton.setGraphic(new ImageView(STOP_BTN)));
                    break;
                }
            }
            if (!empty) {
                setGraphic(cellButton);
            }
        }
    }

    private abstract class AbstractInterruptsWorker extends Task<Void> {

        private final InterruptValueObject selectedIntr;

        protected abstract String getMessagePrefix();

        protected AbstractInterruptsWorker(InterruptValueObject selected, ListenerState guardedState) {
            this.selectedIntr = selected;
        }

        @Override
        protected Void call() throws Exception {
            super.done();
            String msgToSend = gatherMessageFromSubmitted();
            if (msgToSend != null) {
                ClientNetworkManager.setMessageToSend(App.getIpAddressFromCurrentTab(), msgToSend);
                LOGGER.info(String.format("SPI request sent to client: %s", msgToSend));
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
                    .append(selectedIntr.getClientPin().getName())
                    .append(' ')
                    .append(selectedIntr.getType());
            return result.toString();
        }
    }

    public class StartInterruptsWorker extends AbstractInterruptsWorker {

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

    public class StopInterruptsWorker extends AbstractInterruptsWorker {

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
