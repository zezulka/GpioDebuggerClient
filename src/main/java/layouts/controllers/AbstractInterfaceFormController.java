package layouts.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;

public abstract class AbstractInterfaceFormController implements Initializable {

    protected static final String HEXA_PREFIX = "0x";
    protected static final String HEX_BYTE_REGEX = "^(0?[0-9A-Fa-f]|[1-9A-Fa-f][0-9A-Fa-f])$";
    protected static final int MAX_NUM_FIELDS = 16;
    
    protected void addAllModes(ComboBox<Operation> operationList) {
        operationList.setItems(FXCollections.observableArrayList(Operation.values()));
    }

    protected BooleanBinding createDataTextFields(GridPane gridPane) {
        BooleanBinding bind = Bindings.createBooleanBinding(() -> {
            return true;
        });
        for (int i = 0; i < MAX_NUM_FIELDS; i++) {
            TextField newField = new TextField();
            newField.setDisable(true);
            newField.setPrefSize(50, 35);
            newField.setMaxSize(50, 35);
            bind = Bindings.when(newField.disabledProperty()).then(true).otherwise(Bindings.isNotEmpty(newField.textProperty())).and(bind);
            enforceHexValuesOnly(newField);
            gridPane.add(newField, i % 4, i / 4);
        }
        return bind;
    }

    protected void enforceNumericValuesOnly(TextField textfield) {
        textfield.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!(newValue.matches("\\d*"))) {
                textfield.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
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

    protected List<String> getBytes(GridPane gridPane) {
        List<Node> enabledNodes = gridPane.getChildren().filtered((textfield) -> !textfield.isDisabled());
        List<String> resultDataArray = new ArrayList<>();
        for (Node node : enabledNodes) {
            resultDataArray.add(((TextField) node).getText());
        }
        return resultDataArray;
    }

    protected BooleanBinding assertDataFieldsSizeNonpositive(IntegerProperty numFields) {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> numFields.lessThanOrEqualTo(0).get(), numFields);
        return Bindings.when(binding).then(true).otherwise(false);
    }

    protected BooleanBinding assertDataFieldsSizeAtLeastMaxCap(IntegerProperty numFields) {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> numFields.greaterThanOrEqualTo(MAX_NUM_FIELDS).get(), numFields);
        return Bindings.when(binding).then(true).otherwise(false);
    }
    
    protected void addNewTextField(GridPane gridPane, IntegerProperty numFields) {
        ((TextField) gridPane.getChildren().get(numFields.get())).setBackground(new Background(new BackgroundFill(Paint.valueOf("FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));
        ((TextField) gridPane.getChildren().get(numFields.get())).setStyle("");

        gridPane.getChildren().get(numFields.get()).setDisable(false);
        numFields.set(numFields.get() + 1);
    }

    protected void removeLastTextField(GridPane gridPane, IntegerProperty numFields) {
        gridPane.getChildren().get(numFields.get() - 1).setDisable(true);
        ((TextField)gridPane.getChildren().get(numFields.get() - 1)).setText("");
        numFields.set(numFields.get() - 1);
    }
    
    protected abstract StringBuilder getMessagePrefix();
}
