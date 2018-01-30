package core;

import gui.controllers.ControllerUtils;
import gui.userdata.xstream.XStreamUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.NetworkingUtils;
import props.AppPreferencesExtractor;
import util.SshWrapper;
import util.StringConstants;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

/**
 * basic wizard infrastructure class
 */
class Wizard extends StackPane {
    private static final int UNDEFINED = -1;
    private ObservableList<WizardPage> pages = FXCollections.observableArrayList();
    private Stack<Integer> history = new Stack<>();
    private int curPageIdx = UNDEFINED;
    private SshWrapper sshWrapper;

    Wizard(WizardPage... nodes) {
        pages.addAll(nodes);
        navTo(0);
        setStyle("-fx-padding: 10; -fx-background-color: cornsilk;");
    }

    void nextPage() {
        if (hasNextPage()) {
            navTo(curPageIdx + 1);
        }
    }

    SshWrapper getSshWrapper() {
        return sshWrapper;
    }

    void setSshWrapper(SshWrapper sshWrapper) {
        Objects.requireNonNull(sshWrapper);
        this.sshWrapper = sshWrapper;
    }

    void priorPage() {
        if (hasPriorPage()) {
            navTo(history.pop(), false);
        }
    }

    boolean hasNextPage() {
        return (curPageIdx < pages.size() - 1);
    }

    boolean hasPriorPage() {
        return !history.isEmpty();
    }

    void navTo(int nextPageIdx, boolean pushHistory) {
        if (nextPageIdx < 0 || nextPageIdx >= pages.size()) return;
        if (curPageIdx != UNDEFINED) {
            if (pushHistory) {
                history.push(curPageIdx);
            }
        }

        WizardPage nextPage = pages.get(nextPageIdx);
        curPageIdx = nextPageIdx;
        getChildren().clear();
        getChildren().add(nextPage);
        nextPage.manageButtons();
    }

    void navTo(int nextPageIdx) {
        navTo(nextPageIdx, true);
    }

    void navTo(String id) {
        if (id == null) {
            return;
        }

        pages.stream()
                .filter(page -> id.equals(page.getId()))
                .findFirst()
                .ifPresent(page ->
                        navTo(pages.indexOf(page))
                );
    }

    public void finish() {
        sshWrapper.close();
    }
}

/**
 * basic wizard page class
 */
abstract class WizardPage extends VBox {
    Button priorButton = new Button("_Previous");
    Button nextButton = new Button("N_ext");
    Button finishButton = new Button("_Finish");

    WizardPage(String title) {
        priorButton.setOnAction(event -> priorPage());
        nextButton.setOnAction(event -> nextPage());
        finishButton.setOnAction(event -> getWizard().finish());
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 5 0;");
        setId(title);
        setSpacing(5);
        setStyle("-fx-padding:10; -fx-background-color: honeydew; -fx-border-color: derive(honeydew, -30%); -fx-border-width: 3;");

        Region spring = new Region();
        VBox.setVgrow(spring, Priority.ALWAYS);
        getChildren().addAll(getContent(), spring, getButtons());
    }

    HBox getButtons() {
        Region spring = new Region();
        HBox.setHgrow(spring, Priority.ALWAYS);
        HBox buttonBar = new HBox(5);
        finishButton.setDefaultButton(true);
        buttonBar.getChildren().addAll(spring, priorButton, nextButton, finishButton);
        return buttonBar;
    }

    abstract Parent getContent();

    boolean hasNextPage() {
        return getWizard().hasNextPage();
    }

    boolean hasPriorPage() {
        return getWizard().hasPriorPage();
    }

    void nextPage() {
        getWizard().nextPage();
    }

    void priorPage() {
        getWizard().priorPage();
    }

    void navTo(String id) {
        getWizard().navTo(id);
    }

    Wizard getWizard() {
        return (Wizard) getParent();
    }

    public void manageButtons() {
        if (!hasPriorPage()) {
            priorButton.setDisable(true);
        }

        if (!hasNextPage()) {
            nextButton.setDisable(true);
        }
    }
}

/**
 * This class shows a satisfaction survey
 */
public class SurveyWizard extends Wizard {

    public SurveyWizard() {
        super(new IpUsernamePage(), new AuthPage(), new AgentJarPage());
    }

    public void finish() {
    }
}

class IpUsernamePage extends WizardPage {

    private ProgressIndicator pi;

    public IpUsernamePage() {
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
}

class ConnectionWorker extends Task<Boolean> {

    private final InetAddress ia;
    private IpUsernamePage page;

    ConnectionWorker(IpUsernamePage page, InetAddress ia) {
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

class AuthPage extends WizardPage {

    private RadioButton none;
    private RadioButton cert;
    private RadioButton passwd;
    private ToggleGroup options;

    AuthPage() {
        super("Authentication");

        none.setToggleGroup(options);
        cert.setToggleGroup(options);
        passwd.setToggleGroup(options);
        none.setSelected(true);
        finishButton.setVisible(false);
    }

    @Override
    Parent getContent() {
        none = new RadioButton("None");
        cert = new RadioButton("Certificate");
        passwd = new RadioButton("Password");
        options = new ToggleGroup();
        Label l = new Label("Select authentication method for the user " + SshData.instance.username.get());
        PasswordField pf = new PasswordField();
        l.setWrapText(true);
        SshData.instance.password.bind(pf.textProperty());
        nextButton.disableProperty().bind(passwd.selectedProperty().not().or(pf.textProperty().isEmpty()));
        nextButton.setOnAction(e -> {
            try {
                if (getWizard().getSshWrapper() != null) {
                    //check whether the username and ip are different... we want to be careful with resources
                } else {

                }
                getWizard().setSshWrapper(new SshWrapper(SshData.instance));
                List<String> str = getWizard()
                        .getSshWrapper()
                        .getRemoteCommandOutput("netstat -tulnp 2> /dev/null | grep "
                                + AppPreferencesExtractor.defaultSocketPort()
                                + " &> /dev/null ; echo $?");
                if (str.size() == 1 && str.get(0).equals("1")) {
                    nextPage();
                    return;
                } else if(str.size() == 1 && str.get(0).equals("0")) {
                    ControllerUtils.showErrorDialog("Agent is already running.");
                } else {
                    // netstat is not installed on the system, let's try another
                    List<String> strTwo = getWizard()
                            .getSshWrapper()
                            .getRemoteCommandOutput("ps aux | grep \"java -jar.*jar\" | wc -l");
                }
                ControllerUtils.showErrorDialog("Agent is already running.");
            } catch (IOException ioe) {
                // Swallow exception
            }
        });
        return new VBox(5, l, none, cert, passwd, pf);
    }
}

class AgentJarPage extends WizardPage {
    private RadioButton local;
    private RadioButton remote;
    private ToggleGroup options;

    public AgentJarPage() {
        super("Complaints");
        local.setToggleGroup(options);
        remote.setToggleGroup(options);
        options.selectedToggleProperty().addListener((observableValue, oldToggle, newToggle) -> {
            nextButton.setDisable(false);
        });
        nextButton.setVisible(false);
        priorButton.setVisible(false);
    }

    Parent getContent() {
        final String localStr = "Local...";
        local = new RadioButton(localStr);
        Label localFileLabel = new Label();
        remote = new RadioButton();
        options = new ToggleGroup();
        FileChooser fs = new FileChooser();
        fs.setTitle("Choose agent JAR");
        fs.getExtensionFilters().add(new FileChooser.ExtensionFilter("JAR archive (*.jar)", "*.jar"));
        local.setOnMouseClicked(mouseEvent -> {
            File f = fs.showOpenDialog(new Stage());
            if (f != null) {
                localFileLabel.setText(f.getAbsolutePath());
            }
        });
        ComboBox<String> availableRemote = new ComboBox<>();
        availableRemote.setDisable(true);
        availableRemote.disabledProperty().addListener(selected -> {
            if (!availableRemote.isDisabled()) {
                List<String> a = getWizard().getSshWrapper()
                        .getRemoteCommandOutput("find ~ -maxdepth 1 -name \"*[A|a]gent*.jar\"");
                availableRemote.setItems(FXCollections.observableArrayList(a));
            }

        });
        availableRemote.setEditable(true);
        finishButton.disableProperty().bind(availableRemote.getSelectionModel().selectedItemProperty().isNull());
        remote.setOnMouseClicked(e -> local.setText(localStr));
        remote.textProperty().bind(SshData.instance.username);
        availableRemote.disableProperty().bind(remote.selectedProperty().not());
        Label l = new Label("Specify path to the agent JAR file:");
        SshData.instance.localFile.bind(localFileLabel.textProperty());
        SshData.instance.remoteFile.bind(availableRemote.editorProperty().get().textProperty());
        finishButton.setOnAction(event -> {
            String filePath = local.isSelected() ? SshData.instance.localFile.get() : SshData.instance.remoteFile.get();
            String cmdStr = "java -jar " + filePath;
            if (local.isSelected()) {
                //cmdStr = "scp " + filePath + " " + sshCredentials + ":~ ; " + cmdStr;
            }
            for(String s : getWizard().getSshWrapper().getRemoteCommandOutput(cmdStr)) {
                System.out.println(s);
            }
        });

        l.setWrapText(true);
        finishButton.setText("Deploy");
        return new VBox(5, l, new HBox(local, localFileLabel), remote, availableRemote);
    }
}