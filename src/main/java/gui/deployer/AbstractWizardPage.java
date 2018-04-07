package gui.deployer;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * basic wizard page class
 */
abstract class AbstractWizardPage extends VBox {

    //protected static final SshData SSH_DATA = new SshData();
    private final Button resetButton = new Button("⏮");

    AbstractWizardPage(String title) {
        resetButton.setOnAction(e -> getWizard().reset());
        initNodes();
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 5 0;");
        setId(title);
        setSpacing(5);
        setStyle("-fx-padding:10; -fx-background-color: honeydew; "
                + "-fx-border-color: derive(honeydew, -30%); "
                + "-fx-border-width: 3;");

        Region spring = new Region();
        VBox.setVgrow(spring, Priority.ALWAYS);
        getChildren().addAll(getContent(), spring, getButtonBar());
    }

    private HBox getButtonBar() {
        Region spring = new Region();
        HBox.setHgrow(spring, Priority.ALWAYS);
        HBox buttonBar = new HBox(5);
        buttonBar.getChildren().add(spring);
        buttonBar.getChildren().add(resetButton);
        buttonBar.getChildren().addAll(getButtons());
        return buttonBar;
    }

    /*
     * Gives opportunity for child nodes to initialize their
     * components. This is NO-OP by default.
     */
    protected void initNodes() {
    }
    protected abstract Parent getContent();
    protected abstract List<Button> getButtons();


    void nextPage() {
        getWizard().nextPage();
    }

    protected void priorPage() {
        getWizard().priorPage();
    }

    protected void navTo(String id) {
        getWizard().navTo(id);
    }

    Wizard getWizard() {
        return (Wizard) getParent();
    }
}
