package gui.deployer;

import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

class AgentJarPage extends AbstractWizardPage {
    private RadioButton local;
    private RadioButton remote;
    private ToggleGroup options;

    AgentJarPage() {
        super("Select agent JAR");
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
        remote.setText("Remotely (either from available or custom)");
        availableRemote.disableProperty().bind(remote.selectedProperty().not());
        Label l = new Label("Specify path to the agent JAR file:");
        sshData.bindLocalFile(localFileLabel.textProperty());
        sshData.bindRemoteFile(availableRemote.editorProperty().get().textProperty());
        finishButton.setOnAction(event -> {
            String filePath = local.isSelected() ? sshData.getLocalFile() : sshData.getRemoteFile();
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
