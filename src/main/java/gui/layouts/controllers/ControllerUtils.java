package gui.layouts.controllers;

import gui.AgentUserPrivileges;
import gui.RaspiTabLoader;
import gui.TabLoader;
import core.util.StringConstants;
import java.io.File;
import java.net.InetAddress;
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
import protocol.BoardType;

public final class ControllerUtils {

    private static final String FXML_EXT = ".fxml";
    private static final URL RASPI = getPathToFxml("Raspi");
    public static final URL SPI = getPathToFxml("SpiTab");
    public static final URL I2C = getPathToFxml("I2cTab");
    public static final URL INTRS = getPathToFxml("InterruptsTab");
    public static final URL GPIO = getPathToFxml("GpioTab");
    public static final URL MASTER = getPathToFxml("MasterWindow");
    private static URL beagleBoneBlack;
    private static URL cubieboard;

    public static final TabLoader GPIO_TAB_LOADER = new RaspiTabLoader();

    private ControllerUtils() {
    }

    // Should be extended to more devices when more devices are implemented
    public static TabLoader getLoader(AgentUserPrivileges privileges) {
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

    public static URL getUrlFromBoardType(BoardType type) {
        switch (type) {
            case RASPBERRY_PI:
                return RASPI;
            case BEAGLEBONEBLACK:
                return beagleBoneBlack;
            case CUBIEBOARD:
                return cubieboard;
            default:
                throw new IllegalArgumentException("unsupported board type");
        }
    }

    public static Object getControllerFromBoardType(BoardType type,
            InetAddress address) {
        switch (type) {
            case RASPBERRY_PI:
                return new RaspiController(address);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static URL getPathToFxml(String fxmlName) {
        return ClassLoader.getSystemClassLoader().getResource("fxml"
                + File.separator + fxmlName + FXML_EXT);
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
    public static boolean showConfirmDialog(StringConstants message,
            Object... other) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please confirm");
        StringBuilder wholeMessage = new StringBuilder(message.toString());
        for (Object obj : other) {
            wholeMessage.append(obj.toString());
        }
        alert.setHeaderText(null);
        alert.setContentText(wholeMessage.toString());
        alert.setResizable(false);
        alert.getDialogPane()
                .getChildren()
                .stream()
                .filter(node -> node instanceof Label)
                .forEach(node -> ((Label) node)
                .setMinHeight(Region.USE_PREF_SIZE));
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() != null && result.get().equals(ButtonType.OK);
    }
}
