package gui.controllers;

import gui.misc.Operation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import util.StringConstants;

public abstract class AbstractTabController implements Initializable {

    protected static final String HEXA_PREFIX = "0x";
    protected static final String HEX_BYTE_REGEX = "^([0-9A-Fa-f])+$";
    protected static final String HEX_BYTE_REGEX_BINDING
            = "^([0-9A-Fa-f]{2})+$";

    protected final void addAllModes(ComboBox<Operation> operationList) {
        operationList.setItems(Operation.observableValues());
    }

    protected final void enforceNumericValuesOnly(TextField textfield) {
        textfield.textProperty()
                .addListener((ObservableValue<? extends String> obs, String old,
                        String newValue) -> {
                    if (!(newValue.matches("\\d*"))) {
                        textfield.setText(newValue.replaceAll("[^\\d]", ""));
                    }
                });
    }

    protected final List<String> getBytesFromUser(String str) {
        List<String> result = new ArrayList<>();
        final int hexa = 16;
        for (int i = 0; i < str.length() - 1; i += 2) {
            String subStr = str.substring(i, i + 2);
            result.add(subStr + "\n(" + Short.parseShort(subStr, hexa) + ')');
        }
        return result;
    }

    protected final BooleanBinding hexValuesOnly(TextField textfield) {
        BooleanBinding binding = Bindings.createBooleanBinding(()
                -> Pattern.compile(HEX_BYTE_REGEX_BINDING)
                        .matcher(textfield.getText()).matches(),
                textfield.textProperty());
        return Bindings.when(binding).then(true).otherwise(false);
    }

    protected final void enforceHexValuesOnly(TextField textfield) {
        textfield.textProperty()
                .addListener((ObservableValue<? extends String> obs, String old,
                        String newValue) -> {
                    if (newValue.equals("")) {
                        textfield.setText("");
                        return;
                    }
                    if (!(newValue.matches(HEX_BYTE_REGEX))) {
                        textfield.setText(old);
                    }
                });
    }

    protected abstract StringBuilder getMessagePrefix();

    protected static final class BytesViewStringConverter
            extends StringConverter<List<String>> {
        @Override
        public String toString(List<String> t) {
            if (t.size() == 1 && t.get(0)
                    .equals(StringConstants.WRITE_OK)) {
                return StringConstants.WRITE_OK;
            }
            StringBuilder b = new StringBuilder();
            for (String s : t) {
                b = b.append(s).append(' ');
            }
            return b.toString();
        }

        @Override
        public List<String> fromString(String string) {
            if (string.equals(StringConstants.WRITE_OK)) {
                return Arrays.asList("WRITE REQUEST");
            }
            return new ArrayList<>(Arrays.asList(string.split(" ")));
        }
    }
}
