/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guiTest;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Miloslav Zezulka
 */
public class JavaFXDummyApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        String pathToFxml = "fxml/I2cRequestForm.fxml";
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource(pathToFxml));
    
        Scene scene = new Scene(root, 600, 1000);
    
        stage.setTitle(pathToFxml);
        stage.setScene(scene);
        stage.show();
    }

}
