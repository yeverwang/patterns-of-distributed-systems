package patterns.leaderfollowers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Runs an in-process election: try candidates in order of “most up-to-date” log, then higher
 * {@link ReplicationModule#serverId()} (implementation-specific tie-break from the article).
 */
public final class LeaderElection {

    private LeaderElection() {}

    public static ReplicationModule electLeader(List<ReplicationModule> modules) {
        if (modules.isEmpty()) {
            throw new IllegalArgumentException("no modules");
        }
        List<ReplicationModule> order = new ArrayList<>(modules);
        order.sort(
                Comparator.comparingLong(ReplicationModule::lastLogIndex)
                        .reversed()
                        .thenComparing(Comparator.comparingInt(ReplicationModule::serverId).reversed()));

        for (ReplicationModule candidate : order) {
            resetForColdStart(modules);
            if (candidate.seekLeadership(modules)) {
                int lid = candidate.serverId();
                long gen = candidate.replicationState().getGeneration();
                for (ReplicationModule m : modules) {
                    if (m.serverId() != lid) {
                        m.forceFollower(lid, gen);
                    }
                }
                return candidate;
            }
        }
        throw new IllegalStateException("could not elect a leader");
    }

    private static void resetForColdStart(List<ReplicationModule> modules) {
        for (ReplicationModule m : modules) {
            m.replicationState().resetForColdStart();
        }
    }
}
