package gui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;

public final class SwitchButton extends Label {

    private SimpleBooleanProperty switchedOn = new SimpleBooleanProperty(true);
    private static final int BTN_HEIGHT = 30;
    private static final int BTN_WIDTH = 20;

    public SwitchButton() {
        Button switchBtn = new Button();
        switchBtn.setPrefWidth(BTN_WIDTH);
        switchBtn.setPrefHeight(BTN_HEIGHT);
        switchBtn.setOnAction(t -> switchedOn.set(!switchedOn.get()));
        setGraphic(switchBtn);

        switchedOn.addListener((ignore, ignoreAnother, newState) -> {
            if (newState) {

                setStyle("-fx-background-color: green; "
                        + "-fx-padding: 0em 0em 0em 4em;"
                );
                setContentDisplay(ContentDisplay.LEFT);
            } else {
                setStyle("-fx-background-color: grey;"
                        + "-fx-padding: 0em 4em 0em 0em;"
                );
                setContentDisplay(ContentDisplay.RIGHT);
            }
        });
        switchedOn.set(false);
    }

    public SimpleBooleanProperty switchOnProperty() {
        return switchedOn;
    }
}
