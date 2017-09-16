package gui.layouts.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Operations available for communicating with interfaces
 * available on the embedded device.
 */
public enum Operation {
    READ("read"),
    WRITE("write");

    private final String op;

    Operation(String op) {
        this.op = op;
    }

    public String getOp() {
        return this.op;
    }

    public boolean isReadOperation() {
        return this.equals(Operation.READ);
    }

    public boolean isWriteOperation() {
        return this.equals(Operation.WRITE);
    }

    public static ObservableList<Operation> observableValues() {
        return FXCollections.observableArrayList(values());
    }

    @Override
    public String toString() {
        return this.getOp();
    }
}
