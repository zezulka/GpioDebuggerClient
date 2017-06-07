package layouts.controllers;

import java.net.URL;

import java.util.ResourceBundle;
import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.ComboBox;

import protocol.InterruptType;
import protocol.RaspiClientPin;

/**
 * FXML Controller class
 *
 * @author Miloslav Zezulka
 */
public class AddListenerFormController implements Initializable {

    @FXML
    private ComboBox<InterruptType> interruptTypeComboBox;
    @FXML
    private ComboBox<RaspiClientPin> pinComboBox;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addAllPins();
        addAllIntrTypes();
        interruptTypeComboBox.getSelectionModel().selectFirst();
        pinComboBox.getSelectionModel().selectFirst();
    }    

    private void addAllPins() {
        pinComboBox.setItems(FXCollections.observableArrayList(RaspiClientPin.gpioValues()));
    }

    private void addAllIntrTypes() {
       interruptTypeComboBox.setItems(FXCollections.observableArrayList(InterruptType.values()));
    }
    
}
