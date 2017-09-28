package gui.userdata;

import java.io.File;
import java.util.List;

public interface XStreamListWrapper<T> {
    File getAssociatedFile();
    boolean isDirty();
    List<T> getItems();
    boolean contains(T item);
    void addItem(T item);
    boolean removeItem(T item);
}
