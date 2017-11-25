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

    private static final Image STOP_BTN = imageFromPath("stop-button.jpg");
    private static final Image PLAY_BTN = imageFromPath("play-button.jpg");
    private static final Image REMOVE = imageFromPath("remove.jpg");
    private static final Image INFO = getImageFromPath("info.png", 20);
    public static final ImageView ACTIVE = imageView("active.png");
    public static final ImageView CONNECT = imageView("connect.png");
    public static final ImageView HISTORY = imageView("history.png");
    public static final ImageView DISCONNECT = imageView("disconnect.png");

    /**
     *
     * When there are multiple instances of the same picture available on a
     * given scene, fresh ImageView from Image must be created.
     */
    private static ImageView imageToImageView(Image image) {
        return new ImageView(image);
    }

    public static ImageView info() {
        return imageToImageView(INFO);
    }

    public static ImageView stopBtn() {
        return imageToImageView(STOP_BTN);
    }

    public static ImageView playBtn() {
        return imageToImageView(PLAY_BTN);
    }

    public static ImageView removeBtn() {
        return imageToImageView(REMOVE);
    }

    private static Image imageFromPath(String path) {
        return new Image(getPathToImage(path), IMAGE_EDGE_LEN,
                IMAGE_EDGE_LEN, true, true);
    }

    private static Image getImageFromPath(String path, int edge) {
        return new Image(getPathToImage(path), edge,
                edge, true, true);
    }

    private static ImageView imageView(String path) {
        return new ImageView(new Image(getPathToImage(path), IMAGE_EDGE_LEN,
                IMAGE_EDGE_LEN, true, true));
    }

    private static String getPathToImage(String path) {
        return GRAPICS_FOLDER + File.separator + path;
    }

}
