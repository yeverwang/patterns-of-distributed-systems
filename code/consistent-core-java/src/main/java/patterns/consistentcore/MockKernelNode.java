package patterns.consistentcore;

import java.util.Objects;

/** One replica: leader holds {@link InMemoryConsistentCore}; followers carry redirect hint. */
public final class MockKernelNode {

    private final String nodeId;
    private final String host;
    private final int port;
    private final boolean leader;
    /** Follower only: where the current leader listens; null during election. */
    private final String leaderHost;
    private final int leaderPort;
    private final InMemoryConsistentCore core;

    public MockKernelNode(
            String nodeId,
            String host,
            int port,
            boolean leader,
            String leaderHost,
            int leaderPort,
            InMemoryConsistentCore core) {
        this.nodeId = Objects.requireNonNull(nodeId);
        this.host = Objects.requireNonNull(host);
        this.port = port;
        this.leader = leader;
        this.leaderHost = leaderHost;
        this.leaderPort = leaderPort;
        this.core = core;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public boolean leader() {
        return leader;
    }

    public String leaderHost() {
        return leaderHost;
    }

    public int leaderPort() {
        return leaderPort;
    }

    public InMemoryConsistentCore core() {
        return core;
    }

    public String nodeId() {
        return nodeId;
    }
}
