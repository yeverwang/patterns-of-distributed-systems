package patterns.consistentcore;

/** Client sends {@link #CONNECT_REQUEST}; server responds with one of the others. */
public enum RequestId {
    CONNECT_REQUEST(1),
    REDIRECT_TO_LEADER(2),
    LOOKING_FOR_LEADER(3),
    LEADER_CONNECTED(4);

    private final int id;

    RequestId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
