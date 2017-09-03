package gui;

import gui.feature.Feature;
import java.net.InetAddress;
import java.util.Set;
import javafx.scene.control.Tab;
import protocol.BoardType;

public interface TabLoader {
    Tab loadNewTab(InetAddress address, BoardType type, Set<Feature> features);
}
