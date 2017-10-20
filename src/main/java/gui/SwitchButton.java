package gui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;

public final class SwitchButton extends Label {

    private SimpleBooleanProperty switchedOn = new SimpleBooleanProperty(true);
    private static final int BTN_EDGE_SIZE = 30;

    public SwitchButton() {
        Button switchBtn = new Button();
        switchBtn.setPrefWidth(BTN_EDGE_SIZE);
        switchBtn.setPrefHeight(BTN_EDGE_SIZE);
        switchBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                switchedOn.set(!switchedOn.get());
            }
        });

        setGraphic(switchBtn);

        switchedOn.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean t, Boolean t1) {
                if (t1) {
                    setText("Device tree: ON");
                    setStyle("-fx-background-color: green; "
                            + "-fx-text-fill:white;"
                            + "-fx-padding: 0em 0em 0em 1em;"
                    );
                    setContentDisplay(ContentDisplay.RIGHT);
                } else {
                    setText("Device tree: OFF");
                    setStyle("-fx-background-color: grey;"
                            + "-fx-text-fill:black;"
                            + "-fx-padding: 0em 1em 0em 0em;"
                    );
                    setContentDisplay(ContentDisplay.LEFT);
                }
            }
        });
        switchedOn.set(false);
    }

    public SimpleBooleanProperty switchOnProperty() {
        return switchedOn;
    }
}
