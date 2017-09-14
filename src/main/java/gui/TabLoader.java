package gui;

import gui.feature.Feature;
import java.net.InetAddress;
import java.util.Collection;
import javafx.scene.control.Tab;
import protocol.BoardType;

public interface TabLoader {

    Tab loadNewTab(InetAddress address, BoardType type,
            Collection<Feature> features);
}
