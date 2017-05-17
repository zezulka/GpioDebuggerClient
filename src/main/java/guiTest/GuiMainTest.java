/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guiTest;

import guiTest.JavaFXDummyApplication;
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
