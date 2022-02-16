package mcjty.incontrol.data;

import mcjty.incontrol.InControl;

import java.util.ArrayList;
import java.util.List;

public class Statistics {

    private static final List<SpawnStat> SPAWN_STATS = new ArrayList<>();
    private static final List<SpawnerStat> SPAWNER_STATS = new ArrayList<>();

    public static void addSpawnerStat(int rule) {
        while (rule >= SPAWNER_STATS.size()) {
            SPAWNER_STATS.add(new SpawnerStat());
        }
        SpawnerStat stat = SPAWNER_STATS.get(rule);
        stat.counter++;
    }

    public static void addSpawnStat(int rule, boolean deny) {
        while (rule >= SPAWN_STATS.size()) {
            SPAWN_STATS.add(new SpawnStat());
        }
        SpawnStat stat = SPAWN_STATS.get(rule);
        stat.counter++;
        stat.deny = deny;
    }

    public static void clear() {
        SPAWN_STATS.clear();
        SPAWNER_STATS.clear();
    }

    public static void dump() {
        InControl.setup.getLogger().info("### Spawner ###");
        for (int i = 0 ; i < SPAWNER_STATS.size() ; i++) {
            SpawnerStat stat = SPAWNER_STATS.get(i);
            InControl.setup.getLogger().info("Rule " + i + " spawned " + stat.counter + " mobs");
        }
        InControl.setup.getLogger().info("### Spawn ###");
        for (int i = 0; i < SPAWN_STATS.size() ; i++) {
            SpawnStat stat = SPAWN_STATS.get(i);
            InControl.setup.getLogger().info("Rule " + i + " fired " + stat.counter + " times (" + (stat.deny ? "deny)": "allow)"));
        }
    }

    private static class SpawnStat {
        private int counter;
        private boolean deny;
    }

    private static class SpawnerStat {
        private int counter;
    }
}
