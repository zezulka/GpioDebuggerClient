package gui;

import java.net.InetAddress;
import javafx.scene.control.Tab;
import protocol.BoardType;

public interface TabLoader {
    Tab loadNewTab(InetAddress address, BoardType type);
}
