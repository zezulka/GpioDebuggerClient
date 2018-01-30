package gui.deployer;

import gui.controllers.ControllerUtils;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import props.AppPreferencesExtractor;
import util.SshWrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

class AuthPage extends AbstractWizardPage {

    private RadioButton none;
    private RadioButton cert;
    private RadioButton passwd;
    private ToggleGroup options;
    private Button previousButton;
    private Button nextButton;
    private PasswordField pf;

    AuthPage() {
        super("Authentication");

        none.setToggleGroup(options);
        cert.setToggleGroup(options);
        passwd.setToggleGroup(options);
        none.setSelected(true);
    }

    @Override
    protected List<Button> getButtons() {
        return Arrays.asList(previousButton, nextButton);
    }

    @Override
    protected void initNodes() {
        pf = new PasswordField();
        previousButton = new Button("_Previous");
        previousButton.setOnAction(e -> getWizard().priorPage());
        nextButton = new Button("N_ext");
        none = new RadioButton("None");
        cert = new RadioButton("Certificate");
        passwd = new RadioButton("Password");
        options = new ToggleGroup();
        nextButton.disableProperty().bind(passwd.selectedProperty().not()
                .or(pf.textProperty().isEmpty()));
        nextButton.setOnAction(e -> {
            try {
                //TODO
                /*if (getWizard().getSshWrapper() != null) {
                } else {

                }*/
                getWizard().setSshWrapper(new SshWrapper(SSH_DATA));
                List<String> str = getWizard().getSshWrapper()
                        .getRemoteCommandOutput(
                                "netstat -tulnp 2> /dev/null | grep "
                                        + AppPreferencesExtractor
                                        .defaultSocketPort()
                                        + " &> /dev/null ; echo $?");
                if (str.size() == 1 && str.get(0).equals("1")) {
                    nextPage();
                    return;
                } else if (str.size() == 1 && str.get(0).equals("0")) {
                    ControllerUtils
                            .showErrorDialog("Agent is already running.");
                } else {
                    // netstat is not installed on the system, let's try another
                    List<String> strTwo = getWizard()
                            .getSshWrapper()
                            .getRemoteCommandOutput(
                                    "ps aux | grep \"java -jar.*jar\" | wc -l");
                }
                ControllerUtils.showErrorDialog("Agent is already running.");
            } catch (IOException ioe) {
                // Swallow exception
            }
        });
    }

    @Override
    protected Parent getContent() {
        Label l = new Label("Select authentication method for the user "
                + SSH_DATA.getUsername());
        l.setWrapText(true);
        SSH_DATA.bindPassword(pf.textProperty());
        return new VBox(5, l, none, cert, passwd, pf);
    }
}
