package layouts.controllers;

import core.ClientConnectionManager;

import java.io.IOException;

import java.net.URL;
import java.time.LocalTime;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.RadioButton;

import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.converter.IntegerStringConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protocol.ClientPin;
import protocol.InterruptType;

import protocol.InterruptValueObject;

import protocol.ProtocolMessages;
import protocol.ListenerState;

/**
 *
 * @author Miloslav Zezulka
 */
public class RaspiController implements DeviceController, Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaspiController.class);
    private static final ObservableList<InterruptValueObject> INTERRUPTS = FXCollections.observableArrayList();
    private static final int MAX_THREAD_THRESHOLD = 10;

    @FXML
    private RadioButton readRadioButton;
    @FXML
    private TableView<InterruptValueObject> tableView;
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
    private TableColumn<InterruptValueObject, Boolean> bulkAction;
    @FXML
    private ComboBox<BulkAction> bulkActionsComboBox;
    @FXML
    private TableColumn<InterruptValueObject, ListenerState> state;
    @FXML
    private Button submitButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addAllBulkActions();
        bulkActionsComboBox.getSelectionModel().selectFirst();
        initCellValueFactory();
        tableView.setItems(INTERRUPTS);
        tableView.setEditable(true);
        addNewInterruptListenerButton.setOnMouseClicked((event) -> {
            if (INTERRUPTS.size() <= MAX_THREAD_THRESHOLD) {
                try {
                    GuiEntryPoint.getInstance().createNewAddListenerForm();
                } catch (IOException ex) {
                    LOGGER.error(null, ex);
                }
            }
        });
    }

    static boolean addNewInteruptListener(InterruptValueObject interrupt) {
        if (!INTERRUPTS.contains(interrupt)) {
            INTERRUPTS.add(interrupt);
            return true;
        }
        ControllerUtils.showErrorDialogMessage("This interrupt has already been registered.");
        return false;
    }

    private void addAllBulkActions() {
        bulkActionsComboBox.setItems(FXCollections.observableArrayList(BulkAction.values()));
    }

    @FXML
    private void submitBulkActions(MouseEvent event) {
        BulkAction action = bulkActionsComboBox.getValue();
        switch (action) {
            case START: {
                new Thread(new StartInterruptsWorker()).start();
                break;
            }
            case CANCEL: {
                new Thread(new StopInterruptsWorker()).start();
                break;
            }
            case SELECT_NONE: {
                selectNone();
            }
        }
    }

    private void selectNone() {
        INTERRUPTS.forEach((t) -> t.setSelected(Boolean.FALSE));
        tableView.refresh();
    }
    
    public static void updateInterruptListener(InterruptValueObject ivo) {
        for(int i = 0; i < INTERRUPTS.size(); i++) {
            if(INTERRUPTS.get(i).equals(ivo)) {
                INTERRUPTS.set(i, ivo);
                return;
            }
        }
    }
    
    private abstract class AbstractInterruptsWorker extends Task<Void> {
        private final ListenerState guardedState;
        private final List<InterruptValueObject> intrs;
        protected abstract String getMessagePrefix();
        
        protected AbstractInterruptsWorker(ListenerState guardedState) {
            this.guardedState = guardedState;
            this.intrs = 
                INTERRUPTS.filtered((t) -> t.selectedProperty().get()
                && t.stateProperty()
                        .get()
                        .equals(this.guardedState));
        }
        
        @Override
        protected Void call() throws Exception {
            super.done();
            String msgToSend = gatherMessageFromSubmitted();
            if (msgToSend != null) {
                ClientConnectionManager
                        .getInstance()
                        .setMessageToSend(msgToSend);
                LOGGER.info(String.format("SPI request sent to client: %s", msgToSend));
            }
            return null;
        }

        @Override
        protected void done() {
            super.done(); 
            INTERRUPTS.forEach((intr) -> intr.setSelected(Boolean.FALSE));
        }
        
        private String gatherMessageFromSubmitted() {
            if(intrs.isEmpty()) {
                return null;
            }
            StringBuilder result = new StringBuilder(getMessagePrefix());
            for(InterruptValueObject obj : intrs) {
                result = result
                           .append(':')
                           .append(obj.getClientPin().getName())
                           .append(' ')
                           .append(obj.getType());
            }
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

    private static enum BulkAction {
        START("start"), CANCEL("cancel"),
        SELECT_NONE("select none");

        private final String desc;

        private BulkAction(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return this.desc;
        }
    }

    private void initCellValueFactory() {
        pinName.setCellValueFactory(new PropertyValueFactory<>("clientPin"));
        interruptType.setCellValueFactory(new PropertyValueFactory<>("type"));
        timeAdded.setCellValueFactory(new PropertyValueFactory<>("timeAdded"));
        numberOfInterrupts.setCellValueFactory(new PropertyValueFactory<>("numberOfInterrupts"));
        numberOfInterrupts.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        latestInterruptTime.setCellValueFactory(new PropertyValueFactory<>("latestInterruptTime"));
        bulkAction.setCellValueFactory(new PropertyValueFactory<>("selected"));
        bulkAction.setOnEditCommit((event) -> {
            ((InterruptValueObject) event.getTableView().getItems().get(
                    event.getTablePosition().getRow())).setSelected(event.getNewValue());
        });
        bulkAction.setCellFactory((n) -> new CheckBoxTableCell<>());
        state.setCellValueFactory(new PropertyValueFactory<>("state"));
    }

    @FXML
    protected void mouseClickedHandler(MouseEvent event) {
        String op = readRadioButton.isSelected() ? "read" : "write";
        sendRequest(event, "gpio:" + op + ":" + getButtonTitle(event));
        LOGGER.info(String.format("GPIO request has been sent : pin %s, operation : %s", op, getButtonTitle(event)));
    }

    @FXML
    protected void keyPressedHandler(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            String op = readRadioButton.isSelected() ? "read" : "write";
            sendRequest(event, "gpio:" + op + ":" + getButtonTitle(event));
            LOGGER.info(String.format("GPIO request has been sent : pin %s, operation : %s", op, getButtonTitle(event)));
        }
    }

    @FXML
    protected void createSpiForm(MouseEvent event) {
        try {
            GuiEntryPoint.getInstance().createNewSpiForm();
        } catch (IOException ex) {
            LOGGER.error(null, ex);
        }
    }

    @FXML
    protected void createI2cForm(MouseEvent event) {
        try {
            GuiEntryPoint.getInstance().createNewI2cForm();
        } catch (IOException ex) {
            LOGGER.error(null, ex);
        }
    }

    @FXML
    private void addNewListenerMouseClicked(MouseEvent event) {
        try {
            GuiEntryPoint.getInstance().createNewAddListenerForm();
        } catch (IOException ex) {
            LOGGER.error(null, ex);
        }
    }

    private String getButtonTitle(InputEvent event) {
        if (event == null) {
            return null;
        }
        return ((Button) event.getSource()).getText();
    }

    private void sendRequest(InputEvent event, String msg) {
        if (event.getSource() instanceof Button) {
            ClientConnectionManager
                    .getInstance()
                    .setMessageToSend(msg);
        } else {
            LOGGER.error(ProtocolMessages.C_ERR_GUI_NOT_BUTTON.toString());
            throw new IllegalArgumentException("error in MouseEvent: entity clicked is not of Button instance ");
        }
    }
}
