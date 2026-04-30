package patterns.leaderfollowers;

/**
 * From leader-and-followers: each server is either electing, following the leader, or leading.
 */
public enum ServerRole {
    LOOKING_FOR_LEADER,
    FOLLOWING,
    LEADING
}
