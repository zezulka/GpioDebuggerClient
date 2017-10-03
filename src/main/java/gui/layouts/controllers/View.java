package gui.layouts.controllers;

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
