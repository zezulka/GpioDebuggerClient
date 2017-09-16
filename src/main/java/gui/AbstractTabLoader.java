package gui;

import javafx.scene.control.TabPane;

public abstract class AbstractTabLoader implements TabLoader {

    private final TabPane tabPane;

    public AbstractTabLoader(TabPane tabPane) {
        this.tabPane = tabPane;
    }
}
