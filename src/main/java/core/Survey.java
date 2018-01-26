package core;

import gui.userdata.xstream.XStreamUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.NetworkingUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

/**
 * This class displays a survey using a wizard
 */
public class Survey extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // configure and display the scene and stage.
        stage.setScene(new Scene(new SurveyWizard(stage), 400, 250));
        stage.show();
    }
}

/**
 * basic wizard infrastructure class
 */
class Wizard extends StackPane {
    private static final int UNDEFINED = -1;
    private ObservableList<WizardPage> pages = FXCollections.observableArrayList();
    private Stack<Integer> history = new Stack<>();
    private int curPageIdx = UNDEFINED;

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
    }

    public void cancel() {
    }
}

/**
 * basic wizard page class
 */
abstract class WizardPage extends VBox {
    Button priorButton = new Button("_Previous");
    Button nextButton = new Button("N_ext");
    Button cancelButton = new Button("Cancel");
    Button finishButton = new Button("_Finish");

    WizardPage(String title) {
        priorButton.setOnAction(event -> priorPage());
        nextButton.setOnAction(event -> nextPage());
        cancelButton.setOnAction(event -> getWizard().cancel());
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
        cancelButton.setCancelButton(true);
        finishButton.setDefaultButton(true);
        buttonBar.getChildren().addAll(spring, priorButton, nextButton, cancelButton, finishButton);
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
class SurveyWizard extends Wizard {
    Stage owner;

    public SurveyWizard(Stage owner) {
        super(new IpPage(), new UsernamePage(), new AgentJarPage(), new AuthPage());
        this.owner = owner;
    }

    public void finish() {
        owner.close();
    }

    public void cancel() {
        System.out.println("Cancelled");
        owner.close();
    }
}

/**
 * Simple placeholder class for the customer entered survey response.
 */
class SurveyData {
    static SurveyData instance = new SurveyData();
    StringProperty username = new SimpleStringProperty();
    StringProperty ipAddress = new SimpleStringProperty();
    BooleanProperty selectedLocalFile = new SimpleBooleanProperty(false);
}

class IpPage extends WizardPage {

    private ProgressIndicator pi;

    public IpPage() {
        super("IP address");
        finishButton.setDisable(true);
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
        SurveyData.instance.ipAddress.bind(addrComboBox.valueProperty());
        nextButton.disableProperty().bind(addrComboBox.getSelectionModel().selectedItemProperty().isNull());
        nextButton.setOnAction(e -> {
            try {
                new Thread(new ConnectionWorker(this, InetAddress.getByName(SurveyData.instance.ipAddress.get()))).start();
            } catch (IOException ie) {
                // ok
            }
        });
        Label l = new Label("Enter the IP address or hostname of the device:");
        l.setWrapText(true);
        return new VBox(5, l, new HBox(addrComboBox, pi));
    }
}

class ConnectionWorker extends Task<Boolean> {

    private final InetAddress ia;
    private IpPage page;

    ConnectionWorker(IpPage page, InetAddress ia) {
        Objects.requireNonNull(page, "page null");
        this.page = page;
        this.ia = ia;
    }

    private void notifyConnectingFailed() {
        /*Platform.runLater(() -> ControllerUtils.showErrorDialog(
                String.format(StringConstants.F_HOST_NOT_REACHABLE,
                        device.getHostName())
        ));*/
        System.out.println("Could not connect...");
    }

    @Override
    protected Boolean call() {
        page.getPi().setVisible(true);
        if (!NetworkingUtils.isReachable(ia)) {
            notifyConnectingFailed();
            return false;
        }
        System.out.println("Host is reachable!");
        //LOGGER.debug(String.format("Host %s is reachable",
        //        device.getHostName()));
        return true;

    }

    @Override
    protected void done() {
        page.getPi().setVisible(false);
        try {
            if (get()) {
                Platform.runLater(() -> page.nextPage());
            }
        } catch (InterruptedException | ExecutionException ex) {
            //LOGGER.error(null, ex);
        }
    }
}

/**
 * This page gathers more information about the complaint
 */
class UsernamePage extends WizardPage {
    public UsernamePage() {
        super("Username");
    }

    Parent getContent() {
        TextField textField = new TextField();
        nextButton.setDisable(true);
        textField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            nextButton.setDisable(newValue.isEmpty());
        });
        SurveyData.instance.username.bind(textField.textProperty());
        Label l = new Label("Enter the username you " +
                "want to authenticate with:");
        l.setWrapText(true);
        return new VBox(5, l, textField);
    }
}

class AgentJarPage extends WizardPage {
    private RadioButton local;
    private RadioButton remote;
    private ToggleGroup options;


    public AgentJarPage() {
        super("Complaints");

        nextButton.setDisable(true);
        finishButton.setDisable(true);
        local.setToggleGroup(options);
        remote.setToggleGroup(options);
        options.selectedToggleProperty().addListener((observableValue, oldToggle, newToggle) -> {
            nextButton.setDisable(false);
            finishButton.setDisable(false);
        });
    }

    Parent getContent() {
        local = new RadioButton("Local...");
        remote = new RadioButton();
        options = new ToggleGroup();
        FileChooser fs = new FileChooser();
        TextField tf = new TextField();
        fs.setTitle("Choose agent JAR");
        fs.getExtensionFilters().add(new FileChooser.ExtensionFilter("JAR archive (*.jar)", "*.jar"));
        local.setOnMouseClicked(mouseEvent -> {
            File f = fs.showOpenDialog(new Stage());
            if (f != null) {
                local.setText("Local -> " + f.getAbsolutePath());
            }
        });
        remote.textProperty().bind(SurveyData.instance.username);
        tf.disableProperty().bind(remote.selectedProperty().not());
        SurveyData.instance.selectedLocalFile.bind(local.selectedProperty());
        Label l = new Label("Specify path to the agent JAR file:");
        l.setWrapText(true);
        return new VBox(5, l, local, remote, tf);
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
        nextButton.setDisable(true);
        finishButton.setDisable(true);
    }

    @Override
    Parent getContent() {
        none = new RadioButton("None");
        cert = new RadioButton("Certificate");
        passwd = new RadioButton("Password");
        options = new ToggleGroup();
        Label l = new Label("Select authentication method:");
        PasswordField pf = new PasswordField();
        l.setWrapText(true);
        return new VBox(5, l, none, cert, passwd, pf);
    }
}
