/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.controllers;

import core.ClientConnectionManager;
import core.GuiEntryPoint;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import protocol.ProtocolMessages;

/**
 *
 * @author miloslav
 */
public class RaspiController implements Initializable, DeviceController {

    @FXML
    private Label statusBar;
    @FXML
    private RadioButton readRadioButton;

    /**
     * String containing Button title which caused this event to happen. The
     * Button title is equivalent to the one in bulldog naming. (i.e. RaspiNames
     * etc.)
     *
     * @param event
     * @throws IllegalArgumentException in case event is not of Button instance
     */
    @FXML
    protected void sendGpioRequest(MouseEvent event) {
        String op = readRadioButton.isSelected() ? "read" : "write";
        sendRequest(event, "gpio:" + op + ":" + getButtonTitle(event));
    }

    @FXML
    protected void sendInterfaceRequest(MouseEvent event) {
        String op = readRadioButton.isSelected() ? "read" : "write";
        sendRequest(event, getButtonTitle(event) + ":" + op);
    }

    private String getButtonTitle(MouseEvent event) {
        if (event == null) {
            return null;
        }
        return ((Button) event.getSource()).getText();
    }

    private void sendRequest(MouseEvent event, String msg) {
        if (event.getSource() instanceof Button) {
            ClientConnectionManager
                    .getInstance()
                    .setMessageToSend(msg);
        } else {
            GuiEntryPoint.writeErrorToLoggerWithoutCause(ProtocolMessages.C_ERR_GUI_NOT_BUTTON.toString());
            throw new IllegalArgumentException("error in MouseEvent: entity clicked is not of Button instance ");
        }
    }

    @Override
    public void setStatus(String msg) {
        if (msg == null) {
            throw new IllegalArgumentException("msg cannot be null");
        }
        //if (statusBar == null) {
        //    throw new IllegalStateException("status has not been initialized yet!");
        //}
        //statusBar.setText(msg);
        System.out.println(msg);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}