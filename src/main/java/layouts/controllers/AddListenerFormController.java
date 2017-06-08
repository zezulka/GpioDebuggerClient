package layouts.controllers;

import java.net.URL;

import java.util.ResourceBundle;
import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import protocol.InterruptType;
import protocol.InterruptValueObject;
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
    @FXML
    private Button addListenerButton;

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

    @FXML
    private void addNewInterrupt(MouseEvent event) {
        RaspiController.addNewInteruptListener(
                new InterruptValueObject(pinComboBox.getValue(), 
                                         interruptTypeComboBox.getValue()));
        ((Stage)addListenerButton.getScene().getWindow()).close();
    }
    
}
