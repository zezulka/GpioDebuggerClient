package gui.layouts.controllers;

import java.time.LocalTime;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class ByteArrayResponse {

    private final ObjectProperty<LocalTime> time;
    private final ObjectProperty<List<String>> bytes;

    public ByteArrayResponse(LocalTime time, List<String> bytes) {
        this.time = new SimpleObjectProperty<>(time);
        this.bytes = new SimpleObjectProperty<>(bytes);
    }

    public LocalTime getTime() {
        return time.get();
    }

    public List<String> getBytes() {
        return bytes.get();
    }
}
