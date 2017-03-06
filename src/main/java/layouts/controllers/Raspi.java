/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.controllers;

import core.Main;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.net.URL;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import protocol.ProtocolMessages;

/**
 *
 * @author miloslav
 */
public class Raspi extends Application {

    @FXML
    public void exitApplication(ActionEvent event) {
        Platform.exit();
    }

    @Override
    public void stop() {
        Main.closeConnection();
    }

    @FXML
    protected void handleMouseClick(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button buttonClicked = (Button) event.getSource();
            if (Main.getOutput() == null) {
                Logger.getAnonymousLogger().log(Level.WARNING, ProtocolMessages.C_ERR_NOT_CONNECTED.toString());
                return;
            }
            Main.getOutput().println("gpio:write:" + buttonClicked.getText());
        } else {
            Logger.getAnonymousLogger().log(Level.WARNING, ProtocolMessages.C_ERR_NOT_BUTTON.toString());
        }

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
            Logger.getAnonymousLogger().log(Level.SEVERE, "error initializing GUI", e.getMessage());
        }
    }
}
