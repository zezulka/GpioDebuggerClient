package gui.controllers;

import gui.misc.SshData;
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

public final class DeploymentFormController implements Initializable {
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
    @FXML
    private Button remoteHelpBtn;

    private StringProperty jarPath = new SimpleStringProperty(null);
    private FileChooser fc;
    private MasterWindowController mwc;
    private PopOver remoteHelp = new PopOver();

    private void initFileChooser() {
        fc = new FileChooser();
        fc.setTitle("Choose agent JAR");
        fc.getExtensionFilters().add(new FileChooser
                .ExtensionFilter("JAR archive (*.jar)", "*.jar"));
    }

    public DeploymentFormController(MasterWindowController mwc) {
        this.mwc = mwc;
    }

    private RemoteCmdWorker agent(InetAddress ia, String command) {
        return new RemoteCmdWorker(new SshData.Builder()
                .inetAddress(ia)
                .password(passwdField.getText().getBytes())
                .username(usernameField.getText())
                .build(), command);
    }

    private AgentWorker contAgent(InetAddress ia, OutputStream os) {
        return new AgentWorker(new SshData.Builder()
                .inetAddress(ia)
                .password(passwdField.getText().getBytes())
                .username(usernameField.getText())
                .build(), os);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initFileChooser();
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
        remoteBtn.disableProperty().bind(addressField.editorProperty().
                get().textProperty().isEmpty()
                .or(usernameField.textProperty().isEmpty()));
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
                        InetAddress ia = cw.getValue();
                        if (ia != null && remoteBtn.isSelected()) {
                            AgentWorker aw = contAgent(ia, System.out);
                            new Thread(aw).start();
                        }
                    });
            Thread t = new Thread(cw);
            t.start();
        });
        localBtn.setOnMouseClicked(event -> {
            File selected = fc.showOpenDialog(new Stage());
            String selectedPath = selected == null ? null
                    : selected.getAbsolutePath();
            jarPath.setValue(selectedPath);
            localJar.setText(selectedPath);
            mwc.requestDeploymentDialogFocus();
        });
        remoteJar.getSelectionModel().selectedItemProperty()
                .addListener((ign, ign2, n) -> jarPath.setValue(n));
        remoteBtn.setOnMouseClicked(e -> {
            ReachabilityWorker cw = new ReachabilityWorker(this);
            cw.setOnSucceeded(event -> {
                InetAddress ia = cw.getValue();
                if (ia != null) {
                    RemoteCmdWorker aw = agent(ia, "find ~ -maxdepth 1 -name "
                            + "\"*[A|a]gent*.jar\"");
                    aw.setOnSucceeded(ev -> remoteJar.setItems(
                            FXCollections.observableArrayList(aw.getValue())));
                    new Thread(aw).start();
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

    private class AgentWorker extends Task<Integer> {
        // Expect the command output to be the following:
        // first line:   [#] PID
        // second line:  java -jar <JAR_NAME>
        // the rest:     agent logger output
        private final String command = "java -jar " + jarPath.get() + " & fg";
        private SshData data;
        private OutputStream os;

        AgentWorker(SshData data, OutputStream os) {
            this.data = data;
            this.os = os;
        }

        @Override
        protected Integer call() {
            LOGGER.debug(data.toString());
            List<String> init = Collections.emptyList();
            try (SshWrapper wrapper = new SshWrapper(data)) {
                init = wrapper.scanAgentOutput(0, command, os);
                /*if(init.size() < 2) {
                    return null;
                }
                String[] split = init.get(0).split(" ");
                if(split.length != 2) {
                    return null;
                }*/
                return 0;
                //return Integer.getInteger(split[1]);
            } catch (IOException ioe) {
                LOGGER.debug("SSH connection creation failed.", ioe);
            } catch (NumberFormatException nfe) {
                LOGGER.debug(String.format("Error while retrieving agent PID:"
                        + " unexpected parse input: %s.", init));
            }
            return null;
        }

        @Override
        protected void done() {
            try {
                Integer pid = get();
                if (pid == null) {
                    LOGGER.debug("Could not retrieve agent PID.");
                } else {
                    MenuItem mi = new MenuItem(pid.toString());
                    mi.setOnAction(e -> {
                        System.out.println(data.getInetAddress().getHostAddress());
                    });
                    mwc.addNewAgent(mi);
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.debug(e.getMessage());
            }

        }
    }
}
