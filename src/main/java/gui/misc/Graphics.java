package gui.misc;

import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Utility class for storing Images being used in the GUI. These Images are for
 * convenience of the code stored in wrapper ImageView classes if necessary. All
 * constants are package private because the only classes which should refer to
 * these system resources are controllers.
 */
public final class Graphics {

    private static final int IMAGE_EDGE_LEN = 25;

    private Graphics() {
    }

    private static final String GRAPICS_FOLDER = "graphics";

    public static final Image STOP_BTN = imageFromPath("stop-button.jpg");
    public static final Image PLAY_BTN = imageFromPath("play-button.jpg");
    public static final Image REMOVE = imageFromPath("remove.jpg");
    public static final Image INFO = getImageFromPath("info.png", 20);
    public static final ImageView ACTIVE = imageViewFromPath("active.png");
    public static final ImageView CONNECT = imageViewFromPath("connect.png");
    public static final ImageView HISTORY = imageViewFromPath("history.png");
    public static final ImageView DISCONNECT
            = imageViewFromPath("disconnect.png");

    private static Image imageFromPath(String path) {
        return new Image(getPathToImage(path), IMAGE_EDGE_LEN,
                IMAGE_EDGE_LEN, true, true);
    }

    private static Image getImageFromPath(String path, int edge) {
        return new Image(getPathToImage(path), edge,
                edge, true, true);
    }

    private static ImageView imageViewFromPath(String path) {
        return new ImageView(new Image(getPathToImage(path), IMAGE_EDGE_LEN,
                IMAGE_EDGE_LEN, true, true));
    }

    private static String getPathToImage(String path) {
        return GRAPICS_FOLDER + File.separator + path;
    }

}
