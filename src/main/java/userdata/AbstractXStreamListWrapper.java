package userdata;

import java.util.List;
import javafx.collections.FXCollections;

public abstract class AbstractXStreamListWrapper<T>
        implements XStreamListWrapper<T> {

    private final List<T> list;
    private boolean dirty;

    public AbstractXStreamListWrapper(List<T> list) {
        this.list = FXCollections.observableArrayList(list);
        this.dirty = false;
    }

    @Override
    public final List<T> getItems() {
        return list;
    }

    @Override
    public final void addItem(T item) {
        if (!list.contains(item)) {
            this.dirty = true;
            list.add(item);
        }
    }

    @Override
    public final boolean removeItem(T item) {
        boolean successful = list.remove(item);
        if (successful) {
            this.dirty = true;
        }
        return successful;
    }

    @Override
    public final boolean isDirty() {
        if (!list.isEmpty() && list.get(0) instanceof DeviceValueObject) {
            return dirty || isDeepDirty();
        }
        return dirty;
    }

    private boolean isDeepDirty() {
        return list.stream()
                .map((device) -> ((DeviceValueObject) device).isDirty())
                .reduce(false, (acc, curr) -> acc || curr);
    }

    private boolean contains(T item) {
        return list.contains(item);
    }
}
