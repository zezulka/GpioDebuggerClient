package gui.controllers;

import gui.SwitchButton;
import gui.misc.Graphics;
import gui.tab.loader.TabManager;
import gui.tab.loader.TabManagerImpl;
import gui.userdata.DeviceValueObject;
import gui.userdata.xstream.XStreamUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import net.NetworkManager;
import net.NetworkingUtils;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.InterruptManager;
import util.StringConstants;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public final class MasterWindow implements Initializable {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(MasterWindow.class);
    private static final NetworkManager NETWORK_MANAGER
            = NetworkManager.getInstance();
    private static final int HISTORY_BRANCH = 1;
    private static final int ACTIVE_BRANCH_INDEX = 0;
    private static TabManager manager = null;
    private final PopOver deviceInfo = new PopOver();
    private final PopOver deployDialog = new PopOver();
    private final BooleanProperty connectingToDevice
            = new SimpleBooleanProperty(false);
    private final ObservableList<DeviceValueObject> devices =
            FXCollections.observableList(XStreamUtils.getDevices());

    @FXML
    private TabPane devicesTab;
    @FXML
    private BorderPane treePane;
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
    private Button deployButton;
    @FXML
    private ToolBar toolBar;

    public static TabManager getTabManager() {
        return manager;
    }

    ObservableList<DeviceValueObject> getDevices() {
        return devices;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDeviceTree();
        initToolbar();
        manager = new TabManagerImpl(devicesTab);
        deviceInfo.setTitle("Device info");
        deviceInfo.setAnimated(false);
        deviceInfo.setDetachable(false);
        deployDialog.setTitle("Deploy agent remotely");
        FXMLLoader formLoader = new FXMLLoader(Utils.DEPLOYMENT_FORM);
        formLoader.setController(new DeploymentForm(this));
        try {
            deployDialog.setContentNode(formLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        deviceInfo.setHeaderAlwaysVisible(true);
        deployDialog.setHeaderAlwaysVisible(true);
        deployDialog.setConsumeAutoHidingEvents(true);
    }

    public void requestDeploymentDialogFocus() {
        deployDialog.show(deployButton);
    }

    private void initToolbar() {
        initDeviceTreeSwitch();
        initConnectBtn();
        initDisconnectBtn();
        initAddNewDeviceBtn();
        connectBtn.setOnAction(t -> connectToDeviceHandler());
        disconnectButton.setOnAction(t -> disconnectHandler());

        connectingToDevice.addListener((o, old, pending) -> Platform.runLater(()
                -> connectBtn.setText(pending ? "Connecting..." : "Connect")
        ));
        deployButton.setOnAction(e -> {
            if (deployDialog.isShowing()) {
                deployDialog.hide();
            } else {
                deployDialog.setArrowLocation(
                        PopOver.ArrowLocation.BOTTOM_LEFT);
                deployDialog.show(deployButton);
            }
        });
    }

    private void initDeviceTreeSwitch() {
        SwitchButton deviceTreeSwitch = new SwitchButton();
        toolBar.getItems().add(0, deviceTreeSwitch);
        deviceTreeSwitch.switchOnProperty().set(true);
        deviceTreeSwitch.switchOnProperty().addListener(
                (o, old, selected) -> deviceTreeBtnListener(selected));
    }

    private void deviceTreeBtnListener(boolean isSelected) {
        connectBtn.visibleProperty().set(isSelected);
        disconnectButton.visibleProperty().set(isSelected);
        treePane.setVisible(isSelected);
        treePane.managedProperty().bind(treePane.visibleProperty());
    }

    private void initConnectBtn() {
        initToolbarButton(connectBtn, Graphics.CONNECT,
                StringConstants.TOOLTIP_HINT_CONNECT_BTN);
        connectBtn.disableProperty()
                .bind(connectionPending().or(rootOrChildrenSelected(true)));
    }

    private void initDisconnectBtn() {
        initToolbarButton(disconnectButton, Graphics.DISCONNECT,
                StringConstants.TOOLTIP_HINT_DISCONNECT_BTN);
        disconnectButton.disableProperty().bind(rootOrChildrenSelected(false));
    }

    private BooleanBinding rootOrChildrenSelected(boolean desiredValue) {
        return Bindings.createBooleanBinding(() -> {
            TreeItem<DeviceValueObject> selectedItem = getSelectedItem();
            return !(selectedItem != null && selectedItem.isLeaf()
                    && selectedItem.getParent().getParent() != null
                    && (selectedItem.getValue().disconnectedProperty()
                    .getValue() == desiredValue));
        }, devicesTree.getSelectionModel().selectedItemProperty());
    }

    void addNewDevice(DeviceValueObject device) {
        if (device != null) {
            ipAddress.textProperty().set("");
            if (!devices.contains(device)) {
                device.setDirty(true);
                devices.add(device);
                XStreamUtils.addNewDeviceToFile(device);
            }
        }
    }

    private void initAddNewDeviceBtn() {
        addNewDeviceButton.disableProperty()
                .bind(ipAddress.textProperty().isEmpty());
        addNewDeviceButton.setOnAction((event) ->
                addNewDevice(createNewDeviceTextfield()));
    }

    private BooleanBinding connectionPending() {
        BooleanBinding binding =
                Bindings.createBooleanBinding(connectingToDevice::get,
                        connectingToDevice);
        return Bindings.when(binding).then(true).otherwise(false);
    }

    private DeviceValueObject createNewDeviceTextfield() {
        String cand = ipAddress.getText();
        InetAddress inetAddr = NetworkingUtils.getAddressFromHostname(cand);
        if (inetAddr == null) {
            LOGGER.debug("Could not resolve the following hostname: " + cand);
            Utils.showErrorDialog("Unknown hostname.");
            return null;
        }
        return new DeviceValueObject(inetAddr);
    }

    private void initToolbarButton(ButtonBase button,
                                   ImageView buttonImageView,
                                   String tooltipText) {
        button.setGraphic(buttonImageView);
        Tooltip tooltip = new Tooltip(tooltipText);
        button.setTooltip(tooltip);
    }

    private void initDeviceTree() {
        TreeItem<DeviceValueObject> root = new TreeItem<>();
        TreeItem<DeviceValueObject> active = new TreeItem<>();
        TreeItem<DeviceValueObject> hist = new TreeItem<>();
        List<TreeItem<DeviceValueObject>> nodes
                = Arrays.asList(root, active, hist);
        nodes.forEach(node -> {
            node.setExpanded(true);
            node.addEventHandler(TreeItem.branchCollapsedEvent(),
                    t -> t.getTreeItem().setExpanded(true));
        });
        devices.addListener((ListChangeListener<DeviceValueObject>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    assert change.getAddedSize() == 1;
                    DeviceValueObject newItem = change.getAddedSubList().get(0);
                    hist.getChildren().add(getTreeItemWithListener(newItem));
                    break;
                }
            }
        });
        devices.forEach(device -> hist.getChildren().add(
                getTreeItemWithListener(device)));
        root.getChildren().addAll(active, hist);
        devicesTree.setCellFactory(e -> new DeviceTreeCell());
        devicesTree.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
                doubleClickHandler();
                event.consume();
            }
        });
        devicesTree.setRoot(root);
        devicesTree.setShowRoot(false);
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
        boolean okToProceed = Utils
                .showConfirmDialog(StringConstants.OK_TO_DISCONNECT);
        if (okToProceed) {
            disconnect();
        }
    }

    private void disconnect() {
        InetAddress addrSelected;
        addrSelected = NetworkingUtils.getAddressFromHostname(devicesTab
                .getSelectionModel().selectedItemProperty().get().getId());
        NetworkManager.disconnect(addrSelected);
        InterruptManager.clearAllListeners(addrSelected);
        manager.removeTab(addrSelected);
        if (addrSelected != null) {
            TreeItem<DeviceValueObject> active = devicesTree.getRoot()
                    .getChildren().get(ACTIVE_BRANCH_INDEX);
            active.getChildren().forEach((item) -> {
                DeviceValueObject val = item.getValue();
                if (val.getAddress().equals(addrSelected)) {
                    val.disconnectedProperty().set(true);
                }
            });
        }
    }

    private class ConnectionWorker extends Task<Boolean> {

        private final DeviceValueObject device;

        ConnectionWorker(DeviceValueObject device) {
            Objects.requireNonNull(device, "device null");
            this.device = device;
        }

        private void notifyConnectingFailed() {
            Platform.runLater(() -> Utils.showErrorDialog(
                    String.format(StringConstants.F_HOST_NOT_REACHABLE,
                            device.getHostName())
            ));
        }

        @Override
        protected Boolean call() {
            connectingToDevice.setValue(true);
            if (NetworkingUtils.isNotReachable(device.getAddress())) {
                notifyConnectingFailed();
                return false;
            }
            LOGGER.debug(String.format("Host %s is reachable",
                    device.getHostName()));
            return true;

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

        private static final int PADDING_INSETS = 5;
        private static final int HBOX_SPACING = 10;
        private static final int COLUMN_CONSTRAINTS = 175;
        private static final int FONT_SIZE = 14;
        private static final int ROW_PADDING = 7;

        @Override
        protected void updateItem(DeviceValueObject device, boolean empty) {
            super.updateItem(device, empty);
            setDisclosureNode(null);
            if (isEmpty() && empty) {
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
            if (getTreeItem().nextSibling() != null) {
                labelStr = "Active";
                setGraphic(Graphics.ACTIVE);
            } else {
                labelStr = "History";
                setGraphic(Graphics.HISTORY);
            }
            setText(labelStr);
            paddingProperty().set(new Insets(PADDING_INSETS));
        }

        private void updateNonEmpty(DeviceValueObject item) {
            if (item != null) {
                HBox cellBox = new HBox(HBOX_SPACING);
                Button button = new Button(null, Graphics.info());
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
                                .add(new ColumnConstraints(COLUMN_CONSTRAINTS));
                        deviceInfo.setContentNode(p);
                        deviceInfo.setArrowLocation(
                                PopOver.ArrowLocation.BOTTOM_LEFT);
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
            label.paddingProperty().set(new Insets(ROW_PADDING));
        }

        private Label getDecoratedLabel(String text) {
            Text ipText = new Text(text);
            ipText.setFont(Font.font("Verdana", FontWeight.BOLD, FONT_SIZE));
            Label result = new Label(null, ipText);
            setRowPadding(result);
            return result;
        }
    }
}
