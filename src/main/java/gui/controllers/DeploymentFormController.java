package gui.controllers;

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

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
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
                        if (cw.getValue()) {
                            System.out.println("HOORAY!");
                        }
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
    }

    private abstract class DeploymentWorker extends Task<Boolean> {
        protected final DeploymentFormController controller;

        DeploymentWorker(DeploymentFormController controller) {
            this.controller = controller;
        }

        @Override
        public final Boolean get() {
            try {
                return super.get();
            } catch (InterruptedException | ExecutionException e) {
                return false;
            }
        }
    }

    private class ReachabilityWorker extends DeploymentWorker {
        ReachabilityWorker(DeploymentFormController controller) {
            super(controller);
        }

        @Override
        protected Boolean call() {
            InetAddress ia = NetworkingUtils.getAddressFromHostname(controller.
                    addressField.editorProperty().get().getText());
            if (ia == null) {
                return false;
            }
            return !NetworkingUtils.isNotReachable(ia);
        }

        @Override
        protected void done() {
            if (!get()) {
                controller.addressField.setBackground(new Background(
                        new BackgroundFill(Paint.valueOf("#FF2222"),
                                CornerRadii.EMPTY, Insets.EMPTY)));
            } else {
                controller.addressField.setBackground(Background.EMPTY);
            }
            controller.ipProgress.setVisible(false);
        }
    }

    private class AuthenticationWorker extends DeploymentWorker {
        AuthenticationWorker(DeploymentFormController controller) {
            super(controller);
        }

        @Override
        protected Boolean call() {
            return null;
        }
    }
}
