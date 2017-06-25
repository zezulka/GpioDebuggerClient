package guiTest;

import java.awt.EventQueue;
import javafx.application.Application;

/**
 *
 * @author Miloslav Zezulka
 */
public class GuiMainTest {

    /**
     * Helper main to test user interface. This class together with
     * JavaFXDummyApplication is just temporary and is not going to be released
     * in any build.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> Application.launch(JavaFXDummyApplication.class, args));
    }

}
