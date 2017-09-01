package userdata;

import java.util.List;
import javafx.collections.FXCollections;

public abstract class XStreamListWrapper<T> {

    private final List<T> list;
    private boolean dirty;

    public XStreamListWrapper(List<T> list) {
        this.list = FXCollections.observableArrayList(list);
        this.dirty = false;
    }

    public final List<T> getItems() {
        return list;
    }

    public final void addNewItem(T item) {
        this.dirty = true;
        list.add(item);
    }

    public final boolean isDirty() {
        if (!list.isEmpty() && list.get(0) instanceof DeviceValueObject) {
            return dirty && isDeepDirty();
        }
        return dirty;
    }

    private boolean isDeepDirty() {
        return list.stream()
                .map((device) -> ((DeviceValueObject) device).isDirty())
                .reduce(false, (acc, curr) -> acc || curr);
    }

    public final boolean contains(T item) {
        return list.contains(item);
    }
}
