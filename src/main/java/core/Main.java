package core;

import gui.App;
import java.awt.EventQueue;
import javafx.application.Application;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public final class Main {

    private Main() {
    }

    /**
     * Entry point for this application.
     *
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> Application.launch(App.class));
    }
}
