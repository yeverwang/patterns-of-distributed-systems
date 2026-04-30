package patterns.leaderfollowers;

public record VoteRequest(int serverId, long generation, long logIndex) {}
