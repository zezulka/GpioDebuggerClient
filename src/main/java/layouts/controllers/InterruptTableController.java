package layouts.controllers;

import core.net.ClientNetworkManager;
import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import javafx.scene.input.MouseEvent;

import javafx.util.converter.IntegerStringConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protocol.ClientPin;
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
    private static final ObservableList<InterruptValueObject> INTERRUPTS = FXCollections.observableArrayList();
    private static final int MAX_THREAD_THRESHOLD = 10;

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
    private ComboBox<Action> actionsComboBox;
    @FXML
    private TableView<InterruptValueObject> tableView;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addAllBulkActions();
        initCellValueFactory();
        actionsComboBox.getSelectionModel().selectFirst();
        tableView.setItems(INTERRUPTS);
        tableView.setEditable(true);
        addNewInterruptListenerButton.setOnMouseClicked((event) -> {
            if (INTERRUPTS.size() <= MAX_THREAD_THRESHOLD) {
                GuiEntryPoint.createNewAddListenerForm();
            }
        });
    }

    private void addAllBulkActions() {
        actionsComboBox.setItems(FXCollections.observableArrayList(Action.values()));
    }

    @FXML
    private void addNewListenerMouseClicked(MouseEvent event) {
        GuiEntryPoint.createNewAddListenerForm();
    }

    @FXML
    private void submitBulkActions(MouseEvent event) {
        Action action = actionsComboBox.getValue();
        switch (action) {
            case START: {
                new Thread(new StartInterruptsWorker()).start();
                break;
            }
            case CANCEL: {
                new Thread(new StopInterruptsWorker()).start();
                break;
            }
        }
    }

    static boolean addNewInterruptListener(InterruptValueObject interrupt) {
        if (!INTERRUPTS.contains(interrupt)) {
            INTERRUPTS.add(interrupt);
            return true;
        }
        ControllerUtils.showErrorDialogMessage("This interrupt has already been registered.");
        return false;
    }

    public static void updateInterruptListener(InterruptValueObject ivo) {
        for (int i = 0; i < INTERRUPTS.size(); i++) {
            InterruptValueObject curr = INTERRUPTS.get(i);
            if (curr.equals(ivo)) {
                ivo.setNumberOfInterrupts(curr.numberOfInterruptsProperty().get() + 1);
                INTERRUPTS.set(i, ivo);
                return;
            }
        }
    }

    private void initCellValueFactory() {
        pinName.setCellValueFactory(new PropertyValueFactory<>("clientPin"));
        interruptType.setCellValueFactory(new PropertyValueFactory<>("type"));
        timeAdded.setCellValueFactory(new PropertyValueFactory<>("timeAdded"));
        numberOfInterrupts.setCellValueFactory(new PropertyValueFactory<>("numberOfInterrupts"));
        numberOfInterrupts.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        latestInterruptTime.setCellValueFactory(new PropertyValueFactory<>("latestInterruptTime"));
        state.setCellValueFactory(new PropertyValueFactory<>("state"));
    }

    private static enum Action {
        START("start"), CANCEL("cancel");

        private final String desc;

        private Action(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return this.desc;
        }
    }

    private abstract class AbstractInterruptsWorker extends Task<Void> {

        private final InterruptValueObject selectedIntr;

        protected abstract String getMessagePrefix();

        protected AbstractInterruptsWorker(ListenerState guardedState) {
            InterruptValueObject ivo = tableView.getSelectionModel().getSelectedItem();
            if (ivo != null && ivo.stateProperty().get().equals(guardedState)) {
                this.selectedIntr = tableView.getSelectionModel().getSelectedItem();
            } else {
                selectedIntr = null;
            }
        }

        @Override
        protected Void call() throws Exception {
            super.done();
            String msgToSend = gatherMessageFromSubmitted();
            if (msgToSend != null) {
                ClientNetworkManager.setMessageToSend(msgToSend);
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

    private class StartInterruptsWorker extends AbstractInterruptsWorker {

        public StartInterruptsWorker() {
            super(ListenerState.NOT_RUNNING);
        }

        @Override
        protected String getMessagePrefix() {
            return "GPIO:INTR_START";
        }
    }

    private class StopInterruptsWorker extends AbstractInterruptsWorker {

        public StopInterruptsWorker() {
            super(ListenerState.RUNNING);
        }

        @Override
        protected String getMessagePrefix() {
            return "GPIO:INTR_STOP";
        }
    }

}
