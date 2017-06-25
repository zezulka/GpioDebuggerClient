package core;

import core.gui.App;
import java.awt.EventQueue;
import javafx.application.Application;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public class Main {

    /**
     * Entry point for this application.
     * @param args
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> Application.launch(App.class, args));
    }
}
