package patterns.consistentcore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Single-process stand-in for the replicated log + leader (teaching only).
 */
public final class InMemoryConsistentCore implements ConsistentCore {

    private final Map<String, String> data = new ConcurrentHashMap<>();
    private final Map<String, Set<Consumer<WatchEvent>>> watchers = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> leaseTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "consistent-core-lease");
                t.setDaemon(true);
                return t;
            });

    @Override
    public CompletableFuture<Void> put(String key, String value) {
        return CompletableFuture.runAsync(() -> {
            data.put(key, value);
            notifyPrefix(key, new WatchEvent(key, "put"));
        });
    }

    @Override
    public List<String> get(String keyPrefix) {
        List<String> keys = new ArrayList<>(data.keySet());
        Collections.sort(keys);
        List<String> out = new ArrayList<>();
        for (String k : keys) {
            if (k.startsWith(keyPrefix)) {
                out.add(data.get(k));
            }
        }
        return out;
    }

    @Override
    public CompletableFuture<Void> registerLease(String name, long ttl) {
        return CompletableFuture.runAsync(() -> {
            leaseTasks.compute(name, (n, prev) -> {
                if (prev != null) {
                    prev.cancel(false);
                }
                return scheduler.schedule(
                        () -> {
                            leaseTasks.remove(name);
                            notifyPrefix(name, new WatchEvent(name, "lease_expired"));
                        },
                        ttl,
                        TimeUnit.MILLISECONDS);
            });
        });
    }

    @Override
    public void refreshLease(String name) {
        if (leaseTasks.containsKey(name)) {
            registerLease(name, TimeUnit.SECONDS.toMillis(5)).join();
        }
    }

    @Override
    public void watch(String name, Consumer<WatchEvent> watchCallback) {
        watchers.computeIfAbsent(name, k -> ConcurrentHashMap.newKeySet()).add(watchCallback);
    }

    private void notifyPrefix(String changedKey, WatchEvent event) {
        watchers.forEach(
                (prefix, callbacks) -> {
                    if (changedKey.startsWith(prefix)) {
                        for (Consumer<WatchEvent> cb : callbacks) {
                            cb.accept(event);
                        }
                    }
                });
    }

    /** Call from demos/tests if you need a clean shutdown. */
    public void shutdown() {
        scheduler.shutdownNow();
    }
}
