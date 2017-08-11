package layouts.controllers;

import core.util.StringConstants;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public final class ControllerUtils {

    private ControllerUtils() {
    }

    private static void showDialog(Alert.AlertType alertType,
            String message) {

        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setHeaderText(null);
            alert.setResizable(true);
            alert.setContentText(message);
            alert.getDialogPane()
                    .getChildren()
                    .stream()
                    .filter(node -> node instanceof Label)
                    .forEach(node -> ((Label) node)
                    .setMinHeight(Region.USE_PREF_SIZE));
            alert.showAndWait();
        });
    }

    public static void showInfoDialog(String message) {
        showDialog(Alert.AlertType.INFORMATION, message);
    }

    public static void showErrorDialog(String message) {
        showDialog(Alert.AlertType.ERROR, message);
    }

    /**
     * @return True if user confirmed the given action, false otherwise.
     */
    public static boolean showConfirmDialog(StringConstants message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please confirm");
        alert.setHeaderText(null);
        alert.setResizable(true);
        alert.setContentText(message.toString());
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() != null && result.get().equals(ButtonType.OK);
    }
}
