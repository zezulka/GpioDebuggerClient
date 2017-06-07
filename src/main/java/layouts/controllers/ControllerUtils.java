package layouts.controllers;

import javafx.scene.control.Alert;

public class ControllerUtils {
    
    static void showErrorDialogMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR MESSAGE");
        alert.setHeaderText("There has been an error processing the user input:");
        alert.setResizable(true);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
