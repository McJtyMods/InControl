package mcjty.incontrol.rules.support;

import java.util.Set;

/** Snapshots join status before rules run and persists it in the entity's saved tags. */
public final class FirstJoinTracker {

    public static final String JOINED_TAG = "incontrol:joined";

    private FirstJoinTracker() {
    }

    public static boolean beginJoin(boolean loadedFromDisk, Set<String> tags) {
        boolean alreadyJoined = tags.contains(JOINED_TAG);
        // Mark disk-loaded mobs too, so a later dimension transfer cannot count as a first join.
        tags.add(JOINED_TAG);
        return !loadedFromDisk && !alreadyJoined;
    }
}
