package layouts.controllers;

import core.gui.App;
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
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

import javafx.collections.ObservableList;
import core.util.StringConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.InterruptManager;
import userdata.DeviceValueObject;
import userdata.UserDataUtils;

public final class MasterWindowController implements Initializable {

    @FXML
    private TabPane devicesTab;
    @FXML
    private Button connectToDeviceButton;
    @FXML
    private TreeView<Object> devicesTree;
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
    @FXML
    private SplitPane rootSplitPane;

    private static final Logger LOGGER
            = LoggerFactory.getLogger(MasterWindowController.class);

    private static Tab currentTab = null;
    private static final NetworkManager NETWORK_MANAGER
            = NetworkManager.getInstance();

    private final BooleanProperty isConnectionEstablishmentPending
            = new SimpleBooleanProperty(false);

    private static final int ALL_BRANCH_INDEX = 1;
    private static final int ACTIVE_BRANCH_INDEX = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeToolbar();
        initializeDeviceTree();
    }

    private void initializeToolbar() {
        initializeToolbarButton(disconnectButton, Images.DISCONNECT,
                "Disconnects from device. "
                + "\nDevice must be selected in the device tree and active.");
        initializeToolbarButton(connectToDeviceButton, Images.CONNECT,
                "Connects to device. "
                + "\nDevice must be selected in the device tree.");
        initializeToolbarButton(deviceTree, Images.DEVICE_TREE,
                "Device tree browser.");

        initIndicator();

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
                childrenAllBranch.add(new TreeItem<>(newDevice));
                UserDataUtils.addNewDeviceToFile(newDevice);
            }
        });
        connectToDeviceButton.disableProperty().
                bind(isDeviceBranchCorrect(
                        (t) -> t.getParent().nextSibling() == null).not()
                );
        disconnectButton.disableProperty().
                bind(isDeviceBranchCorrect(
                        (t) -> t.getParent().nextSibling() != null).not()
                );
        devicesTab.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> currentTab = newVal);
        final double threshold = 0.7;
        deviceTree.setSelected(true);
        devicesTab.maxWidthProperty()
                .bind(rootSplitPane.widthProperty().multiply(threshold));
        deviceTree.selectedProperty()
                .addListener((observable, oldValue, toggled) -> {
                    final double dividerPos = toggled ? threshold : 1.0;
                    devicesTab.maxWidthProperty().bind(rootSplitPane
                            .widthProperty()
                            .multiply(dividerPos));
                    rootSplitPane.setDividerPositions(dividerPos);
                });
    }

    private void initIndicator() {
        BooleanBinding tfNotEmpty = ipAddress.textProperty().isNotEmpty();
        nowEnteringLabel.visibleProperty().bind(tfNotEmpty);
        indicationLabel.visibleProperty().bind(tfNotEmpty);
        indicationLabel.textProperty().bind(indicatorBinding());
    }

    private StringBinding indicatorBinding() {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> ipTextfieldContainsIpAddress(), ipAddress.textProperty());
        return Bindings.when(binding).then("IP address").otherwise("hostname");
    }

    private boolean ipTextfieldContainsIpAddress() {
        return InetAddressValidator.getInstance()
                .isValid(ipAddress.getText().trim());
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
                    && selectedItem.getValue() instanceof DeviceValueObject
                    && verifyFn.apply(selectedItem);
        }, devicesTree.getSelectionModel().selectedItemProperty());
        return Bindings.when(binding).then(true).otherwise(false);
    }

    private void initializeToolbarButton(ButtonBase button, Image buttonImage,
            String tooltipText) {
        button.setGraphic(new ImageView(buttonImage));
        Tooltip tooltip = new Tooltip(tooltipText);
        button.setTooltip(tooltip);
    }

    private void initializeDeviceTree() {

        TreeItem root = new TreeItem("devices", new ImageView(Images.DEVICES));
        TreeItem active = new TreeItem("alive", new ImageView(Images.ACTIVE));
        TreeItem hist = new TreeItem("history", new ImageView(Images.HISTORY));
        UserDataUtils
                .getDevices()
                .forEach((device)
                        -> hist
                        .getChildren()
                        .add(new TreeItem<>(device)));
        root.getChildren().addAll(active, hist);
        root.setExpanded(true);
        devicesTree.setRoot(root);
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
        new Thread(new ConnectionWorker(device)).start();
    }

    @FXML
    private void disconnectHandler(MouseEvent event) {
        boolean okToProceed = ControllerUtils
                .showConfirmDialog(StringConstants.OK_TO_DISCONNECT);
        if (okToProceed) {
            TreeItem selectedItem = getSelectedItem();
            DeviceValueObject device
                    = (DeviceValueObject) selectedItem.getValue();
            NetworkManager.disconnect(device.getAddress());
            InterruptManager.clearAllListeners(device.getAddress());
            moveDeviceNodeToOtherBranch(selectedItem);
            devicesTab
                    .getTabs()
                    .remove(App.getTabFromInetAddress(device.getAddress()));
        }
    }

    public static Tab getCurrentTab() {
        return MasterWindowController.currentTab;
    }

    private class ConnectionWorker extends Task<Boolean> {

        private final TreeItem selectedItem;
        private final DeviceValueObject device;

        ConnectionWorker(TreeItem selectedItem) {
            this.selectedItem = selectedItem;
            this.device = (DeviceValueObject) selectedItem.getValue();
        }

        private void informConnectingFailed() {
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
                boolean isHostReachable = device
                        .getAddress()
                        .isReachable(NetworkManager.TIMEOUT);
                if (isHostReachable) {
                    LOGGER.debug(String.format("Host %s is reachable",
                            device.getHostName()));
                    return true;
                } else {
                    informConnectingFailed();
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
