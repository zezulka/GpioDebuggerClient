package gui.deployer;

import gui.controllers.ControllerUtils;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import props.AppPreferencesExtractor;
import util.SshWrapper;

import java.io.IOException;
import java.util.List;

class AuthPage extends AbstractWizardPage {

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
        Label l = new Label("Select authentication method for the user " + sshData.getUsername());
        PasswordField pf = new PasswordField();
        l.setWrapText(true);
        sshData.bindPassword(pf.textProperty());
        nextButton.disableProperty().bind(passwd.selectedProperty().not().or(pf.textProperty().isEmpty()));
        nextButton.setOnAction(e -> {
            try {
                if (getWizard().getSshWrapper() != null) {
                    //check whether the username and ip are different... we want to be careful with resources
                } else {

                }
                getWizard().setSshWrapper(new SshWrapper(sshData));
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
