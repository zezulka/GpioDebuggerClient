package gui.controllers;

import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.util.ResourceBundle;
import javafx.scene.control.Tab;

public final class DeviceController implements Initializable {

    @FXML
    private Tab raspiTab;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        raspiTab.setClosable(false);
    }
}
