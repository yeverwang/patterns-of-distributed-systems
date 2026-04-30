package patterns.leaderfollowers;

/** Minimal WAL surface used for leader eligibility (“latest” log index). */
public interface Wal {

    long getLastLogEntryId();
}
