package gui.controllers;

import gui.misc.SshData;
import gui.userdata.xstream.XStreamUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.NetworkingUtils;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SshWrapper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public final class DeploymentForm implements Initializable {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DeploymentForm.class);
    @FXML
    private Button deployBtn;
    @FXML
    private ComboBox<String> addressField;
    @FXML
    private TextField usernameField;
    @FXML
    private ToggleGroup auth;
    @FXML
    private RadioButton passwdBtn;
    @FXML
    private RadioButton keyBtn;
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
    @FXML
    private Button remoteHelpBtn;

    private StringProperty jarPath = new SimpleStringProperty(null);
    private FileChooser jarFc;
    private MasterWindow mwc;
    private PopOver remoteHelp = new PopOver();

    private void initJarFileChooser() {
        jarFc = new FileChooser();
        jarFc.setTitle("Choose agent JAR");
        jarFc.getExtensionFilters().add(new FileChooser
                .ExtensionFilter("JAR archive (*.jar)", "*.jar"));
    }

    public DeploymentForm(MasterWindow mwc) {
        this.mwc = mwc;
    }

    private SshData getSshWrapper(InetAddress ia) {
        return new SshData.Builder().inetAddress(ia)
                .password(keyBtn.isSelected() ? new byte[]{}
                        : passwdField.getText().getBytes())
                .username(usernameField.getText()).build();
    }

    private RemoteCmdWorker remoteCmdWorker(InetAddress ia, String command) {
        return new RemoteCmdWorker(getSshWrapper(ia), command);
    }

    private AgentWorker agentWorker(InetAddress ia, OutputStream os) {
        return new AgentWorker(getSshWrapper(ia), os);
    }

    private CopyJarWorker copyJarWorker(InetAddress ia, OutputStream os) {
        return new CopyJarWorker(getSshWrapper(ia), os);
    }

    private void initRemoteDeploymentNodes() {
        Label l = new Label("Once you click on the \"Remote\" "
                + "button, client will try to search for jar files located "
                + "in the user home folder. Please note that you must fill in "
                + "the authentication data first.The list will then be "
                + "available in the combo box.");
        l.setPadding(new Insets(5));
        l.setWrapText(true);
        l.setPrefWidth(250);
        remoteHelp.setContentNode(l);
        remoteHelp.setArrowLocation(PopOver.ArrowLocation.BOTTOM_CENTER);
        remoteHelpBtn.setOnAction(e -> {
            if (remoteHelp.isShowing()) {
                remoteHelp.hide();
            } else {
                remoteHelp.show(remoteHelpBtn);
            }
        });
        remoteJar.getSelectionModel().selectedItemProperty()
                .addListener((ign, ign2, n) -> jarPath.setValue(n));
        remoteJar.disableProperty().bind(addressField.editorProperty().get()
                .textProperty().isEmpty()
                .or(usernameField.textProperty().isEmpty()));
        remoteBtn.disableProperty().bind(addressField.editorProperty().
                get().textProperty().isEmpty()
                .or(usernameField.textProperty().isEmpty()));
        remoteBtn.setOnMouseClicked(e -> {
            ReachabilityWorker cw = new ReachabilityWorker(this);
            cw.setOnSucceeded(event -> {
                InetAddress ia = cw.getValue();
                if (ia != null) {
                    RemoteCmdWorker aw = remoteCmdWorker(ia, "find ~ -maxdepth "
                            + "1 -name \"*[A|a]gent*.jar\"");
                    aw.setOnSucceeded(ev -> remoteJar.setItems(
                            FXCollections.observableArrayList(aw.getValue())));
                    aw.setOnFailed(ev ->
                            LOGGER.debug("Remote command execution failed."));
                    new Thread(aw).start();
                }
            });
            cw.setOnFailed(event -> {
                LOGGER.debug("Could not reach remote host.");
            });
            new Thread(cw).start();
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initJarFileChooser();
        initRemoteDeploymentNodes();
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
            cw.setOnSucceeded(event -> {
                InetAddress ia = cw.getValue();
                if (ia != null) {
                    if (localBtn.isSelected()) {
                        CopyJarWorker cjw = copyJarWorker(ia, System.out);
                        cjw.setOnSucceeded(a -> {
                            if (cjw.getValue()) {
                                AgentWorker aw = agentWorker(ia, System.out);
                                new Thread(aw).start();
                            }
                        });
                        new Thread(cjw).start();
                    } else if (remoteBtn.isSelected()) {
                        AgentWorker aw = agentWorker(ia, System.out);
                        new Thread(aw).start();
                    }
                }

            });
            Thread t = new Thread(cw);
            t.start();
        });
        localBtn.setOnMouseClicked(event -> {
            File selected = jarFc.showOpenDialog(new Stage());
            String selectedPath = selected == null ? null
                    : selected.getAbsolutePath();
            jarPath.setValue(selectedPath);
            localJar.setText(selectedPath);
            mwc.requestDeploymentDialogFocus();
        });
    }

    private class ReachabilityWorker extends Task<InetAddress> {
        private DeploymentForm controller;

        ReachabilityWorker(DeploymentForm controller) {
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

    private class RemoteCmdWorker extends Task<List<String>> {
        private String command;
        private SshData data;

        RemoteCmdWorker(SshData data, String command) {
            this.data = data;
            this.command = command;
        }

        @Override
        protected List<String> call() {
            LOGGER.debug(data.toString());
            try (SshWrapper wrapper = new SshWrapper(data)) {
                return wrapper.scanRemoteOutput(command);
            } catch (IOException ioe) {
                LOGGER.debug("SSH connection creation failed.", ioe);
                return Collections.emptyList();
            }
        }
    }

    private class AgentWorker extends Task<Void> {
        private SshData data;
        private OutputStream os;
        private String command;

        AgentWorker(SshData data, OutputStream os) {
            this.data = data;
            this.os = os;
            String[] jarPathSplit = jarPath.get().split("/");
            this.command = "java -jar " + jarPathSplit[jarPathSplit.length - 1];
        }

        @Override
        protected Void call() {
            LOGGER.debug(data.toString());
            try (SshWrapper wrapper = new SshWrapper(data)) {
                wrapper.launchRemoteCommand(command, os);
            } catch (IOException ioe) {
                LOGGER.debug("SSH connection creation failed.", ioe);
            }
            return null;
        }

        @Override
        protected void done() {

        }
    }

    private class CopyJarWorker extends Task<Boolean> {
        private final SshData data;
        private final String jarPathStr = jarPath.get();
        private final OutputStream os;

        CopyJarWorker(SshData data, OutputStream os) {
            this.data = data;
            this.os = os;
        }

        @Override
        protected Boolean call() {
            LOGGER.debug(String.format("Copying JAR '%s' to remote host '%s'.",
                    jarPathStr, data.getInetAddress().getHostAddress()));
            try (SshWrapper wrapper = new SshWrapper(data)) {
                return wrapper.uploadFileToRemoteServer(new File(jarPathStr));
            } catch (IOException ioe) {
                LOGGER.debug("SSH connection creation failed.", ioe);
                return false;
            }
        }
    }
}
