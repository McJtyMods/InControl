package mcjty.incontrol.rules.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks every distinct chunk radius that any loaded "perlocal" mincount/maxcount rule needs.
 * RuleCache uses this to know which PlayerDistanceMap instances to keep alive.
 * Gets populated while rules are parsed and is reset before a rule reload re-parses everything.
 */
public final class LocalDistanceRegistry {
    // Distinct chunk radii to track
    private static final Set<Integer> RADII = new HashSet<>();

    private LocalDistanceRegistry() {}

    // When a new chunk radius is needed it gets registered. We skip radii of distance less than 0 as they don't really make too much sense.
    public static void register(int chunkRadius) {
        if (chunkRadius >= 0) {
            RADII.add(chunkRadius);
        }
    }

    // Straightforward
    public static Set<Integer> getRadii() {
        return Collections.unmodifiableSet(RADII);
    }

    // We reset radii when reloading rules
    public static void reset() {
        RADII.clear();
    }
}