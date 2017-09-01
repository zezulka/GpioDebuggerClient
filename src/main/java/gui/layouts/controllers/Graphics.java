package gui.layouts.controllers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Utility class for storing Images being used in the GUI. These Images are for
 * convenience of the code stored in wrapper ImageView classes if necessary. All
 * constants are package private because the only classes which should refer to
 * these system resources are controllers.
 *
 * @author Miloslav Zezulka
 */
public final class Graphics {

    private static final int IMAGE_EDGE_LEN = 30;

    private Graphics() {
    }

    static final Image STOP_BTN = getImageFromPath("stop-button.jpg");
    static final Image PLAY_BTN = getImageFromPath("play-button.jpg");
    static final Image REMOVE = getImageFromPath("remove.jpg");
    static final ImageView ACTIVE = getImageViewFromPath("active.png");
    static final ImageView DEVICES = getImageViewFromPath("devices.png");
    static final ImageView CONNECT = getImageViewFromPath("connect.png");
    static final ImageView HISTORY = getImageViewFromPath("history.png");
    static final ImageView DISCONNECT = getImageViewFromPath("disconnect.png");

    private static Image getImageFromPath(String path) {
        return new Image(path, IMAGE_EDGE_LEN, IMAGE_EDGE_LEN, true, true);
    }

    private static ImageView getImageViewFromPath(String path) {
        return new ImageView(
                new Image(path, IMAGE_EDGE_LEN, IMAGE_EDGE_LEN, true, true)
        );
    }

}
