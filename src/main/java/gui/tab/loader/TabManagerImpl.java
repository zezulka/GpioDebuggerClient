package gui.tab.loader;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TabManagerImpl implements TabManager {

    private final Set<TabAddressPair> pairs = new HashSet<>();
    private final TabPane tabPane;
    private static final Logger LOGGER
            = LoggerFactory.getLogger(TabManagerImpl.class);

    public TabManagerImpl(TabPane tabPane) {
        Objects.requireNonNull(tabPane, "tabPane cannot be null");
        this.tabPane = tabPane;
    }

    @Override
    public Tab findTabByAddress(InetAddress address) {
        for (Tab t : tabPane.getTabs()) {
            if (t.getId().equals(address.getHostAddress())) {
                return t;
            }
        }
        throw new IllegalArgumentException("Address not found");
    }

    @Override
    public void addTab(TabAddressPair pair) {
        pairs.add(pair);
        Platform.runLater(() -> {
            tabPane.getTabs().add(pair.getTab());
            tabPane.getSelectionModel().select(pair.getTab());
        });
    }

    /**
     * @throws IllegalArgumentException if no tab has {@code address} associated
     * to it
     */
    @Override
    public void removeTab(InetAddress address) {
        TabAddressPair toRemove = null;
        for (TabAddressPair pair : pairs) {
            if (pair.getAddress().equals(address)) {
                toRemove = pair;
                break;
            }
        }
        if (toRemove == null) {
            LOGGER.debug("no such Tab with this address exists: " + address);
            throw new IllegalArgumentException();
        }
        toRemove.getTab().getTabPane().getTabs().remove(toRemove.getTab());
        pairs.remove(toRemove);
    }
}
