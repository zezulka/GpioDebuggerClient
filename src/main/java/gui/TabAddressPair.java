package gui;

import java.net.InetAddress;
import java.util.Objects;
import javafx.scene.control.Tab;

/**
 *
 * @author Miloslav Zezulka, 2017
 */
public final  class TabAddressPair {
    private final Tab tab;
    private final InetAddress address;

    public TabAddressPair(Tab tab, InetAddress address) {
        this.tab = tab;
        this.address = address;
    }

    public Tab getTab() {
        return tab;
    }

    public InetAddress getAddress() {
        return address;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.tab);
        hash = 37 * hash + Objects.hashCode(this.address);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TabAddressPair other = (TabAddressPair) obj;
        return this.tab.equals(other.tab) && this.address.equals(other.address);
    }
}
