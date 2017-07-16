package layouts.controllers;

import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public abstract class AbstractInterfaceFormController implements Initializable {

    protected static final String HEXA_PREFIX = "0x";
    protected static final String HEX_BYTE_REGEX = "^([0-9A-Fa-f])+$";
    protected static final String HEX_BYTE_REGEX_BINDING = "^([0-9A-Fa-f]{2})+$";
    
    protected void addAllModes(ComboBox<Operation> operationList) {
        operationList.setItems(FXCollections.observableArrayList(Operation.values()));
    }

    protected void enforceNumericValuesOnly(TextField textfield) {
        textfield.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!(newValue.matches("\\d*"))) {
                textfield.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
    
    protected BooleanBinding createHexValuesOnlyBinding(TextField textfield) {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> Pattern.compile(HEX_BYTE_REGEX_BINDING).matcher(textfield.getText()).matches(), textfield.textProperty());
        return Bindings.when(binding).then(true).otherwise(false);
    }
    
    protected void enforceHexValuesOnly(TextField textfield) {
        textfield.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.equals("")) {
                textfield.setText("");
                return;
            }
            if (!(newValue.matches(HEX_BYTE_REGEX))) {
                textfield.setText(oldValue);
            }
        });
    }

    protected abstract StringBuilder getMessagePrefix();
}
