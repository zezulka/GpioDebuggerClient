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
public class SpiReadRequestFormController implements Initializable {

    @FXML
    private GridPane gridPane;
    @FXML
    private TextField chipEnableIndex;
    @FXML
    private Button addNewFieldButton;
    @FXML
    private Label statusBar;
    @FXML
    private Button spiRequestButton;

    private static int numFields = 0;
    private static final int MAX_NUM_FIELDS = 16;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void addNewTextField(MouseEvent event) {
        if (numFields >= MAX_NUM_FIELDS) {
            statusBar.setText(String.format("Maximum number of rows is %d", MAX_NUM_FIELDS));
            return;
        }

        int size = gridPane.getChildren().size();
        TextField tf1 = new TextField();
        tf1.setMaxWidth(50.0);

        gridPane.add(tf1, numFields % 2 == 1 ? 1 : 0, size - (numFields % 2 == 1 ? 1 : 0));
        ++numFields;
    }

    @FXML
    private void sendSpiRequest(MouseEvent event) {
    }

}
