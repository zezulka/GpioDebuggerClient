package layouts.controllers;

import core.gui.App;
import core.net.ClientNetworkManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
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
import javafx.scene.control.MultipleSelectionModel;
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
import protocol.InterruptManager;
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
    private static final Image DISCONNECT_IMG = new Image("disconnect.png", 30, 30, true, true);

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
    @FXML
    private Button disconnectButton;

    public MasterWindowController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeToolbar();
        initializeDeviceTree();
    }

    private void initializeToolbar() {
        initializeToolbarButton(disconnectButton, DISCONNECT_IMG, "Disconnects from device. Device must be both selected in the device tree and active.");
        initializeToolbarButton(connectToDeviceButton, CONNECT_IMG, "Connects to device. Device must be selected in the device tree.");
        initializeToolbarButton(addNewDeviceButton, ADD_NEW_IMG, "Adds new device.");
        initializeToolbarButton(deviceTree, TREE_IMG, "Device tree browser.");

        nowEnteringLabel.visibleProperty().bind(ipAddress.textProperty().isNotEmpty());
        indicationLabel.visibleProperty().bind(ipAddress.textProperty().isNotEmpty());
        indicationLabel.textProperty().bind(indicatorBinding());
        addNewDeviceButton.disableProperty().bind(ipAddress.textProperty().isEmpty());
        addNewDeviceButton.setOnAction((event) -> {
            DeviceValueObject newDevice = getNewDeviceFromUser();
            ObservableList<TreeItem<Object>> childrenAllBranch = devicesTree.getRoot().getChildren().get(ALL_BRANCH_INDEX).getChildren();
            if (newDevice != null) {
                ipAddress.textProperty().set("");
                for (TreeItem<Object> item : childrenAllBranch) {
                    if (item.getValue() instanceof DeviceValueObject && ((DeviceValueObject) item.getValue()).equals(newDevice)) {
                        devicesTree.getSelectionModel().select(item);
                        return;
                    }
                }
                childrenAllBranch.add(new TreeItem<>(newDevice));
                UserDataUtils.putNewDeviceEntryIntoCollection(newDevice);
            }
        });
        connectToDeviceButton.disableProperty().bind(isDeviceFromCorrectBranchSelected((t) -> t.getParent().nextSibling() == null).not());
        disconnectButton.disableProperty().bind(isDeviceFromCorrectBranchSelected(     (t) -> t.getParent().nextSibling() != null).not());
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
    
    /**
     * This binding tells whether selected device in the device tree is from correct branch.
     * Specifically used as a helper function to determine whether to enable 
     * disconnect (connect respectively) button.
     * @param verifyFn
     * @return 
     */
    private BooleanBinding isDeviceFromCorrectBranchSelected(Function<TreeItem, Boolean> verifyFn) {
        BooleanBinding binding = Bindings.createBooleanBinding(() -> {
            MultipleSelectionModel<TreeItem<Object>> selectionModel = devicesTree.getSelectionModel();
            TreeItem<Object> selectedItem = selectionModel.getSelectedItem();
            
            return !selectionModel.isEmpty() && 
                    selectedItem.getValue() instanceof DeviceValueObject &&
                    verifyFn.apply(selectedItem);
        }, devicesTree.getSelectionModel().selectedItemProperty()); 
        return Bindings.when(binding).then(true).otherwise(false);
    }

    private void initializeToolbarButton(ButtonBase button, Image buttonImage, String tooltipText) {
        button.setGraphic(new ImageView(buttonImage));
        Tooltip tooltip = new Tooltip(tooltipText);
        button.setTooltip(tooltip);
    }

    private void initializeDeviceTree() {
        final double separatorThreshold = 0.7;
        DoubleProperty splitPaneDividerPosition = splitPane.getDividers().get(0).positionProperty();
        splitPane.getDividers().get(0).positionProperty().set(separatorThreshold);
        deviceTree.setSelected(true);
        splitPaneDividerPosition.addListener((obs, oldPos, newPos)
                -> {
            if (deviceTree.isSelected() && newPos.doubleValue() > separatorThreshold) {
                splitPane.getDividers().get(0).setPosition(separatorThreshold);
            }
            deviceTree.setSelected(newPos.doubleValue() < 0.97);
        }
        );
        deviceTree.setOnAction(event -> {
            splitPane.setDividerPositions(deviceTree.isSelected() ? separatorThreshold : 1.0);
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

    private void moveDeviceNodeToOtherBranch(TreeItem item) {
        TreeItem parentNode = item.getParent();
        parentNode.getChildren().remove(item);
        TreeItem nextNode;
        if((nextNode = parentNode.nextSibling()) == null) {
            parentNode.previousSibling().getChildren().add(item);
        } else {
            nextNode.getChildren().add(item);
        }
    }

    @FXML
    private void connectToDeviceHandler(MouseEvent event) {
        try {
            TreeItem selectedItem = devicesTree.getSelectionModel().getSelectedItem();
            new Thread(new ConnectionWorker(selectedItem)).start();
        } catch (ClassCastException ex) {
            //this should never happen because of the connectToDevice button binding!
            throw new RuntimeException(ex);
        }
    }

    @FXML
    private void disconnectHandler(MouseEvent event) {
        if (ControllerUtils.showConfirmationDialogMessage("Are you sure that you want to disconnect from this device?")) {
            TreeItem selectedItem = devicesTree.getSelectionModel().getSelectedItem();
            DeviceValueObject selectedDevice = (DeviceValueObject) selectedItem.getValue();
            ClientNetworkManager.disconnect(selectedDevice.getAddress());
            InterruptManager.clearAllListeners(selectedDevice.getAddress());
            moveDeviceNodeToOtherBranch(selectedItem);
            devicesTab.getTabs().remove(App.getTabFromInetAddress(selectedDevice.getAddress()));
        }
    }

    public static Tab getCurrentTab() {
        return MasterWindowController.currentTab;
    }

    private class ConnectionWorker extends Task<Boolean> {

        private final TreeItem selectedItem;
        private final DeviceValueObject device;

        public ConnectionWorker(TreeItem selectedItem) {
            this.selectedItem = selectedItem;
            this.device = (DeviceValueObject) selectedItem.getValue();
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
                    moveDeviceNodeToOtherBranch(selectedItem);
                    devicesTree.refresh();
                }
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.error(null, ex);
            }
            isConnectionEstablishmentPending.set(false);
        }
    }
}
