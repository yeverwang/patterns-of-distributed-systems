package patterns.consistentcore;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Runnable narrative matching content/consistent-core.md and leader election from
 * leader-and-followers.md (in-process vote round then membership keys + leader discovery).
 *
 * <p>Run: {@code mvn -f code/consistent-core-java exec:java}
 */
public final class ConsistentCoreDemo {

    private static final Logger LOG = Logger.getLogger(ConsistentCoreDemo.class.getName());

    public static void main(String[] args) throws Exception {
        InMemoryConsistentCore core = new InMemoryConsistentCore();
        try {
            MockKernelCluster cluster =
                    LeaderFollowersClusterFactory.buildCluster(
                            Arrays.asList(
                                    new LeaderFollowersClusterFactory.NodeSpec(
                                            1, "n1", "127.0.0.1", 9000, 0),
                                    new LeaderFollowersClusterFactory.NodeSpec(
                                            2, "n2", "127.0.0.1", 9001, 0),
                                    new LeaderFollowersClusterFactory.NodeSpec(
                                            3, "n3", "127.0.0.1", 9002, 0)),
                            core);
            ConsistentCoreClient client = new ConsistentCoreClient(cluster);

            client.establishConnectionToLeader(
                    Arrays.asList(
                            new InetSocketAddress("127.0.0.1", 9000),
                            new InetSocketAddress("127.0.0.1", 9001),
                            new InetSocketAddress("127.0.0.1", 9002)));

            InetSocketAddress expected = new InetSocketAddress("127.0.0.1", 9002);
            if (!expected.equals(client.getLeader())) {
                throw new AssertionError("leader: " + client.getLeader());
            }

            core.put("/servers/1", "{address:192.168.199.10, port:8000}").join();
            core.put("/servers/2", "{address:192.168.199.11, port:8000}").join();
            core.put("/servers/3", "{address:192.168.199.12, port:8000}").join();

            List<String> values = core.get("/servers");
            List<String> expectedValues =
                    Arrays.asList(
                            "{address:192.168.199.10, port:8000}",
                            "{address:192.168.199.11, port:8000}",
                            "{address:192.168.199.12, port:8000}");
            if (!values.equals(expectedValues)) {
                throw new AssertionError("got " + values + " expected " + expectedValues);
            }
            LOG.info(() -> "get(\"/servers\") => " + values);
            LOG.info("Demo OK: prefix metadata + leader discovery.");
        } finally {
            core.shutdown();
        }
    }

    private ConsistentCoreDemo() {}
}
