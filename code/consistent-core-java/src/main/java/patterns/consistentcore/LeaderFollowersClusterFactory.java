package patterns.consistentcore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import patterns.leaderfollowers.LeaderElection;
import patterns.leaderfollowers.ReplicationModule;
import patterns.leaderfollowers.ReplicationState;
import patterns.leaderfollowers.SimpleWal;

/**
 * Builds a {@link MockKernelCluster} after running leader election (see content/leader-and-followers.md).
 */
public final class LeaderFollowersClusterFactory {

    public record NodeSpec(int serverId, String nodeId, String host, int port, long initialLastLogIndex) {}

    public static MockKernelCluster buildCluster(List<NodeSpec> specs, InMemoryConsistentCore leaderCore) {
        List<ReplicationModule> modules = new ArrayList<>();
        Map<Integer, NodeSpec> byId = new HashMap<>();
        for (NodeSpec s : specs) {
            byId.put(s.serverId(), s);
            ReplicationState state = new ReplicationState();
            SimpleWal wal = new SimpleWal(s.initialLastLogIndex());
            modules.add(new ReplicationModule(s.serverId(), state, wal));
        }
        ReplicationModule leaderModule = LeaderElection.electLeader(modules);
        NodeSpec leaderSpec = byId.get(leaderModule.serverId());
        String leaderHost = leaderSpec.host();
        int leaderPort = leaderSpec.port();

        List<MockKernelNode> nodes = new ArrayList<>();
        for (NodeSpec s : specs) {
            boolean isLeader = s.serverId() == leaderModule.serverId();
            nodes.add(
                    new MockKernelNode(
                            s.nodeId(),
                            s.host(),
                            s.port(),
                            isLeader,
                            leaderHost,
                            leaderPort,
                            isLeader ? leaderCore : null));
        }
        return new MockKernelCluster(nodes);
    }

    private LeaderFollowersClusterFactory() {}
}
