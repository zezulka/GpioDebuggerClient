package layouts.controllers;

import core.net.ClientNetworkManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.apache.commons.validator.routines.InetAddressValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userdata.DeviceValueObject;
import userdata.UserDataUtils;

public class MasterWindowController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MasterWindowController.class);
    private static Tab currentTab = null;
    private static final ClientNetworkManager NETWORK_MANAGER = ClientNetworkManager.getInstance();
    private final BooleanProperty isConnectionEstablishmentPending = new SimpleBooleanProperty(false);
    private static final int ALL_BRANCH_INDEX = 1;
    private static final int ACTIVE_BRANCH_INDEX = 0;

    private static final Image DEVICES_IMG = new Image("devices.png", 30, 30, true, true);
    private static final Image ACTIVE_IMG = new Image("active.png", 20, 20, true, true);
    private static final Image HISTORY_IMG = new Image("history.png", 30, 30, true, true);
    private static final Image ADD_NEW_IMG = new Image("add_new.png", 30, 30, true, true);
    private static final Image TREE_IMG = new Image("tree.png", 30, 30, true, true);
    private static final Image CONNECT_IMG = new Image("connect.png", 30, 30, true, true);

    @FXML
    private TabPane devicesTab;
    @FXML
    private Button connectToDeviceButton;
    @FXML
    private TreeView<Object> devicesTree;
    @FXML
    private SplitPane splitPane;
    @FXML
    private ToggleButton deviceTree;
    @FXML
    private Button addNewDeviceButton;
    @FXML
    private TextField ipAddress;
    @FXML
    private Label nowEnteringLabel;
    @FXML
    private Label indicationLabel;

    public MasterWindowController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeToolbar();
        initializeDeviceTree();
    }

    private void initializeToolbar() {
        initializeToolbarButton(connectToDeviceButton, CONNECT_IMG, "Connects to device. Device must be selected in device tree.");
        initializeToolbarButton(addNewDeviceButton, ADD_NEW_IMG, "Adds new device.");
        initializeToolbarButton(deviceTree, TREE_IMG, "Device tree browser.");
        nowEnteringLabel.visibleProperty().bind(ipAddress.textProperty().isNotEmpty());
        indicationLabel.visibleProperty().bind(ipAddress.textProperty().isNotEmpty());
        indicationLabel.textProperty().bind(indicatorBinding());
        addNewDeviceButton.disableProperty().bind(ipAddress.textProperty().isEmpty());
        addNewDeviceButton.setOnAction((event) -> {
            DeviceValueObject newDevice = getNewDeviceFromUser();
            ObservableList<TreeItem<Object>> childrenAllBranch = devicesTree.getRoot().getChildren().get(ALL_BRANCH_INDEX).getChildren();
            if(newDevice != null) {
                ipAddress.textProperty().set("");
                for(TreeItem<Object> item : childrenAllBranch) {
                    if(item.getValue() instanceof DeviceValueObject && ((DeviceValueObject) item.getValue()).equals(newDevice)) {
                        devicesTree.getSelectionModel().select(item);
                        return;
                    }
                }
                childrenAllBranch.add(new TreeItem<>(newDevice));
                UserDataUtils.putNewDeviceEntryIntoCollection(newDevice);
            }
        });
        connectToDeviceButton.disableProperty().bind(isAnyDeviceSelected());
        devicesTab.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            currentTab = newValue;
        });
    }
    
    private StringBinding indicatorBinding() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> textfieldContainsIpAddress(), ipAddress.textProperty());
        return Bindings.when(binding).then("IP address").otherwise("hostname");
    }
    
    private boolean textfieldContainsIpAddress() {
        return InetAddressValidator.getInstance().isValid(ipAddress.getText().trim());
    }
    
    private DeviceValueObject getNewDeviceFromUser() {
        try {
            return new DeviceValueObject(InetAddress.getByName(ipAddress.getText()), null);
        } catch (UnknownHostException ex) {
            LOGGER.error("Invalid IP address");
            ControllerUtils.showErrorDialogMessage("The hostname you provided has not been found.");
        }
        return null;
    }

    private BooleanBinding isAnyDeviceSelected() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> (!devicesTree.getSelectionModel().isEmpty() && devicesTree.getSelectionModel().getSelectedItem().getValue() instanceof DeviceValueObject),
                devicesTree.getSelectionModel().selectedItemProperty());
        return Bindings.when(binding).then(false).otherwise(true);
    }

    private void initializeToolbarButton(ButtonBase button, Image buttonImage, String tooltipText) {
        button.setGraphic(new ImageView(buttonImage));
        Tooltip tooltip = new Tooltip(tooltipText);
        button.setTooltip(tooltip);
    }

    private void initializeDeviceTree() {
        DoubleProperty splitPaneDividerPosition = splitPane.getDividers().get(0).positionProperty();
        splitPaneDividerPosition.addListener((obs, oldPos, newPos)
                -> deviceTree.setSelected(newPos.doubleValue() > 0.05));
        deviceTree.setOnAction(event -> {
            splitPane.setDividerPositions(deviceTree.isSelected() ? 0.25 : 0.0);
        });
        TreeItem<Object> root = new TreeItem<>("devices", new ImageView(DEVICES_IMG));
        TreeItem<Object> activeBranch = new TreeItem<>("active", new ImageView(ACTIVE_IMG));
        TreeItem<Object> historyBranch = new TreeItem<>("all", new ImageView(HISTORY_IMG));
        UserDataUtils.getDevices().forEach((device) -> historyBranch.getChildren().add(new TreeItem<>(device)));
        root.getChildren().add(activeBranch);
        root.getChildren().add(historyBranch);
        root.setExpanded(true);
        devicesTree.setRoot(root);
    }

    @FXML
    private void connectToDeviceHandler(MouseEvent event) {
        try {
            DeviceValueObject selectedDevice = (DeviceValueObject) devicesTree.getSelectionModel().getSelectedItem().getValue();
            new Thread(new ConnectionWorker(selectedDevice)).start();
        } catch(ClassCastException ex) {
            //this should never happen because of the connectToDevice button binding!
            throw new RuntimeException(ex);
        }
    }

    public static Tab getCurrentTab() {
        return MasterWindowController.currentTab;
    }

    private class ConnectionWorker extends Task<Boolean> {

        private final DeviceValueObject device;

        public ConnectionWorker(DeviceValueObject device) {
            this.device = device;
        }

        @Override
        protected Boolean call() {
            try {
                if (!device.getAddress().isReachable(ClientNetworkManager.TIMEOUT)) {
                    Platform.runLater(() -> {
                        ControllerUtils.showErrorDialogMessage(String.format("Host %s could not be reached.", device.getHostName()));
                    });

                } else {
                    LOGGER.debug(String.format("Host %s is reachable", device.getHostName()));
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
            try {
                if (get() && NETWORK_MANAGER.connectToDevice(device)) {
                    //devicesTree.getRoot().getChildren().get(0).getChildren().add(new TreeItem<>(device));
                    devicesTree.refresh();
                }
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.error(null, ex);
            }
            isConnectionEstablishmentPending.set(false);
        }
    }
}
