package patterns.consistentcore;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Same control flow as consistent-core.md: {@code establishConnectionToLeader} scans servers,
 * handles redirect and "looking for leader", then knows the leader address.
 */
public final class ConsistentCoreClient {

    private static final Logger LOG = Logger.getLogger(ConsistentCoreClient.class.getName());

    private final MockKernelCluster cluster;
    private InetSocketAddress leader;

    public ConsistentCoreClient(MockKernelCluster cluster) {
        this.cluster = Objects.requireNonNull(cluster);
    }

    public void establishConnectionToLeader(List<InetSocketAddress> servers) {
        for (InetSocketAddress server : servers) {
            try {
                LOG.info(() -> "Trying to connect to " + server);
                MockKernelCluster.ConnectHandshakeResult response =
                        sendConnectRequest(server.getHostString(), server.getPort());

                if (isRedirectResponse(response.requestId()) && response.redirect() != null) {
                    redirectToLeader(response.redirect());
                    return;
                }
                if (isLookingForLeader(response.requestId())) {
                    LOG.info("Server is looking for leader. Trying next server");
                    continue;
                }
                LOG.info("Found leader. Establishing connection.");
                leader = server;
                return;
            } catch (RuntimeException e) {
                LOG.info(() -> "Unable to connect to " + server + ": " + e.getMessage());
            }
        }
        throw new IllegalStateException("could not find a leader among servers");
    }

    /** Same role as the article's {@code sendConnectRequest} over a socket; here in-process. */
    private MockKernelCluster.ConnectHandshakeResult sendConnectRequest(String host, int port) {
        return cluster.handleConnect(host, port);
    }

    private void redirectToLeader(RedirectToLeaderResponse response) {
        leader = new InetSocketAddress(response.leaderHost(), response.leaderPort());
        LOG.info(
                () -> "Connected via redirect to leader at "
                        + response.leaderHost()
                        + ":"
                        + response.leaderPort());
    }

    private static boolean isRedirectResponse(RequestId id) {
        return id == RequestId.REDIRECT_TO_LEADER;
    }

    private static boolean isLookingForLeader(RequestId id) {
        return id == RequestId.LOOKING_FOR_LEADER;
    }

    public InetSocketAddress getLeader() {
        return leader;
    }
}
