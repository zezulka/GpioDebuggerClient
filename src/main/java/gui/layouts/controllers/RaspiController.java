package gui.layouts.controllers;

import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.util.ResourceBundle;
import javafx.scene.control.Tab;

/**
 *
 * @author Miloslav Zezulka
 */
public final class RaspiController implements Initializable {

    @FXML
    private Tab raspiTab;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        raspiTab.setClosable(false);
    }
}
