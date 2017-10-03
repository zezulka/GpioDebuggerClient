package gui.layouts.controllers;

import java.time.LocalTime;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class SpiResponse {

    private final ObjectProperty<LocalTime> time;
    private final ObjectProperty<List<String>> bytes;
    private final ObjectProperty<View> view;

    public SpiResponse(LocalTime time, List<String> bytes) {
        this.time = new SimpleObjectProperty<>(time);
        this.bytes = new SimpleObjectProperty<>(bytes);
        this.view = new SimpleObjectProperty<>(View.Hexadecimal);
    }

    public LocalTime getTime() {
        return time.get();
    }

    public List<String> getBytes() {
        return bytes.get();
    }

    public View getView() {
        return view.get();
    }
    
    public void setView(View view) {
        this.view.set(view);
    }
}
