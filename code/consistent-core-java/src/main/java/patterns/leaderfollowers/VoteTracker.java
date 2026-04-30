package patterns.leaderfollowers;

/**
 * Per-generation vote bookkeeping: at most one grant per term (article’s {@code VoteTracker}).
 */
public final class VoteTracker {

    private Integer votedFor;

    public boolean alreadyVoted() {
        return votedFor != null;
    }

    public void registerVote(int serverId) {
        this.votedFor = serverId;
    }

    public boolean grantedVoteForSameServer(int serverId) {
        return alreadyVoted() && votedFor == serverId;
    }

    public void clear() {
        votedFor = null;
    }
}
