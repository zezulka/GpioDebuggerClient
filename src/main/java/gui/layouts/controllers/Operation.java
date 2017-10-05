package gui.layouts.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Operations available for communicating with interfaces
 * available on the embedded device.
 */
public enum Operation {
    READ("read only"),
    WRITE("write only"),
    WRITE_READ("write and read");

    private final String op;

    Operation(String op) {
        this.op = op;
    }

    public String getOp() {
        return this.op;
    }

    public static ObservableList<Operation> observableValues() {
        return FXCollections.observableArrayList(values());
    }

    @Override
    public String toString() {
        return this.getOp();
    }
}
