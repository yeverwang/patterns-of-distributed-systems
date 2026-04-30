package patterns.leaderfollowers;

import java.util.List;
import java.util.Objects;

/**
 * Leader election vote handling as in leader-and-followers.md ({@code ReplicationModule} excerpt).
 *
 * <p>{@link #seekLeadership} corresponds to starting an election, requesting votes from peers, and
 * becoming leader on quorum (production stacks layer RPCs and heartbeat on top).
 */
public final class ReplicationModule {

    private final int serverId;
    private final ReplicationState replicationState;
    private final Wal wal;

    public ReplicationModule(int serverId, ReplicationState replicationState, Wal wal) {
        this.serverId = serverId;
        this.replicationState = Objects.requireNonNull(replicationState);
        this.wal = Objects.requireNonNull(wal);
    }

    public int serverId() {
        return serverId;
    }

    public ReplicationState replicationState() {
        return replicationState;
    }

    public Wal wal() {
        return wal;
    }

    public long lastLogIndex() {
        return wal.getLastLogEntryId();
    }

    /**
     * Article: increment generation, self-vote, {@code requestVoteFrom(followers)} — here we poll
     * all peers and count grants until quorum.
     */
    public boolean seekLeadership(List<ReplicationModule> peersIncludingSelf) {
        replicationState.incrementGenerationAndResetVote();
        registerSelfVote();
        VoteRequest voteRequest =
                new VoteRequest(serverId, replicationState.getGeneration(), wal.getLastLogEntryId());
        int grants = 1;
        int quorum = peersIncludingSelf.size() / 2 + 1;
        for (ReplicationModule peer : peersIncludingSelf) {
            if (peer.serverId == this.serverId) {
                continue;
            }
            VoteResponse response = peer.handleVoteRequest(voteRequest);
            if (response.granted()) {
                grants++;
            }
        }
        if (grants >= quorum) {
            becomeLeader();
            return true;
        }
        transitionTo(ServerRole.LOOKING_FOR_LEADER);
        return false;
    }

    private void registerSelfVote() {
        replicationState.getVoteTracker().registerVote(serverId);
    }

    /** Exposed for {@link LeaderElection}; article uses package-private handlers on followers. */
    public VoteResponse handleVoteRequest(VoteRequest voteRequest) {
        VoteTracker voteTracker = replicationState.getVoteTracker();
        Long requestGeneration = voteRequest.generation();
        if (replicationState.getGeneration() > requestGeneration) {
            return rejectVote();
        }
        if (replicationState.getGeneration() < requestGeneration) {
            becomeFollower(-1, requestGeneration);
            voteTracker.registerVote(voteRequest.serverId());
            return grantVote();
        }
        return handleVoteRequestForSameGeneration(voteRequest);
    }

    private VoteResponse handleVoteRequestForSameGeneration(VoteRequest voteRequest) {
        Long requestGeneration = voteRequest.generation();
        VoteTracker voteTracker = replicationState.getVoteTracker();

        if (voteTracker.alreadyVoted()) {
            return voteTracker.grantedVoteForSameServer(voteRequest.serverId())
                    ? grantVote()
                    : rejectVote();
        }

        if (voteRequest.logIndex() >= wal.getLastLogEntryId()) {
            becomeFollower(ReplicationState.NO_LEADER_ID, requestGeneration);
            voteTracker.registerVote(voteRequest.serverId());
            return grantVote();
        }
        return rejectVote();
    }

    private void becomeFollower(int leaderId, long generation) {
        if (replicationState.getGeneration() != generation) {
            replicationState.getVoteTracker().clear();
        }
        replicationState.setGeneration(generation);
        replicationState.setLeaderId(leaderId);
        transitionTo(ServerRole.FOLLOWING);
    }

    private void becomeLeader() {
        replicationState.setLeaderId(serverId);
        transitionTo(ServerRole.LEADING);
    }

    /** After election completes, normalize non-leaders to follow the chosen leader. */
    public void forceFollower(int leaderId, long generation) {
        replicationState.setGeneration(generation);
        replicationState.setLeaderId(leaderId);
        transitionTo(ServerRole.FOLLOWING);
    }

    private void transitionTo(ServerRole role) {
        replicationState.setRole(role);
    }

    private VoteResponse grantVote() {
        return VoteResponse.granted(serverId, replicationState.getGeneration(), wal.getLastLogEntryId());
    }

    private VoteResponse rejectVote() {
        return VoteResponse.rejected(serverId, replicationState.getGeneration(), wal.getLastLogEntryId());
    }
}
