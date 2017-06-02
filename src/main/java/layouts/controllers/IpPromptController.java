/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.controllers;

import core.ClientConnectionManager;
import core.GuiEntryPoint;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * FXML Controller class
 *
 * @author Miloslav
 */
public class IpPromptController implements Initializable {

    @FXML
    private TextField ipValue;
    @FXML
    private TextArea feedbackArea;

    /**
     * initialises the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    private void handler() {
        String ip = ipValue.getText().trim();
        if (ip == null || "".equals(ip) || !InetAddressValidator.getInstance().isValid(ip)) {
            Platform.runLater(() -> {
                GuiEntryPoint.provideFeedback("IP is not valid!");
            });
            return;
        }
        ClientConnectionManager.getInstance().connectToDevice(ip);
    }

    @FXML
    private void submitButtonPressed(KeyEvent event) {
        handler();
    }

    @FXML
    private void submitButtonPressed(MouseEvent event) {
        handler();
    }

}
