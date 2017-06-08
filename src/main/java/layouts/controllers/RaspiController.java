package layouts.controllers;

import core.ClientConnectionManager;

import java.io.IOException;

import java.net.URL;
import java.time.LocalTime;

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
import protocol.InterruptValueObject.State;

import protocol.ProtocolMessages;

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
    private TableColumn<InterruptValueObject, State> state;

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

    static void addNewInteruptListener(InterruptValueObject interrupt) {
        INTERRUPTS.add(interrupt);
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
            case REMOVE: {
                new Thread(new RemoveInterruptsWorker()).start();
                break;
            }
            case STOP: {
                new Thread(new StopInterruptsWorker()).start();
                break;
            }
        }
    }

    private class RemoveInterruptsWorker extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            //send request to ARM device and wait for request
            Thread.sleep(5000);
            return null;
        }

        @Override
        protected void done() {
            INTERRUPTS.removeAll(INTERRUPTS.filtered((t) -> t.selectedProperty().get()));
        }
    }

    private class StartInterruptsWorker extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            Thread.sleep(50);
            return null;
        }

        @Override
        protected void done() {
            for (InterruptValueObject obj : INTERRUPTS.filtered((t) -> t.selectedProperty().get())) {
                obj.incrementNumberOfInterrupts();
                obj.setLatestInterruptTime(LocalTime.now());
                obj.setState(InterruptValueObject.State.RUNNING);
            }
        }
    }

    private class StopInterruptsWorker extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            return null;
        }

        @Override
        protected void done() {
            for (InterruptValueObject obj : INTERRUPTS.filtered((t) -> t.selectedProperty().get())) {
                obj.incrementNumberOfInterrupts();
                obj.setState(InterruptValueObject.State.STOPPED);
            }
        }
    }

    private static enum BulkAction {
        START("start"), STOP("stop"), REMOVE("remove");

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
    }

    @FXML
    protected void keyPressedHandler(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            String op = readRadioButton.isSelected() ? "read" : "write";
            sendRequest(event, "gpio:" + op + ":" + getButtonTitle(event));
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
