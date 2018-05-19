package gui.controllers;

import gui.tab.loader.TabLoader;
import gui.tab.loader.TabLoaderImpl;

import java.net.URL;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public final class Utils {

    private static final String FXML_EXT = ".fxml";
    private static final URL DEVICE = getPathToFxml("Device");
    public static final URL SPI = getPathToFxml("SpiTab");
    public static final URL I2C = getPathToFxml("I2cTab");
    public static final URL INTRS = getPathToFxml("InterruptsTab");
    public static final URL RASPI_GPIO = getPathToFxml("RaspiGpioTab");
    public static final URL TESTING_GPIO = getPathToFxml("TestingGpioTab");
    public static final URL MASTER = getPathToFxml("MasterWindow");
    public static final URL DEPLOYMENT_FORM = getPathToFxml("DeploymentForm");

    private static final TabLoader GPIO_TAB_LOADER = new TabLoaderImpl();

    private Utils() {
    }

    // Should be extended to more devices when more devices are implemented
    public static TabLoader getLoader() {
        return GPIO_TAB_LOADER;
    }

    private static void showDialog(Alert.AlertType alertType,
                                   String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setHeaderText(null);
            alert.setResizable(false);
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

    public static URL getBoardUrl() {
        return DEVICE;
    }

    public static Object getDeviceController() {
        return new Device();
    }

    private static URL getPathToFxml(String fxmlName) {
        return Utils.class.getResource("/fxml"
                + "/" + fxmlName + FXML_EXT);
    }

    public static void playButtonAnimation(Button btn) {
        final double startVal = 1.0F;
        final double endVal = 0.6F;
        final int dur = 200;
        final double shift = 10;
        final Duration transDuration = Duration.millis(dur);
        FadeTransition fadeTransition = new FadeTransition(transDuration, btn);
        fadeTransition.setFromValue(startVal);
        fadeTransition.setToValue(endVal);
        fadeTransition.setCycleCount(2);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();
        TranslateTransition tt
                = new TranslateTransition(Duration.millis(dur), btn);
        tt.setFromX(0.0F);
        tt.setByX(shift);
        tt.setCycleCount(2);
        tt.setAutoReverse(true);
        tt.playFromStart();
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
    public static boolean showConfirmDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please confirm");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setResizable(false);
        alert.getDialogPane()
                .getChildren()
                .stream()
                .filter(node -> node instanceof Label)
                .forEach(node -> ((Label) node)
                        .setMinHeight(Region.USE_PREF_SIZE));
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().equals(ButtonType.OK);
    }
}
