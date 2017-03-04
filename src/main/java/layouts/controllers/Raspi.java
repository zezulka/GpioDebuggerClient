/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.controllers;

import core.Main;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.stage.Stage;

/**
 *
 * @author miloslav
 */
public class Raspi extends Application {

    @FXML
    protected void handleMouseClick() {
        Main.getOutput().println("gpio:write:");
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            URL path = new URL("file://" + System.getProperty("user.dir") + "/src/main/resources/fxml/Raspi.fxml");
            Parent root = FXMLLoader.load(path);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "error initializing GUI" + e.getMessage());
        }
    }
}
