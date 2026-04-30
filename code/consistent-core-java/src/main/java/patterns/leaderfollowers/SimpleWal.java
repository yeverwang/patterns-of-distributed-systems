package patterns.leaderfollowers;

/** Teaching stub: last appended entry id only. */
public final class SimpleWal implements Wal {

    private long lastLogEntryId;

    public SimpleWal(long initialLastLogEntryId) {
        this.lastLogEntryId = initialLastLogEntryId;
    }

    @Override
    public long getLastLogEntryId() {
        return lastLogEntryId;
    }

    /** Simulate replication advancing the log (not used by vote path unless tests call it). */
    public void setLastLogEntryId(long id) {
        this.lastLogEntryId = id;
    }
}
