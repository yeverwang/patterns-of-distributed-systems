package patterns.leaderfollowers;

public record VoteResponse(boolean granted, int serverId, long generation, long lastLogEntryId) {

    public static VoteResponse granted(int serverId, long generation, long lastLogEntryId) {
        return new VoteResponse(true, serverId, generation, lastLogEntryId);
    }

    public static VoteResponse rejected(int serverId, long generation, long lastLogEntryId) {
        return new VoteResponse(false, serverId, generation, lastLogEntryId);
    }
}
