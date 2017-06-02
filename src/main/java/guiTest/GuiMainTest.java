package guiTest;

import java.awt.EventQueue;
import javafx.application.Application;

/**
 *
 * @author miloslav
 */
public class GuiMainTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> Application.launch(JavaFXDummyApplication.class, args));
    }
    
}
