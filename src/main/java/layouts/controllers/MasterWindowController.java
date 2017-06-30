package layouts.controllers;

import core.gui.App;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MasterWindowController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MasterWindowController.class);
    private static Tab currentTab = null;

    @FXML
    private TabPane devicesTab;

    public MasterWindowController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        devicesTab.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            currentTab = newValue;
        });
    }

    @FXML
    private void connectToDeviceHandler(MouseEvent event) {
        App.createNewIpPromptForm();
    }

    public static Tab getCurrentTab() {
        return MasterWindowController.currentTab;
    }
}
