package protocol;

public enum ListenerState {
    RUNNING("running"),
    NOT_RUNNING("not running");

    private final String desc;

    ListenerState(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
