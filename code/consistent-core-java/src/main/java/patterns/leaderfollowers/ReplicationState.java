package patterns.leaderfollowers;

/**
 * Generation clock + current leader + role + vote tracker (pattern article).
 */
public final class ReplicationState {

    public static final int NO_LEADER_ID = -1;

    private long generation;
    private int leaderId = NO_LEADER_ID;
    private ServerRole role = ServerRole.LOOKING_FOR_LEADER;
    private final VoteTracker voteTracker = new VoteTracker();

    public long getGeneration() {
        return generation;
    }

    public void setGeneration(long generation) {
        this.generation = generation;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    public ServerRole getRole() {
        return role;
    }

    public void setRole(ServerRole role) {
        this.role = role;
    }

    public VoteTracker getVoteTracker() {
        return voteTracker;
    }

    /** Cold-start / retry election attempts. */
    public void resetForColdStart() {
        generation = 0;
        leaderId = NO_LEADER_ID;
        role = ServerRole.LOOKING_FOR_LEADER;
        voteTracker.clear();
    }

    public void incrementGenerationAndResetVote() {
        generation = generation + 1;
        voteTracker.clear();
    }
}
