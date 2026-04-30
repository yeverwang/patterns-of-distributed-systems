package patterns.consistentcore;

/** Fired when a watched prefix changes or a lease expires (teaching simplification). */
public final class WatchEvent {
    private final String key;
    private final String kind; // "put" | "delete" | "lease_expired"

    public WatchEvent(String key, String kind) {
        this.key = key;
        this.kind = kind;
    }

    public String key() {
        return key;
    }

    public String kind() {
        return kind;
    }

    @Override
    public String toString() {
        return "WatchEvent{" + "key='" + key + '\'' + ", kind='" + kind + '\'' + '}';
    }
}
