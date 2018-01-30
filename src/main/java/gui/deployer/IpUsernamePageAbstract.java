package gui.deployer;

import gui.controllers.ControllerUtils;
import gui.userdata.xstream.XStreamUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.NetworkingUtils;
import util.StringConstants;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

class IpUsernamePageAbstract extends AbstractWizardPage {

    private ProgressIndicator pi;

    IpUsernamePageAbstract() {
        super("IP address and username");
        priorButton.setVisible(false);
        finishButton.setVisible(false);
    }

    ProgressIndicator getPi() {
        return pi;
    }

    @Override
    Parent getContent() {
        pi = new ProgressIndicator();
        pi.setPrefHeight(20);
        pi.setVisible(false);
        ComboBox<String> addrComboBox = new ComboBox<>();
        addrComboBox.setEditable(true);
        ObservableList<String> list = FXCollections.observableArrayList();
        XStreamUtils.getDevices().forEach(deviceValueObject -> list.add(deviceValueObject.getHostName()));
        addrComboBox.setItems(list);
        SshData.instance.ipAddress.bind(addrComboBox.editorProperty().get().textProperty());

        Label l = new Label("Enter the IP address or hostname of the device:");
        l.setWrapText(true);


        TextField username = new TextField();
        SshData.instance.username.bind(username.textProperty());
        Label l2 = new Label("Enter the username you " +
                "want to authenticate with:");
        l.setWrapText(true);

        nextButton.disableProperty().bind(addrComboBox
                .getSelectionModel().selectedItemProperty().isNull()
                .or(username.textProperty().isEmpty()));
        nextButton.setOnAction(e -> {
            try {
                new Thread(new ConnectionWorker(this,
                        InetAddress.getByName(SshData.instance.ipAddress.get())))
                        .start();
            } catch (IOException ie) {
                // ok
            }
        });
        return new VBox(5, l, new HBox(addrComboBox, pi), l2, username);
    }

    private class ConnectionWorker extends Task<Boolean> {

        private final InetAddress ia;
        private IpUsernamePageAbstract page;

        ConnectionWorker(IpUsernamePageAbstract page, InetAddress ia) {
            Objects.requireNonNull(page, "page null");
            this.page = page;
            this.ia = ia;
        }

        private void notifyConnectingFailed() {
            Platform.runLater(() -> ControllerUtils.showErrorDialog(
                    String.format(StringConstants.F_HOST_NOT_REACHABLE, ia.getHostName())
            ));
        }

        @Override
        protected Boolean call() {
            page.getPi().setVisible(true);
            if (!NetworkingUtils.isReachable(ia)) {
                notifyConnectingFailed();
                return false;
            }
            return true;

        }

        @Override
        protected void done() {
            //page.getPi().setVisible(false);
            try {
                if (get()) {
                    Platform.runLater(() -> page.nextPage());
                }
            } catch (InterruptedException | ExecutionException ex) {
                //LOGGER.error(null, ex);
            }
        }
    }
}
