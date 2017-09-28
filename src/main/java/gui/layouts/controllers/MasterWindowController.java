package gui.layouts.controllers;

import gui.TabManagerImpl;
import gui.SwitchButton;
import gui.TabManager;
import core.net.NetworkManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
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
import javafx.scene.control.SplitPane;

import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;

import javafx.collections.ObservableList;
import core.util.StringConstants;
import javafx.scene.control.ToolBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.InterruptManager;
import gui.userdata.DeviceValueObject;
import gui.userdata.UserDataUtils;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.controlsfx.control.PopOver;

public final class MasterWindowController implements Initializable {

    private SwitchButton deviceTreeSwitch;
    @FXML
    private TabPane devicesTab;
    @FXML
    private Pane treePane;
    @FXML
    private Button connectBtn;
    @FXML
    private TreeView<DeviceValueObject> devicesTree;
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

    private final PopOver deviceInfo = new PopOver();

    private static final Logger LOGGER
            = LoggerFactory.getLogger(MasterWindowController.class);
    private static final NetworkManager NETWORK_MANAGER
            = NetworkManager.getInstance();
    private static TabManager manager;

    private final BooleanProperty connectingToDevice
            = new SimpleBooleanProperty(false);

    private static final int HISTORY_BRANCH = 1;
    private static final int ACTIVE_BRANCH_INDEX = 0;

    public MasterWindowController() {
    }

    public static TabManager getTabManager() {
        return manager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeDeviceTree();
        initializeToolbar();
        manager = new TabManagerImpl(devicesTab);
        deviceInfo.setTitle("Device info");
        deviceInfo.setHeaderAlwaysVisible(true);
        deviceInfo.setAnimated(false);
    }

    public TabPane getTabPane() {
        return devicesTab;
    }

    private void initializeToolbar() {
        initDeviceTreeSwitch();
        initConnectToDeviceBtn();
        initDisconnectBtn();
        initAddNewDeviceBtn();
        connectBtn.setOnAction(t -> connectToDeviceHandler());
        disconnectButton.setOnAction(t -> disconnectHandler());
        connectingToDevice.addListener((o, old, pending) -> {
            Platform.runLater(()
                    -> connectBtn.setText(pending ? "Connecting..." : "Connect")
            );
        });
        final double threshold = 0.75;
        devicesTab.maxWidthProperty()
                .bind(rootSplitPane.widthProperty().multiply(threshold));
    }

    private void initDeviceTreeSwitch() {
        deviceTreeSwitch = new SwitchButton();
        toolBar.getItems().add(0, deviceTreeSwitch);
        deviceTreeSwitch.switchOnProperty().set(true);
        deviceTreeSwitch.switchOnProperty().addListener((o, old, selected) -> {
            deviceTreeBtnListener(selected);
        });
    }

    private void deviceTreeBtnListener(boolean isSelected) {
        final double threshold = 0.75;
        connectBtn.visibleProperty().set(isSelected);
        disconnectButton.visibleProperty().set(isSelected);
        final double dividerPos = isSelected ? threshold : 1.0;
        devicesTab.maxWidthProperty().bind(rootSplitPane.widthProperty()
                .multiply(dividerPos));
        rootSplitPane.setDividerPositions(dividerPos);
    }

    private void initConnectToDeviceBtn() {
        initializeToolbarButton(connectBtn, Graphics.CONNECT,
                "Connects to device. "
                + "\nDevice must be selected in the device tree.");
        connectBtn.disableProperty()
                .bind(connectionPending().or(rootOrChildrenSelected(true)));
    }

    private void initDisconnectBtn() {
        initializeToolbarButton(disconnectButton, Graphics.DISCONNECT,
                "Disconnects from device. "
                + "\nDevice must be selected in the device tree and active.");
        disconnectButton.disableProperty().bind(rootOrChildrenSelected(false));
    }

    private BooleanBinding rootOrChildrenSelected(boolean desiredValue) {
        return Bindings.createBooleanBinding(() -> {
            TreeItem<DeviceValueObject> selectedItem = getSelectedItem();
            return !(selectedItem != null
                    && selectedItem.isLeaf()
                    && selectedItem.getParent().getParent() != null
                    && (selectedItem.getValue().disconnectedProperty()
                            .getValue() == desiredValue));
        }, devicesTree.getSelectionModel().selectedItemProperty());
    }

    private void initAddNewDeviceBtn() {
        addNewDeviceButton.disableProperty()
                .bind(ipAddress.textProperty().isEmpty());
        addNewDeviceButton.setOnAction((event) -> {
            DeviceValueObject newDevice = getNewDeviceFromUser();
            ObservableList<TreeItem<DeviceValueObject>> history = devicesTree
                    .getRoot()
                    .getChildren()
                    .get(HISTORY_BRANCH)
                    .getChildren();
            ObservableList<TreeItem<DeviceValueObject>> active = devicesTree
                    .getRoot()
                    .getChildren()
                    .get(ACTIVE_BRANCH_INDEX)
                    .getChildren();
            if (newDevice != null) {
                ipAddress.textProperty().set("");
                for (TreeItem<DeviceValueObject> obj : history) {
                    DeviceValueObject dvo = obj
                            .getChildren()
                            .get(0).getValue();
                    if (dvo.equals(newDevice)) {
                        devicesTree.getSelectionModel()
                                .select(obj.getChildren().get(0));
                        return;
                    }
                }
                for (TreeItem<DeviceValueObject> obj : active) {
                    DeviceValueObject dvo = obj
                            .getChildren()
                            .get(0).getValue();
                    if (dvo.equals(newDevice)) {
                        devicesTree.getSelectionModel()
                                .select(obj.getChildren().get(0));
                        return;
                    }
                }
                history.add(getTreeItemWithListener(newDevice));
                UserDataUtils.addNewDeviceToFile(newDevice);
            }
        });
    }

    private BooleanBinding connectionPending() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> connectingToDevice.get(), connectingToDevice);
        return Bindings.when(binding).then(true).otherwise(false);
    }

    private DeviceValueObject getNewDeviceFromUser() {
        try {
            InetAddress inetAddr = InetAddress.getByName(ipAddress.getText());
            return new DeviceValueObject(inetAddr);
        } catch (UnknownHostException ex) {
            LOGGER.debug("Invalid IP address");
            LOGGER.error(ex.getMessage());
            ControllerUtils.showErrorDialog("Unknown hostname.");
        }
        return null;
    }

    private void initializeToolbarButton(ButtonBase button,
            ImageView buttonImageView, String tooltipText) {
        button.setGraphic(buttonImageView);
        Tooltip tooltip = new Tooltip(tooltipText);
        button.setTooltip(tooltip);
    }

    private void initializeDeviceTree() {

        TreeItem<DeviceValueObject> root = new TreeItem<>();
        TreeItem<DeviceValueObject> active = new TreeItem<>();
        TreeItem<DeviceValueObject> hist = new TreeItem<>();
        root.setExpanded(true);
        active.setExpanded(true);
        hist.setExpanded(true);
        root.addEventHandler(TreeItem.branchCollapsedEvent(), (t) -> {
            t.getTreeItem().setExpanded(true);
        });
        active.addEventHandler(TreeItem.branchCollapsedEvent(), (t) -> {
            t.getTreeItem().setExpanded(true);
        });
        hist.addEventHandler(TreeItem.branchCollapsedEvent(), (t) -> {
            t.getTreeItem().setExpanded(true);
        });

        UserDataUtils.getDevices().forEach((device)
                -> hist.getChildren().add(getTreeItemWithListener(device)));
        root.getChildren().addAll(active, hist);
        devicesTree.setCellFactory(e -> new DeviceTreeCell());
        devicesTree.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
                doubleClickHandler();
                event.consume();
            }
        });
        devicesTree.setRoot(root);
    }

    private TreeItem<DeviceValueObject>
            getTreeItemWithListener(DeviceValueObject obj) {
        TreeItem<DeviceValueObject> treeItem
                = new TreeItem<>(obj, new Label(obj.getHostName()));
        obj.disconnectedProperty().addListener((observable, before, now) -> {
            if (before.booleanValue() != now.booleanValue()) {
                moveDeviceNodeToOtherBranch(treeItem);
            }
        });
        return treeItem;
    }

    private void moveDeviceNodeToOtherBranch(TreeItem<DeviceValueObject> item) {
        if (item.getParent().getParent() == null) {
            throw new RuntimeException(item.getValue().toString());
        }
        TreeItem<DeviceValueObject> parentNode = item.getParent();
        parentNode.getChildren().remove(item);
        TreeItem<DeviceValueObject> nextNode = parentNode.nextSibling();
        if (nextNode == null) {
            parentNode.previousSibling().getChildren().add(item);
        } else {
            nextNode.getChildren().add(item);
        }
    }

    /**
     * Returns selected item in the device tree.
     *
     * @return
     */
    private TreeItem<DeviceValueObject> getSelectedItem() {
        return devicesTree.getSelectionModel().getSelectedItem();
    }

    private void connectToDeviceHandler() {
        TreeItem<DeviceValueObject> selectedNode = getSelectedItem();
        if (selectedNode == null) {
            return;
        }
        DeviceValueObject correspondingDevice;
        if (selectedNode.isLeaf()) {
            correspondingDevice = selectedNode.getValue();
        } else {
            correspondingDevice = selectedNode.getChildren().get(0).getValue();
        }
        new Thread(new ConnectionWorker(correspondingDevice)).start();
    }

    private void doubleClickHandler() {
        if (!getSelectedItem().isLeaf()
                || getSelectedItem().getParent().getParent() == null) {
            return;
        }
        if (getSelectedItem().getParent().nextSibling() == null) {
            connectToDeviceHandler();
        } else {
            disconnectHandler();
        }
    }

    private void disconnectHandler() {
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
            manager.removeTab(addrSelected);
        } catch (UnknownHostException ex) {
            LOGGER.debug("unknown host");
            return;
        }
        TreeItem<DeviceValueObject> active = devicesTree.getRoot()
                .getChildren().get(ACTIVE_BRANCH_INDEX);
        active.getChildren().forEach((item) -> {
            DeviceValueObject val = item.getValue();
            if (val.getAddress().equals(addrSelected)) {
                val.disconnectedProperty().set(true);
            }
        });
    }

    private class ConnectionWorker extends Task<Boolean> {

        private final DeviceValueObject device;

        ConnectionWorker(DeviceValueObject device) {
            Objects.requireNonNull(device, "device null");
            this.device = device;
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
                connectingToDevice.setValue(true);
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

    private final class DeviceTreeCell extends TreeCell<DeviceValueObject> {
        
        @Override
        protected void updateItem(DeviceValueObject device, boolean empty) {
            super.updateItem(device, empty);
            setDisclosureNode(null);
            if(isEmpty() && empty) {
                setGraphic(null);
                setText(null);
                return;
            }
            if (getTreeItem() != null && device == null) {
                updateEmpty();
            } else {
                updateNonEmpty(device);
            }
        }

        private void updateEmpty() {
            String labelStr;
            if (getTreeItem().getParent() == null) {
                labelStr = "DEVICES TREE";
            } else if (getTreeItem().nextSibling() != null) {
                labelStr = "Active";
                setGraphic(Graphics.ACTIVE);
            } else {
                labelStr = "History";
                setGraphic(Graphics.HISTORY);
            }
            setText(labelStr);
            paddingProperty().set(new Insets(5));
        }

        private void updateNonEmpty(DeviceValueObject item) {
            if (item != null) {
                HBox cellBox = new HBox(10);
                Button button = new Button(null, new ImageView(Graphics.INFO));
                button.setStyle("-fx-background-radius: 100");
                button.setOnMouseClicked((event) -> {
                    if (deviceInfo.isShowing()) {
                        deviceInfo.hide();
                    } else {
                        Label ipLabel = getDecoratedLabel("IP address");
                        Label boardLabel = getDecoratedLabel("Board type");
                        Label timeLabel = getDecoratedLabel("Last connected");
                        Label ipContent
                                = new Label(item.getAddress().getHostAddress());
                        Label boardContent
                                = new Label(item.getBoardType().toString());
                        Label timeContent
                                = new Label(item.getTimeConnectedStr());

                        setRowPadding(ipContent);
                        setRowPadding(boardContent);
                        setRowPadding(timeContent);

                        GridPane p = new GridPane();

                        p.add(ipLabel, 0, 0);
                        p.add(ipContent, 1, 0);
                        p.add(boardLabel, 0, 1);
                        p.add(boardContent, 1, 1);
                        p.add(timeLabel, 0, 2);
                        p.add(timeContent, 1, 2);
                        p.getColumnConstraints()
                                .add(new ColumnConstraints(175));
                        deviceInfo.setContentNode(p);
                        deviceInfo.setArrowLocation(
                                PopOver.ArrowLocation.RIGHT_TOP);
                        deviceInfo.show(button);
                    }
                });
                button.setPadding(Insets.EMPTY);
                cellBox.getChildren().addAll(button);
                setGraphic(cellBox);
                setText(item.getHostName());
            }
        }

        private void setRowPadding(Label label) {
            label.paddingProperty().set(new Insets(7));
        }

        private Label getDecoratedLabel(String text) {
            Text ipText = new Text(text);
            ipText.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
            Label result = new Label(null, ipText);
            setRowPadding(result);
            return result;
        }
    }
}
