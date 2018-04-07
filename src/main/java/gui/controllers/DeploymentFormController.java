package gui.controllers;

import gui.deployer.SshData;
import gui.userdata.xstream.XStreamUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.NetworkingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SshWrapper;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class DeploymentFormController implements Initializable {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DeploymentFormController.class);
    @FXML
    private Button deployBtn;
    @FXML
    private ComboBox<String> addressField;
    @FXML
    private TextField usernameField;
    @FXML
    private ToggleGroup auth;
    @FXML
    private RadioButton noneBtn;
    @FXML
    private RadioButton passwdBtn;
    @FXML
    private RadioButton certBtn;
    @FXML
    private PasswordField passwdField;
    @FXML
    private ToggleGroup jar;
    @FXML
    private Label localJar;
    @FXML
    private RadioButton localBtn;
    @FXML
    private RadioButton remoteBtn;
    @FXML
    private ComboBox<String> remoteJar;
    @FXML
    private ProgressIndicator ipProgress;
    private StringProperty jarPath = new SimpleStringProperty(null);
    private FileChooser fc;
    private MasterWindowController mwc;

    private void initFileChooser() {
        fc = new FileChooser();
        fc.setTitle("Choose agent JAR");
        fc.getExtensionFilters().add(new FileChooser
                .ExtensionFilter("JAR archive (*.jar)", "*.jar"));
    }

    public DeploymentFormController(MasterWindowController mwc) {
        this.mwc = mwc;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initFileChooser();
        addressField.setEditable(true);
        ObservableList<String> list = FXCollections.observableArrayList();
        XStreamUtils.getDevices().forEach(deviceValueObject
                -> list.add(deviceValueObject.getHostName()));
        addressField.setItems(list);
        deployBtn.disableProperty().bind(
                addressField.editorProperty().get().textProperty().isEmpty()
                        .or(usernameField.textProperty().isEmpty())
                        .or(jarPath.isEmpty()));
        deployBtn.setOnAction(e -> {
            ipProgress.setVisible(true);
            ReachabilityWorker cw = new ReachabilityWorker(this);
            cw.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                    event -> {
                        // continue with deployment only if IP is reachable,
                        // i.e. worker succeeded
                        //if (cw.getValue()) {
                        //    System.out.println("HOORAY!");
                        //}
                    });
            Thread t = new Thread(cw);
            t.start();
        });
        localBtn.setOnMouseClicked(event -> {
            File selected = fc.showOpenDialog(new Stage());
            String selectedPath = selected == null ? null : selected.getAbsolutePath();
            jarPath.setValue(selectedPath);
            localJar.setText(selectedPath);
            mwc.requestDeploymentDialogFocus();
        });
        remoteBtn.setOnMouseClicked(e -> {
            ReachabilityWorker cw = new ReachabilityWorker(this);
            cw.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
                InetAddress ia = cw.getValue();
                if (ia != null) {
                    SshData data = new SshData.Builder()
                            .inetAddress(ia)
                            .password(passwdField.getText().getBytes())
                            .username(usernameField.getText())
                            .build();
                    LOGGER.debug(data.toString());
                    try (SshWrapper wrapper = new SshWrapper(data)) {
                        List<String> a = wrapper.getRemoteCommandOutput(
                                "find ~ -maxdepth 1 -name \"*[A|a]gent*.jar\"");
                        remoteJar.setItems(FXCollections.observableArrayList(a));
                    } catch (IOException ioe) {
                        LOGGER.debug("SSH connection creation failed.", ioe);
                    }
                }
            });
            new Thread(cw).start();
        });
    }

    private class ReachabilityWorker extends Task<InetAddress> {
        private DeploymentFormController controller;

        ReachabilityWorker(DeploymentFormController controller) {
            this.controller = controller;
        }

        @Override
        protected InetAddress call() {
            String ipString = controller.
                    addressField.editorProperty().get().getText();
            if (ipString.isEmpty()) {
                return null;
            }
            InetAddress ia = NetworkingUtils.getAddressFromHostname(ipString);
            if (ia == null || NetworkingUtils.isNotReachable(ia)) {
                return null;
            }
            return ia;
        }

        @Override
        public InetAddress get() {
            try {
                return super.get();
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
        }

        @Override
        protected void done() {
            if (get() == null) {
                controller.addressField.setBackground(new Background(
                        new BackgroundFill(Paint.valueOf("#FF2222"),
                                CornerRadii.EMPTY, Insets.EMPTY)));
            } else {
                controller.addressField.setBackground(Background.EMPTY);
            }
            controller.ipProgress.setVisible(false);
        }
    }

    private class AuthenticationWorker extends Task {
        private DeploymentFormController controller;

        AuthenticationWorker(DeploymentFormController controller) {
            this.controller = controller;
        }

        @Override
        protected Boolean call() {
            return null;
        }
    }
}
