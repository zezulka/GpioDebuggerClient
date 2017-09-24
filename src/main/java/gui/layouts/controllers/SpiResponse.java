package gui.layouts.controllers;

import java.time.LocalTime;
import javafx.scene.control.ListView;

public final class SpiResponse {

    private final LocalTime time;
    private final ListView<String> bytes;
    private View view;

    public SpiResponse(LocalTime time, ListView<String> bytes) {
        this.time = time;
        this.bytes = bytes;
        this.view = View.Hexadecimal;
    }

    public enum View {
        Binary(2),
        Decimal(10),
        Hexadecimal(16);

        private final int radix;

        View(int radix) {
            this.radix = radix;
        }

        public int getRadix() {
            return this.radix;
        }
    }

    public void changeView(View radix) {
        this.view = radix;
        changeToRadix(radix);
    }

    private void changeToRadix(View radix) {
        for (int i = 0; i < bytes.getItems().size(); i++) {
            String value = bytes.getItems().get(i);
            String replacement = Integer
                    .toString(Short.parseShort(value, view.radix), radix.radix);
            bytes.getItems().set(i, replacement);
        }
    }
}
