package layouts.controllers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Utility class for storing Images being used in the GUI. These Images
 * are for convenience of the code stored in wrapper ImageView classes.
 * All ImageView constants are package private simply because the only classes
 * which should refer to these system resources are controllers.
 *
 * @author Miloslav Zezulka
 */
public final class ImageViews {

    private static final int IMAGE_EDGE_LEN = 30;

    private ImageViews() {
    }

    static final ImageView STOP_BTN = getImageFromPath("stop-button.jpg");
    static final ImageView PLAY_BTN = getImageFromPath("play-button.jpg");
    static final ImageView ACTIVE = getImageFromPath("active.png");
    static final ImageView DEVICES = getImageFromPath("devices.png");
    static final ImageView CONNECT = getImageFromPath("connect.png");
    static final ImageView DEVICE_TREE = getImageFromPath("tree.png");
    static final ImageView HISTORY = getImageFromPath("history.png");
    static final ImageView REMOVE = getImageFromPath("remove.jpg");
    static final ImageView DISCONNECT = getImageFromPath("disconnect.png");
    static final ImageView DEVICE_TREE_SELECTED =
            getImageFromPath("tree-selected.png");

    private static ImageView getImageFromPath(String path) {
        return new ImageView(
                new Image(path, IMAGE_EDGE_LEN, IMAGE_EDGE_LEN, true, true)
        );
    }

}
