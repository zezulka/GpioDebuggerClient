package layouts.controllers;

import core.net.ClientNetworkManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javafx.application.Platform;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userdata.SpiRequestValueObject;
import userdata.UserDataUtils;

/**
 * FXML Controller class
 *
 * @author Miloslav
 */
public class IpPromptController implements Initializable {

    @FXML
    private TextField byteOne;
    @FXML
    private TextField byteTwo;
    @FXML
    private TextField byteThree;
    @FXML
    private TextField byteFour;
    @FXML
    private ComboBox<InetAddress> ipAddressesComboBox;

    private static final String BYTE_REGEX = "^([0-9]|[1-9][0-9]|1[0-9][0-9]|25[0-5]|2[0-4][0-9])$";
    private static final Pattern BYTE_REGEX_PATTERN = Pattern.compile(BYTE_REGEX);
    private static final Logger LOGGER = LoggerFactory.getLogger(IpPromptController.class);
    private final BooleanProperty isConnectivityCheckInProgress = new SimpleBooleanProperty(false);
    private static final ClientNetworkManager NETWORK_MANAGER = ClientNetworkManager.getInstance();
    @FXML
    private Button submitExistingButton;
    @FXML
    private Button submitNewButton;

    /**
     * initialises the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ipAddressesComboBox.setCellFactory((ListView<InetAddress> param) -> {
            final ListCell<InetAddress> cell = new ListCell<InetAddress>() {
                {
                    super.setPrefWidth(150);
                }

                @Override
                public void updateItem(InetAddress item,
                        boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.getHostAddress());
                    }
                }
            };
            return cell;
        }
        );
        ipAddressesComboBox.setItems(FXCollections.observableArrayList(UserDataUtils.getAddressesFromFile()));
        ipAddressesComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String[] strs = newValue.getHostAddress().split("\\.");
            //unfortunately, newValue.getAddress() does not yield appropriate results (byte is always interpreted as signed)
            //that's why split had to be used
            byteOne.setText(strs[0]);
            byteTwo.setText(strs[1]);
            byteThree.setText(strs[2]);
            byteFour.setText(strs[3]);
        });
        submitExistingButton.disableProperty().bind(
                ipAddressesComboBox.getSelectionModel().selectedItemProperty().isNull()
        );
        submitNewButton.disableProperty().bind(
                Bindings.isEmpty(byteOne.textProperty())
                        .or(Bindings.isEmpty(byteTwo.textProperty()))
                        .or(Bindings.isEmpty(byteThree.textProperty()))
                        .or(Bindings.isEmpty(byteFour.textProperty()))
                        .or(isConnectivityCheckInProgress)
                        .or(textFieldContainsByteValue(byteOne))
                        .or(textFieldContainsByteValue(byteTwo))
                        .or(textFieldContainsByteValue(byteThree))
                        .or(textFieldContainsByteValue(byteFour))
        );
        enforceNumericValuesOnly(byteOne);
        enforceNumericValuesOnly(byteTwo);
        enforceNumericValuesOnly(byteThree);
        enforceNumericValuesOnly(byteFour);
        checkByteValuesOnly(byteOne);
        checkByteValuesOnly(byteTwo);
        checkByteValuesOnly(byteThree);
        checkByteValuesOnly(byteFour);
    }

    private BooleanBinding textFieldContainsByteValue(TextField textField) {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> BYTE_REGEX_PATTERN.matcher(textField.getText()).matches(), textField.textProperty());
        return Bindings.when(binding).then(false).otherwise(true);
    }

    private void enforceNumericValuesOnly(TextField textfield) {
        textfield.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.equals("")) {
                textfield.setText("");
                return;
            }
            if (!(newValue.matches(BYTE_REGEX))) {
                textfield.setText(oldValue);
            }
        });
    }

    private void checkByteValuesOnly(TextField textfield) {
        textfield.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue && !textfield.getText().isEmpty()) {
                if (!textfield.getText().matches(BYTE_REGEX)) {
                    textfield.setBackground(new Background(new BackgroundFill(Paint.valueOf("ff5555"), CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    textfield.setBackground(new Background(new BackgroundFill(Paint.valueOf("#eeffee"), CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }

        });
    }

    @FXML
    private void submitExistingButtonPressed(MouseEvent event) {
        existingAddressHandler();
    }

    @FXML
    private void submitNewButtonPressed(MouseEvent event) {
        newAddressHandler();
    }

    private class CheckConnectivityWorker extends Task<Boolean> {

        private final InetAddress inetAddress;

        public CheckConnectivityWorker(InetAddress inetAddress) {
            this.inetAddress = inetAddress;
        }

        @Override
        protected Boolean call() {
            try {
                if (!inetAddress.isReachable(ClientNetworkManager.TIMEOUT)) {
                    Platform.runLater(() -> {
                        ControllerUtils.showErrorDialogMessage(String.format("Host %s could not be reached.", inetAddress));
                    });

                } else {
                    LOGGER.debug(String.format("Host %s is reachable", inetAddress));
                    return true;
                }
            } catch (IOException ex) {
                LOGGER.error(null, ex);
                return false;
            }
            return false;
        }

        @Override
        protected void done() {
            isConnectivityCheckInProgress.set(false);
            try {
                if (get()) {
                    NETWORK_MANAGER.connectToDevice(inetAddress);
                    Stage stage = (Stage) submitExistingButton.getScene().getWindow();
                    Platform.runLater(() -> stage.close());
                }
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.error(null, ex);
            }
        }
    }

    private void newAddressHandler() {
        isConnectivityCheckInProgress.set(true);
        byte[] bytes = new byte[4];
        InetAddress address;
        try {
            bytes[0] = (byte) Short.parseShort(byteOne.getText());
            bytes[1] = (byte) Short.parseShort(byteTwo.getText());
            bytes[2] = (byte) Short.parseShort(byteThree.getText());
            bytes[3] = (byte) Short.parseShort(byteFour.getText());
            address = InetAddress.getByAddress(bytes);
            Task connectivityTask = new CheckConnectivityWorker(address);
            new Thread(connectivityTask).start();
        } catch (NumberFormatException | IOException ex) {
            ControllerUtils.showErrorDialogMessage(ex.getLocalizedMessage());
        }
    }

    private void existingAddressHandler() {
        isConnectivityCheckInProgress.set(true);
        Task connectivityTask = new CheckConnectivityWorker(ipAddressesComboBox.getSelectionModel().getSelectedItem());
        new Thread(connectivityTask).start();
    }

    private void keyTyped(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            newAddressHandler();
        }
    }

}
