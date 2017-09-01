package gui.layouts.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

/**
 * FXML Controller class
 *
 * @author miloslav
 */
public class GpioTabController implements Initializable {

    @FXML
    private Tab gpioTab;
    @FXML
    private GridPane gpioGridPane;
    @FXML
    private RadioButton writeRadioButton;
    @FXML
    private ToggleGroup op;
    @FXML
    private RadioButton readRadioButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
