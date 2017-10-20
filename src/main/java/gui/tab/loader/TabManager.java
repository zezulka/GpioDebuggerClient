package gui.tab.loader;

import java.net.InetAddress;
import javafx.scene.control.Tab;

public interface TabManager {

    Tab findTabByAddress(InetAddress address);

    void removeTab(InetAddress address);

    void addTab(TabAddressPair pair);
}
