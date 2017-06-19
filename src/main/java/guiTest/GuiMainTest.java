package guiTest;

import java.awt.EventQueue;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import layouts.controllers.GuiEntryPoint;

/**
 *
 * @author Miloslav Zezulka
 */
public class GuiMainTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            GuiEntryPoint.initControllerPaths();
        } catch (MalformedURLException ex) {
            Logger.getLogger(GuiMainTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        EventQueue.invokeLater(() -> Application.launch(JavaFXDummyApplication.class, args));
    }
    
}
