package layouts.controllers;

import core.gui.App;
import core.gui.SwitchButton;
import core.net.NetworkManager;
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SplitPane;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import javafx.collections.ObservableList;
import core.util.StringConstants;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.InterruptManager;
import userdata.DeviceValueObject;
import userdata.UserDataUtils;

public final class MasterWindowController implements Initializable {

    private SwitchButton deviceTreeSwitch;
    @FXML
    private TabPane devicesTab;
    @FXML
    private SplitPane controlPanel;
    @FXML
    private GridPane newDevicePane;
    @FXML
    private Button connectBtn;
    @FXML
    private TreeView<Object> devicesTree;
    @FXML
    private Button addNewDeviceButton;
    @FXML
    private TextField ipAddress;
    @FXML
    private Button disconnectButton;
    @FXML
    private SplitPane rootSplitPane;
    @FXML
    private ToolBar toolBar;

    private static final Logger LOGGER
            = LoggerFactory.getLogger(MasterWindowController.class);

    private static Tab currentTab = null;
    private static final NetworkManager NETWORK_MANAGER
            = NetworkManager.getInstance();

    private final BooleanProperty connectingToDevice
            = new SimpleBooleanProperty(false);

    private static final int ALL_BRANCH_INDEX = 1;
    private static final int ACTIVE_BRANCH_INDEX = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeDeviceTree();
        initializeToolbar();
    }

    private void initializeToolbar() {
        initDeviceTreeSwitch();
        initConnectToDeviceBtn();
        initDisconnectBtn();
        initAddNewDeviceBtn();
        connectingToDevice.addListener((o, old, pending) -> {
            Platform.runLater(()
                    -> connectBtn.setText(pending ? "Connecting..." : "Connect")
            );
        });
        devicesTab.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> currentTab = newVal);
        final double threshold = 0.7;
        devicesTab.maxWidthProperty()
                .bind(rootSplitPane.widthProperty().multiply(threshold));
        newDevicePane.minHeightProperty()
                .bind(controlPanel.heightProperty().multiply(1 - threshold));
    }

    private void initDeviceTreeSwitch() {
        deviceTreeSwitch = new SwitchButton();
        toolBar.getItems().add(0, deviceTreeSwitch);
        deviceTreeSwitch.switchOnProperty().set(true);
        deviceTreeSwitch.switchOnProperty().addListener((o, old, selected) -> {
            deviceTreeBtnListener(selected);
        });
    }

    private void initConnectToDeviceBtn() {
        initializeToolbarButton(connectBtn, Graphics.CONNECT,
                "Connects to device. "
                + "\nDevice must be selected in the device tree.");
        connectBtn.disableProperty().bind(connectionPending()
                .or(isDeviceBranchCorrect((t)
                        -> t.getParent().getParent().nextSibling() == null)
                        .not())
                .or(deviceTreeVisible().not())
        );
    }

    private void initDisconnectBtn() {
        initializeToolbarButton(disconnectButton, Graphics.DISCONNECT,
                "Disconnects from device. "
                + "\nDevice must be selected in the device tree and active.");
        disconnectButton.disableProperty().bind(devicesTree
                .getRoot().getChildren().get(ACTIVE_BRANCH_INDEX).leafProperty()
        );
    }

    private void initAddNewDeviceBtn() {
        addNewDeviceButton.disableProperty()
                .bind(ipAddress.textProperty().isEmpty());
        addNewDeviceButton.setOnAction((event) -> {
            DeviceValueObject newDevice = getNewDeviceFromUser();
            ObservableList<TreeItem<Object>> childrenAllBranch
                    = devicesTree
                            .getRoot()
                            .getChildren()
                            .get(ALL_BRANCH_INDEX)
                            .getChildren();
            if (newDevice != null) {
                ipAddress.textProperty().set("");
                childrenAllBranch
                        .add((TreeItem) getTreeItemWithListener(newDevice));
                UserDataUtils.addNewDeviceToFile(newDevice);
            }
        });
    }

    private BooleanBinding connectionPending() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> connectingToDevice.get(), connectingToDevice);
        return Bindings.when(binding).then(true).otherwise(false);
    }

    private BooleanBinding deviceTreeVisible() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> devicesTree.getRoot().isExpanded(),
                devicesTree.getRoot().expandedProperty());
        return Bindings.when(binding).then(true).otherwise(false);
    }

    private void deviceTreeBtnListener(boolean isSelected) {
        final double threshold = 0.7;
        connectBtn.visibleProperty().set(isSelected);
        disconnectButton.visibleProperty().set(isSelected);
        final double dividerPos = isSelected ? threshold : 1.0;
        devicesTab.maxWidthProperty().bind(rootSplitPane.widthProperty()
                .multiply(dividerPos));
        rootSplitPane.setDividerPositions(dividerPos);
    }

    private DeviceValueObject getNewDeviceFromUser() {
        try {
            InetAddress inetAddr = InetAddress.getByName(ipAddress.getText());
            return new DeviceValueObject(inetAddr);
        } catch (UnknownHostException ex) {
            LOGGER.error("Invalid IP address");
            ControllerUtils.showErrorDialog("Unknown hostname.");
        }
        return null;
    }

    /**
     * This binding tells whether selected device in the device tree is from
     * correct branch. Specifically used as a helper function to determine
     * whether to enable disconnect (connect respectively) button.
     *
     * @param verifyFn
     * @return
     */
    private BooleanBinding
            isDeviceBranchCorrect(Function<TreeItem, Boolean> verifyFn) {
        BooleanBinding binding = Bindings.createBooleanBinding(() -> {
            MultipleSelectionModel<TreeItem<Object>> selectionModel
                    = devicesTree.getSelectionModel();
            TreeItem<Object> selectedItem = selectionModel.getSelectedItem();

            return !selectionModel.isEmpty()
                    && selectedItem != null
                    && selectedItem.getValue() instanceof DeviceValueObject
                    && verifyFn.apply(selectedItem);
        }, devicesTree.getSelectionModel().selectedItemProperty());
        return Bindings.when(binding).then(true).otherwise(false);
    }

    private void initializeToolbarButton(ButtonBase button,
            ImageView buttonImageView, String tooltipText) {
        button.setGraphic(buttonImageView);
        Tooltip tooltip = new Tooltip(tooltipText);
        button.setTooltip(tooltip);
    }

    private void initializeDeviceTree() {

        TreeItem root = new TreeItem("devices", Graphics.DEVICES);
        TreeItem active = new TreeItem("alive", Graphics.ACTIVE);
        TreeItem hist = new TreeItem("history", Graphics.HISTORY);
        UserDataUtils.getDevices().forEach((device)
                -> hist.getChildren().add(getTreeItemWithListener(device)));
        hist.setExpanded(true);
        root.getChildren().addAll(active, hist);
        root.setExpanded(true);
        devicesTree.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
                doubleClickHandler(event);
            }
        });
        devicesTree.setRoot(root);
    }

    private TreeItem<DeviceValueObject>
            getTreeItemWithListener(DeviceValueObject obj) {
        TreeItem itemLabel = new TreeItem(obj.getHostName());
        TreeItem<DeviceValueObject> treeItem = new TreeItem<>(obj);
        obj.disconnectedProperty().addListener((observable, before, now) -> {
            if (before.booleanValue() != now.booleanValue()) {
                moveDeviceNodeToOtherBranch(treeItem.getParent());
            }
        });
        itemLabel.getChildren().add(treeItem);
        return itemLabel;
    }

    private static void moveDeviceNodeToOtherBranch(TreeItem item) {
        TreeItem parentNode = item.getParent();
        parentNode.getChildren().remove(item);
        TreeItem nextNode = parentNode.nextSibling();
        if (nextNode == null) {
            parentNode.previousSibling().getChildren().add(item);
        } else {
            nextNode.getChildren().add(item);
        }
    }

    /**
     * Returns selected item in the device tree. Note that the return value is
     * not bound to any value even though having generic value assigned to it.
     *
     * @return
     */
    private TreeItem getSelectedItem() {
        return devicesTree.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void connectToDeviceHandler(MouseEvent event) {
        TreeItem device = getSelectedItem();
        connectingToDevice.setValue(true);
        new Thread(new ConnectionWorker(device)).start();
    }

    private void doubleClickHandler(MouseEvent event) {
        TreeItem sel = devicesTree.getSelectionModel().getSelectedItem();
        if (sel.getValue() instanceof DeviceValueObject) {
            if (sel.getParent().getParent().nextSibling() == null) {
                connectToDeviceHandler(event);
            } else {
                disconnectHandler(event);
            }
        }
    }

    @FXML
    private void disconnectHandler(MouseEvent event) {
        boolean okToProceed = ControllerUtils
                .showConfirmDialog(StringConstants.OK_TO_DISCONNECT);
        if (okToProceed) {
            disconnect();
        }
    }

    private void disconnect() {
        InetAddress addrSelected;
        try {
            addrSelected = InetAddress.getByName(devicesTab
                    .getSelectionModel().selectedItemProperty().get().getId());
            NetworkManager.disconnect(addrSelected);
            InterruptManager.clearAllListeners(addrSelected);
            App.removeTab(addrSelected);
        } catch (UnknownHostException ex) {
            LOGGER.debug("ip address / host not resolved.");
            return;
        }
        TreeItem<Object> active = devicesTree.getRoot()
                .getChildren().get(ACTIVE_BRANCH_INDEX);
        active.getChildren().forEach((item) -> {
            String val = (String) item.getValue();
            if (val.equals(addrSelected.getHostAddress())) {
                DeviceValueObject device = (DeviceValueObject) item
                        .getChildren().get(0).getValue();
                device.disconnectedProperty().set(true);
            }
        });
    }

    private class ConnectionWorker extends Task<Boolean> {

        private final DeviceValueObject device;

        ConnectionWorker(TreeItem selectedItem) {
            this.device = (DeviceValueObject) selectedItem.getValue();
        }

        private void notifyConnectingFailed() {
            Platform.runLater(() -> {
                ControllerUtils.showErrorDialog(String.format(
                        StringConstants.F_HOST_NOT_REACHABLE.toString(),
                        device.getHostName())
                );
            });
        }

        @Override
        protected Boolean call() {
            try {
                boolean isHostReachable = device.getAddress()
                        .isReachable(NetworkManager.TIMEOUT);
                if (isHostReachable) {
                    LOGGER.debug(String.format("Host %s is reachable",
                            device.getHostName()));
                    return true;
                } else {
                    notifyConnectingFailed();
                }
            } catch (IOException ex) {
                LOGGER.error(null, ex);
            }
            return false;
        }

        @Override
        protected void done() {
            try {
                if (get() && NETWORK_MANAGER.connectToDevice(device)) {
                    devicesTree.refresh();
                }
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.error(null, ex);
            }
            connectingToDevice.set(false);
        }
    }
}
