package gui.deployer;

import gui.controllers.ControllerUtils;
import gui.userdata.xstream.XStreamUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

class IpUsernamePage extends AbstractWizardPage {

    private ProgressIndicator pi;
    private Button nextButton;
    private ComboBox<String> addrComboBox;
    private TextField username;

    IpUsernamePage() {
        super("IP address and username");
    }

    @Override
    protected void initNodes() {
        pi = new ProgressIndicator();
        pi.setPrefHeight(20);
        pi.setVisible(false);

        username = new TextField();

        addrComboBox  = new ComboBox<>();
        addrComboBox.setEditable(true);

        nextButton = new Button("N_ext");
        nextButton.disableProperty().bind(addrComboBox
                .getSelectionModel().selectedItemProperty().isNull()
                .or(username.textProperty().isEmpty()));
        nextButton.setOnAction(e -> {
            /*try {
                //new Thread(new ConnectionWorker(this,
                //        InetAddress.getByName(SSH_DATA.getIpaddress())))
                //        .start();
            } catch (IOException ie) {
                // ok
            }*/
        });
    }

    @Override
    protected List<Button> getButtons() {
        return Arrays.asList(nextButton);
    }

    @Override
    protected Parent getContent() {
        ObservableList<String> list = FXCollections.observableArrayList();
        XStreamUtils.getDevices().forEach(deviceValueObject
                -> list.add(deviceValueObject.getHostName()));
        addrComboBox.setItems(list);
        //SSH_DATA.bindIpAddress(addrComboBox.editorProperty()
        //        .get().textProperty());
        Label l = new Label("Enter the IP address or hostname of the device:");
        l.setWrapText(true);
        //SSH_DATA.bindUsername(username.textProperty());
        Label l2 = new Label("Enter the username you "
                + "want to authenticate with:");
        l.setWrapText(true);
        return new VBox(5, l, new HBox(addrComboBox, pi), l2, username);
    }


}
