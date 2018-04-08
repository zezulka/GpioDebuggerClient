package gui.tab.loader;

import gui.misc.Feature;
import javafx.scene.control.Tab;
import protocol.BoardType;

import java.net.InetAddress;
import java.util.Collection;

public interface TabLoader {

    Tab loadNewTab(InetAddress address, BoardType type,
            Collection<Feature> features);
}
