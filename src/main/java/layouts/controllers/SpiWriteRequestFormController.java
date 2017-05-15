/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

/**
 * FXML Controller class
 *
 * @author miloslav
 */
public class SpiWriteRequestFormController implements Initializable {

    @FXML
    private Label statusBar;
    @FXML
    private Button spiRequestButton;
    @FXML
    private TextField chipEnableIndex;
    @FXML
    private Button addNewFieldButton;
    @FXML
    private GridPane gridPane;
    
    private static int numFields = 0;
    private static final int MAX_NUM_FIELDS = 8;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    

    @FXML
    private void sendSpiRequest(MouseEvent event) {
    }

    @FXML
    private void addNewTextField(MouseEvent event) {
        if(numFields >= MAX_NUM_FIELDS) {
            statusBar.setText(String.format("Maximum number of rows is %d", MAX_NUM_FIELDS));
            return;
        }
        ++numFields;
        int size = gridPane.getChildren().size();
        TextField tf1 = new TextField();
        tf1.setMaxWidth(50.0);
        
        TextField tf2 = new TextField();
        tf2.setMaxWidth(50.0);
        
        gridPane.add(tf1, 0, size);
        gridPane.add(tf2, 1, size);
    }
    
}
