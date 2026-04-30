package patterns.consistentcore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * In-memory cluster map: {@link #handleConnect} mimics the connect handshake response (no TCP).
 */
public final class MockKernelCluster {

    private final Map<String, MockKernelNode> nodes = new HashMap<>();

    public MockKernelCluster(List<MockKernelNode> nodeList) {
        for (MockKernelNode n : nodeList) {
            nodes.put(key(n.host(), n.port()), n);
        }
    }

    private static String key(String host, int port) {
        return host + ":" + port;
    }

    public ConnectHandshakeResult handleConnect(String host, int port) {
        MockKernelNode node = nodes.get(key(host, port));
        if (node == null) {
            throw new IllegalArgumentException("unknown server");
        }
        if (node.leader() && node.core() != null) {
            return new ConnectHandshakeResult(RequestId.LEADER_CONNECTED, null);
        }
        if (node.leaderHost() == null) {
            return new ConnectHandshakeResult(RequestId.LOOKING_FOR_LEADER, null);
        }
        return new ConnectHandshakeResult(
                RequestId.REDIRECT_TO_LEADER,
                new RedirectToLeaderResponse(node.leaderHost(), node.leaderPort()));
    }

    public InMemoryConsistentCore coreOnLeader() {
        for (MockKernelNode n : nodes.values()) {
            if (n.leader() && n.core() != null) {
                return n.core();
            }
        }
        return null;
    }

    public record ConnectHandshakeResult(RequestId requestId, RedirectToLeaderResponse redirect) {
        public ConnectHandshakeResult {
            Objects.requireNonNull(requestId);
        }
    }
}
