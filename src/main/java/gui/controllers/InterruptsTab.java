package gui.controllers;

import gui.misc.Graphics;
import gui.userdata.InterruptValueObject;

import java.net.InetAddress;
import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;

import javafx.application.Platform;
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
import javafx.scene.layout.GridPane;
import javafx.util.converter.IntegerStringConverter;
import net.NetworkManager;
import protocol.*;
import util.StringConstants;

import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.ChangeListener;

public final class InterruptsTab implements Initializable {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(Device.class);

    @FXML
    private ComboBox<InterruptType> interruptTypeComboBox;
    @FXML
    private Button pinButton;
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

    private final InetAddress address;
    private final PopOver pinPopup = new PopOver();
    private final GridPane pinGridPane = new GridPane();

    public InterruptsTab(InetAddress address) {
        this.address = address;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addAllPins();
        addAllIntrTypes();
        initPinGridPane();
        pinPopup.animatedProperty().set(false);
        pinPopup.autoFixProperty().set(true);
        pinPopup.setTitle("GPIO pin selector");
        interruptTypeComboBox.getSelectionModel().selectFirst();
        interruptTypeComboBox.setStyle("-fx-background-color: #FFD166;");
        pinButton.setText("Select pin");
        pinButton.setStyle("-fx-background-color: #FFD166;");
        pinButton.setOnMouseClicked((event) -> {
            if (pinPopup.isShowing()) {
                pinPopup.hide();
            } else {
                pinPopup.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
                pinPopup.show(pinButton);
            }
        });
        initCellValueFactory();
        tableView.setItems(InterruptManager.getInterrupts(address));
        tableView.selectionModelProperty().set(null);
    }

    /**
     * Creates new InterruptValueObject from selected ComboBoxes.
     */
    private InterruptValueObject getNewInterruptValueObject(ClientPin pin) {
        InterruptValueObject ivo = new InterruptValueObject(
                pin,
                interruptTypeComboBox.getSelectionModel().getSelectedItem()
        );
        ivo.destroyProperty().addListener((ign, ignore, newState) -> {
            if(newState) {
                tableView.itemsProperty().get().remove(ivo);
            } else {
                throw new IllegalStateException("Cannot switch from destroyed " +
                        "back to not destroyed.");
            }
        });
        return ivo;
    }

    private void initPinGridPane() {
        addAllPins();
        pinPopup.setContentNode(pinGridPane);
    }

    private void addAllPins() {
        for (ClientPin pin : RaspiClientPin.pins()) {
            PinButton btn = new PinButton(pin);
            int pos = btn.pin.getPort() - 1;
            pinGridPane.add(btn, pos % 2, pos / 2);
        }
    }

    private class PinButton extends Button {

        private final ClientPin pin;
        private static final int WIDTH = 70;
        private static final int HEIGHT = 30;

        PinButton(ClientPin pin) {
            super(pin.toString());
            this.pin = pin;
            setMnemonicParsing(false);
            if (!pin.isGpio()) {
                setStyle("-fx-background-color: #000000;"
                        + "-fx-text-fill: #FFFFFF");
                setDisable(true);
                setOpacity(1.0);
            }
            setOnMouseClicked((e) -> {
                InterruptManager.add(address,
                        getNewInterruptValueObject(pin));
                pinPopup.hide();
            });
            setPrefSize(WIDTH, HEIGHT);
        }
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
        numOfIntrs.setCellFactory(TextFieldTableCell
                .forTableColumn(new IntegerStringConverter()));
        lastIntrTime.setCellValueFactory(
                new PropertyValueFactory<>("lastIntrTime"));
        state.setCellValueFactory(new PropertyValueFactory<>("state"));
        state.setCellFactory(p -> new StatePropertyButtonCell());
        removeRowBtn.setCellFactory(p -> new RemoveRowButtonCell());
    }

    private class RemoveRowButtonCell extends
            TableCell<InterruptValueObject, Void> {

        private final Button cellBtn = new Button(null, Graphics.removeBtn());

        RemoveRowButtonCell() {
            cellBtn.setPadding(Insets.EMPTY);
            cellBtn.setOnAction((event) -> buttonClickedListener());
        }

        private void buttonClickedListener() {
            InterruptValueObject ivo = (InterruptValueObject) getTableRow()
                    .getItem();
            if (ivo.stateProperty().get().equals(ListenerState.NOT_RUNNING)) {
                ivo.destroy();
            } else {
                Utils.showErrorDialog(StringConstants.LISTENER_ACTIVE);
            }
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

        private final Button cellBtn = new Button(null, Graphics.playBtn());

        StatePropertyButtonCell() {
            cellBtn.setPadding(Insets.EMPTY);
            cellBtn.setOnAction((event) -> {
                InterruptValueObject selected
                        = (InterruptValueObject) getTableRow().getItem();
                cellBtn.disableProperty().set(true);
                switch (selected.stateProperty().get()) {
                    case NOT_RUNNING: {
                        new Thread(new AbstractInterruptWorker(selected,
                                ListenerState.NOT_RUNNING) {
                            @Override
                            protected String getMessagePrefix() {
                                return "GPIO:INTR_START";
                            }
                        }).start();
                        break;
                    }
                    case RUNNING: {
                        new Thread(new StopInterruptWorker(selected)).start();
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
                    Platform.runLater(()
                            -> cellBtn.setGraphic(Graphics.playBtn())
                    );
                    break;
                }
                case RUNNING: {
                    Platform.runLater(()
                            -> cellBtn.setGraphic(Graphics.stopBtn()));
                    break;
                }
                default:
                    throw new RuntimeException("Unknown state.");
            }
            if (!empty) {
                setGraphic(cellBtn);
            }
        }
    }

    private abstract class AbstractInterruptWorker extends Task<Void> {

        private final InterruptValueObject interrupt;

        protected abstract String getMessagePrefix();

        protected AbstractInterruptWorker(InterruptValueObject selected,
                                          ListenerState guardedState) {
            this.interrupt = selected;
        }

        @Override
        protected Void call() {
            String msgToSend = gatherMessageFromSubmitted();
            if (msgToSend != null) {
                NetworkManager.setMessageToSend(address, msgToSend);
                LOGGER.info(String.format("Request sent: %s", msgToSend));
            }
            return null;
        }

        protected InterruptValueObject getInterrupt() {
            return interrupt;
        }

        private String gatherMessageFromSubmitted() {
            if (interrupt == null) {
                return null;
            }
            StringBuilder result = new StringBuilder(getMessagePrefix());
            result = result
                    .append(':')
                    .append(interrupt.getClientPin().getPinId())
                    .append(' ')
                    .append(interrupt.getType());
            return result.toString();
        }
    }

    private class StopInterruptWorker extends AbstractInterruptWorker {

        StopInterruptWorker(InterruptValueObject selected) {
            super(selected, ListenerState.RUNNING);
        }

        @Override
        protected String getMessagePrefix() {
            return "GPIO:INTR_STOP";
        }
    }
}
