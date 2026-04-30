package patterns.consistentcore;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Consistent metadata kernel API (see content/consistent-core.md).
 *
 * <p>Production: backed by a small Raft (etc.) cluster; this interface is what large data
 * clusters use for coordination without running quorum logic on every node.
 */
public interface ConsistentCore {

    CompletableFuture<Void> put(String key, String value);

    List<String> get(String keyPrefix);

    /** @param ttl time-to-live in milliseconds */
    CompletableFuture<Void> registerLease(String name, long ttl);

    void refreshLease(String name);

    void watch(String name, Consumer<WatchEvent> watchCallback);
}
