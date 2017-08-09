package layouts.controllers;

import javafx.scene.image.Image;

/**
 * Utility class for storing Images being used in the GUI. All Image
 * constants are package private simply because the only classes which should
 * refer to these system resources are controllers.
 *
 * @author Miloslav Zezulka
 */
public final class Images {

    private static final int IMAGE_EDGE_LEN = 30;

    private Images() {
    }

    static final Image STOP_BTN = getImageFromPath("stop-button.jpg");
    static final Image PLAY_BTN = getImageFromPath("play-button.jpg");
    static final Image ACTIVE = getImageFromPath("active.png");
    static final Image DEVICES = getImageFromPath("devices.png");
    static final Image CONNECT = getImageFromPath("connect.png");
    static final Image DEVICE_TREE = getImageFromPath("tree.png");
    static final Image ADD_NEW = getImageFromPath("add_new.png");
    static final Image HISTORY = getImageFromPath("history.png");
    static final Image DISCONNECT = getImageFromPath("disconnect.png");

    private static Image getImageFromPath(String path) {
        return new Image(path, IMAGE_EDGE_LEN, IMAGE_EDGE_LEN, true, true);
    }

}
