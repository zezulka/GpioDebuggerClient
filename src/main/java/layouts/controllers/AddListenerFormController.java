package layouts.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import java.util.ResourceBundle;
import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import protocol.ClientPin;

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
    private ComboBox<ClientPin> pinComboBox;
    @FXML
    private Button addListenerButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addAllPins();
        addAllIntrTypes();
        interruptTypeComboBox.getSelectionModel().selectFirst();
        pinComboBox.getSelectionModel().selectFirst();
    }    

    private void addAllPins() {
        List<ClientPin> result = new ArrayList<>();
        for(ClientPin pin : RaspiClientPin.pins()) {
            if(pin.isGpio()) {
                result.add(pin);
            }
        }
        pinComboBox.setItems(FXCollections.observableArrayList(result));
    }
    private void addAllIntrTypes() {
       interruptTypeComboBox.setItems(FXCollections.observableArrayList(InterruptType.values()));
    }
    

    @FXML
    private void addNewInterrupt(MouseEvent event) {
        if(InterruptTableController.addNewInterruptListener(
                new InterruptValueObject(pinComboBox.getValue(), 
                                         interruptTypeComboBox.getValue()))) {
            ((Stage)addListenerButton.getScene().getWindow()).close();
        }
    }
    
}
