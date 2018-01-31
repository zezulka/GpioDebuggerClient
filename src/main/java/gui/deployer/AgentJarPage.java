package gui.deployer;

import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.List;

class AgentJarPage extends AbstractWizardPage {

    private RadioButton local;
    private RadioButton remote;
    private ToggleGroup options;
    private Button nextButton;
    private Button finishButton;
    private FileChooser fc;
    private Label locFile;

    AgentJarPage() {
        super("Select agent JAR");

    }

    @Override
    protected void initNodes() {
        locFile = new Label();
        fc = new FileChooser();
        fc.setTitle("Choose agent JAR");
        fc.getExtensionFilters().add(new FileChooser
                .ExtensionFilter("JAR archive (*.jar)", "*.jar"));
        nextButton = new Button("N_ext");
        nextButton.setVisible(false);
        finishButton = new Button("Deploy");
        finishButton.setOnAction(event -> {
            String filePath = local.isSelected() ? SSH_DATA.getLocalFile()
                    : SSH_DATA.getRemoteFile();
            String cmdStr = "java -jar " + filePath;
            //TODO SCP
            /*if (local.isSelected()) {

            }*/
            for (String s : getWizard().getSshWrapper()
                    .getRemoteCommandOutput(cmdStr)) {
                System.out.println(s);
            }
        });
        remote = new RadioButton();
        remote.setOnMouseClicked(e -> local.setText("Local..."));
        remote.setText("Remote");
        remote.setToggleGroup(options);
        options = new ToggleGroup();
        options.selectedToggleProperty().addListener((val, old, newv) ->
                nextButton.setDisable(false));
        local = new RadioButton("Local...");
        local.setOnMouseClicked(mouseEvent -> {
            File f = fc.showOpenDialog(new Stage());
            if (f != null) {
                locFile.setText(f.getAbsolutePath());
            }
        });
        local.setToggleGroup(options);
    }

    @Override
    protected List<Button> getButtons() {
        return Arrays.asList(nextButton, finishButton);
    }

    protected Parent getContent() {
        ComboBox<String> remotePaths = new ComboBox<>();
        remotePaths.setDisable(true);
        remotePaths.disabledProperty().addListener(selected -> {
            if (!remotePaths.isDisabled()) {
                List<String> a = getWizard().getSshWrapper()
                        .getRemoteCommandOutput(
                                "find ~ -maxdepth 1 -name \"*[A|a]gent*.jar\"");
                remotePaths.setItems(FXCollections.observableArrayList(a));
            }
        });
        remotePaths.setEditable(true);
        finishButton.disableProperty().bind(remotePaths.getSelectionModel()
                .selectedItemProperty().isNull());
        remotePaths.disableProperty().bind(remote.selectedProperty().not());
        Label l = new Label("Specify path to the agent JAR file:");
        SSH_DATA.bindLocalFile(locFile.textProperty());
        SSH_DATA.bindRemoteFile(remotePaths.editorProperty()
                .get().textProperty());
        l.setWrapText(true);

        return new VBox(5, l, new HBox(local, locFile), remote, remotePaths);
    }
}
