package core;

import java.awt.EventQueue;
import javafx.application.Application;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class Main {

    private static final ClientConnectionManager CM = ClientConnectionManager.getInstance();
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> Application.launch(GuiEntryPoint.class, args));
    }
    
    
    public static void connectToDevice(String ipAddress) {
        CM.setIpAddress(ipAddress);
        new Thread(CM).start();
    }
}
