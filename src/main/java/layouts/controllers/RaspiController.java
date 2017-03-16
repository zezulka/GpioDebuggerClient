/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.controllers;

import core.ConnectionManager;
import core.GuiEntryPoint;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import core.Main;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import protocol.ProtocolMessages;

/**
 *
 * @author miloslav
 */
public class RaspiController implements Initializable {

    /**
     * Returns String containing Button title which caused this event to happen.
     * The Button title is equivalent to the one in bulldog naming. (i.e.
     * RaspiNames etc.)
     *
     * @param event
     * @throws IllegalArgumentException in case event is not of Button instance
     */
    @FXML
    protected void getButtonTitle(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button buttonClicked = (Button) event.getSource();
            ConnectionManager.getInstance().setMessage("gpio:write:" + buttonClicked.getText());
        } else {
            GuiEntryPoint.writeErrorToLoggerWithoutCause(ProtocolMessages.C_ERR_GUI_NOT_BUTTON.toString());
            throw new IllegalArgumentException("error in MouseEvent: entity clicked is not of Button instance ");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }
}
