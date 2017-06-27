package layouts.controllers;

import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class ControllerUtils {

    private static void showDialogMessage(AlertType alertType, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setHeaderText(null);
            alert.setResizable(true);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    public static void showInformationDialogMessage(String message) {
        showDialogMessage(AlertType.INFORMATION, message);
    }
    
    public static void showErrorDialogMessage(String message) {
        showDialogMessage(AlertType.ERROR, message);
    }

    /**
     * @return True if user confirmed that they confirm the given action, false
     * otherwise. This method must be wrapped in Platform.runLater (because of 
     * the return value).
     */
    public static boolean showConfirmationDialogMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please confirm");
        alert.setHeaderText(null);
        alert.setResizable(true);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() != null && result.get().equals(ButtonType.OK);
    }
}
