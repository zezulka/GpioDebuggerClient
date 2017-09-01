package gui;

import javafx.scene.control.TabPane;

/**
 *
 * @author miloslav
 */
public abstract class AbstractTabLoader implements TabLoader {

    private final TabPane tabPane;

    public AbstractTabLoader(TabPane tabPane) {
        this.tabPane = tabPane;
    }
}
