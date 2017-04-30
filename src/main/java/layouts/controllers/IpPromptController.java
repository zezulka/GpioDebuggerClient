/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.controllers;

import core.GuiEntryPoint;
import core.Main;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
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

    /**
     * initialises the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    public void clickHandler(MouseEvent event) {
        String ip = ipValue.getText();
        if (ip == null || "".equals(ip) || !InetAddressValidator.getInstance().isValid(ip)) {
            Platform.runLater(() -> {
                GuiEntryPoint.provideFeedback("IP is not valid!");
            });
            return;
        }
        Main.connectToDevice(ip);
    }

}
