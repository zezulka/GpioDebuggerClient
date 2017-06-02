package layouts.controllers;

import core.ClientConnectionManager;

import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.Node;
import javafx.scene.control.Alert;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javafx.scene.input.MouseEvent;

import javafx.scene.layout.GridPane;

import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author miloslav
 */
public class SpiRequestFormController implements Initializable {

    @FXML
    private Label statusBar;
    @FXML
    private Button spiRequestButton;
    @FXML
    private ComboBox<String> modeList;
    @FXML
    private ComboBox<Integer> chipSelectList;
    @FXML
    private GridPane textFieldGridPane;

    private static int numFields;
    private static final int MAX_NUM_FIELDS = 16;
    private static final char SEPARATOR = ':';
    private static final String HEXA_PREFIX = "0x";
    /**
     * Highest possible index which is reasonable to set in BCM2835's CS
     * register, in the manual referred to as "SPI Master Control and Status"
     * register.
     */
    private static final int MAX_CS_INDEX = 2;

    private static final Map<String, Operation> MODES = FXCollections.observableHashMap();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addAllModes();
        addAllChipSelectIndexes();
        chipSelectList.getSelectionModel().selectFirst();
        modeList.getSelectionModel().selectFirst();
        numFields = 0;
    }

    @FXML
    private void sendSpiRequest(MouseEvent event) {
        Stage stage = (Stage) spiRequestButton.getScene().getWindow();
        String msgToSend = gatherMessageFromForm();
        if (msgToSend != null) {
            ClientConnectionManager
                    .getInstance()
                    .setMessageToSend(msgToSend);
            stage.close();
        }
    }

    @FXML
    private void addNewTextField(MouseEvent event) {
        if (numFields >= MAX_NUM_FIELDS) {
            statusBar.setText(String.format("Maximum number of rows is %d", MAX_NUM_FIELDS));
            return;
        }

        int size = textFieldGridPane.getChildren().size();
        TextField tf = new TextField();
        tf.setMaxHeight(20.0);
        tf.setMaxWidth(100.0);

        textFieldGridPane.add(tf, numFields % 2 == 1 ? 1 : 0, size - (numFields % 2 == 1 ? 1 : 0));
        ++numFields;
    }

    private String gatherMessageFromForm() {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder = resultBuilder
                .append("SPI:")
                .append(MODES.get(modeList.getSelectionModel().getSelectedItem()).toString())
                .append(SEPARATOR)
                .append(HEXA_PREFIX)
                .append(chipSelectList.getSelectionModel().getSelectedItem())
                .append(SEPARATOR);
        if (textFieldGridPane.getChildren().isEmpty()) {
            showErrorDialogMessage("At least one byte must be sent!");
            return null;
        }
        for (Iterator<Node> it = textFieldGridPane.getChildren().iterator(); it.hasNext();) {
            String t = ((TextField) it.next()).getText().trim();
            if (t == null || t.isEmpty()) {
                showErrorDialogMessage("At least one field is empty. "
                        + "Please fill in all the fields.");
                return null;
            }
            if(isStringNumericAndPositive(HEXA_PREFIX + t)) {
                resultBuilder = resultBuilder.append(HEXA_PREFIX).append(t);
            } else {
                showErrorDialogMessage(String.format("At least one field is"
                    + " not a valid input, found '%s'", t));
                return null;
            }
            if (it.hasNext()) {
                resultBuilder = resultBuilder.append(' ');
            }
        }
        System.out.println(resultBuilder.toString());
        return resultBuilder.toString();
    }
    
    private boolean isStringNumericAndPositive(String input) {
        try {
            if (input == null || input.isEmpty()) {
                return false;
            }
            return Short.decode(input) >= 0;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
    
    private static void showErrorDialogMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR MESSAGE");
        alert.setHeaderText("There has been an error processing user input:");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addAllModes() {
        for (Operation op : Operation.values()) {
            MODES.put(op.getOp(), op);
        }
        this.modeList.setItems(FXCollections.observableArrayList(MODES.keySet()));
    }

    private void addAllChipSelectIndexes() {
        List<Integer> ints = new ArrayList<>();
        for (int i = 0; i < MAX_CS_INDEX; i++) {
            ints.add(i);
        }
        this.chipSelectList.setItems(FXCollections.observableArrayList(ints));
    }

}
