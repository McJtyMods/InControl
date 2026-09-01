package mcjty.incontrol.rules.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;

import mcjty.incontrol.compat.CustomNPCSupport;
import mcjty.incontrol.mob.CNPCMob;
import mcjty.incontrol.setup.Config;
import mcjty.incontrol.setup.ModSetup;
import mcjty.incontrol.tools.distance.PlayerDistanceMap;
import mcjty.incontrol.tools.varia.Tools;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashMap;
import java.util.Map;

public class RuleCache {

    private final Map<ResourceKey<Level>, CachePerWorld> caches = new HashMap<>();

    public void performCount(LevelAccessor world) {
        ResourceKey<Level> key = Tools.getDimensionKey(world);
        // Get a cache or create it when it doesn't exist
        CachePerWorld cache = caches.computeIfAbsent(key, k -> new CachePerWorld());
        cache.count(world);
    }

    public void addMob(LevelAccessor world, Entity entity) {
        ResourceKey<Level> key = Tools.getDimensionKey(world);
        CachePerWorld cache = caches.computeIfAbsent(key, k -> new CachePerWorld());
        cache.addCountedMob(entity);
    }

    public void removeMob(LevelAccessor world, Entity entity) {
        ResourceKey<Level> key = Tools.getDimensionKey(world);
        CachePerWorld cache = caches.get(key);
        if (cache != null) {
            if (!cache.removeCountedMob(entity)) {
                // The cache is probably invalid so we need to recalculate
                cache.setDirtyCounter(0);
            }
        }
    }

    public int getValidSpawnChunks(LevelAccessor world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getValidSpawnChunks();
    }

    public int getValidPlayers(LevelAccessor world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getValidPlayers();
    }

    public int getCountAll(LevelAccessor world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountAll();
    }

    public int getCountPassive(LevelAccessor world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountPassive();
    }

    public int getCountHostile(LevelAccessor world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountHostile();
    }

    public int getCountNeutral(LevelAccessor world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountNeutral();
    }


    public int getCount(LevelAccessor world, EntityType entityType) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCount(entityType);
    }

    public int getNpcCount(LevelAccessor world, Entity entity) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getNpcCount(entity);
    }

    public int getCountPerMod(LevelAccessor world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(mod);
        return countPerMod == null ? 0 : countPerMod.total;
    }

    public int getCountPerModHostile(LevelAccessor world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(mod);
        return countPerMod == null ? 0 : countPerMod.hostile;
    }

    public int getCountPerModPassive(LevelAccessor world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(mod);
        return countPerMod == null ? 0 : countPerMod.passive;
    }

    public int getCountPerModAll(LevelAccessor world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(mod);
        return countPerMod == null ? 0 : countPerMod.total;
    }

//    public void registerSpawn(LevelAccessor world, EntityType entityType) {
//        CachePerWorld cache = getOrCreateCache(world);
//        cache.registerSpawn(entityType);
//    }

//    public void registerDespawn(LevelAccessor world, EntityType entityType) {
//        CachePerWorld cache = getOrCreateCache(world);
//        cache.registerDespawn(entityType);
//    }

    // Since the counting with 'perlocal' is local and not global, we need new count functions.
    // We pass minChunks and maxChunks as well as we're gonna count the entities between those boundaries.
    public int getLocalCountAll(LevelAccessor world, Entity entity, int minChunks, int maxChunks) {
        return getOrCreateCache(world).getLocalCountAll(entity, minChunks, maxChunks);
    }

    public int getLocalCountPassive(LevelAccessor world, Entity entity, int minChunks, int maxChunks) {
        return getOrCreateCache(world).getLocalCountPassive(entity, minChunks, maxChunks);
    }

    public int getLocalCountHostile(LevelAccessor world, Entity entity, int minChunks, int maxChunks) {
        return getOrCreateCache(world).getLocalCountHostile(entity, minChunks, maxChunks);
    }

    public int getLocalCountNeutral(LevelAccessor world, Entity entity, int minChunks, int maxChunks) {
        return getOrCreateCache(world).getLocalCountNeutral(entity, minChunks, maxChunks);
    }

    public int getLocalCount(LevelAccessor world, Entity entity, EntityType entityType, int minChunks, int maxChunks) {
        return getOrCreateCache(world).getLocalCount(entity, entityType, minChunks, maxChunks);
    }

    public int getLocalNpcCount(LevelAccessor world, Entity entity, int minChunks, int maxChunks) {
        return getOrCreateCache(world).getLocalNpcCount(entity, minChunks, maxChunks);
    }

    public int getLocalCountPerMod(LevelAccessor world, Entity entity, String mod, int minChunks, int maxChunks) {
        return getOrCreateCache(world).getLocalCountPerMod(entity, mod, minChunks, maxChunks);
    }

    public int getLocalCountPerModHostile(LevelAccessor world, Entity entity, String mod, int minChunks,
            int maxChunks) {
        return getOrCreateCache(world).getLocalCountPerModHostile(entity, mod, minChunks, maxChunks);
    }

    public int getLocalCountPerModPassive(LevelAccessor world, Entity entity, String mod, int minChunks,
            int maxChunks) {
        return getOrCreateCache(world).getLocalCountPerModPassive(entity, mod, minChunks, maxChunks);
    }

    public int getLocalCountPerModAll(LevelAccessor world, Entity entity, String mod, int minChunks, int maxChunks) {
        return getOrCreateCache(world).getLocalCountPerModAll(entity, mod, minChunks, maxChunks);
    }

    // public void registerSpawn(LevelAccessor world, EntityType entityType) {
    // CachePerWorld cache = getOrCreateCache(world);
    // cache.registerSpawn(entityType);
    // }

    // public void registerDespawn(LevelAccessor world, EntityType entityType) {
    // CachePerWorld cache = getOrCreateCache(world);
    // cache.registerDespawn(entityType);
    // }

    private CachePerWorld getOrCreateCache(LevelAccessor world) {
        ResourceKey<Level> key = Tools.getDimensionKey(world);
        CachePerWorld cache = caches.get(key);
        if (cache == null) {
            cache = new CachePerWorld();
            caches.put(key, cache);
        }
        return cache;
    }


    private static class CountPerMod {
        private int hostile;
        private int passive;
        private int neutral;
        private int total;
    }

    private static class CachePerWorld {
        // One PlayerDistanceMap per registered chunk radius
        private final Map<Integer, PlayerDistanceMap> distanceMaps = new HashMap<>();
        // For each radius, a map from each player to their PlayerLocalCounts
        private final Map<Integer, Map<ServerPlayer, PlayerLocalCounts>> localCountsByRadius = new HashMap<>();

        private final Map<EntityType, Integer> cachedCounters = new HashMap<>();
        private final Map<CNPCMob, Integer> cachedNpcCounters = new HashMap<>();
        private final Map<String, CountPerMod> countPerMod = new HashMap<>();
        private int countPassive = -1;
        private int countHostile = -1;
        private int countNeutral = -1;
        private int validSpawnChunks = -1;
        private int validPlayers = -1;
        private int dirtyCounter = 0;

        // Updates all registered PlayerDistanceMap instances for the current player positions and prunes the ones that are no longer needed.
        private void tickPlayerDistanceMap(LevelAccessor world) {
            ServerLevel sw = Tools.getServerWorld(world);
            List<ServerPlayer> players = sw.players();

            Set<Integer> radii = LocalDistanceRegistry.getRadii();

            for (Integer radius : radii) {
                distanceMaps.computeIfAbsent(radius, r -> new PlayerDistanceMap()).update(players, radius);
            }

            distanceMaps.keySet().retainAll(radii);
            localCountsByRadius.keySet().retainAll(radii);

            for (Map<ServerPlayer, PlayerLocalCounts> counts : localCountsByRadius.values()) {
                counts.keySet().retainAll(players);
            }
        }

        public int getValidSpawnChunks() {
            return validSpawnChunks;
        }

        public int getValidPlayers() {
            return validPlayers;
        }

        public int getCountAll() {
            return countHostile + countPassive + countNeutral;
        }

        public int getCountPassive() {
            return countPassive;
        }

        public int getCountHostile() {
            return countHostile;
        }

        public int getCountNeutral() {
            return countNeutral;
        }

        public int getDirtyCounter() {
            return dirtyCounter;
        }

        public void setDirtyCounter(int dirtyCounter) {
            this.dirtyCounter = dirtyCounter;
        }

        private int countValidPlayers(LevelAccessor world) {
            int cnt = 0;
            for (Player entityplayer : world.players()) {
                if (!entityplayer.isSpectator()) {
                    cnt++;
                }
            }
            return cnt;
        }

        private void count(LevelAccessor world) {
            tickPlayerDistanceMap(world);

            dirtyCounter--;
            if (dirtyCounter > 0) {
                return;
            }
            dirtyCounter = Config.CACHE_RETENTION_TICKS.get();
            ServerLevel sw1 = Tools.getServerWorld(world);
            validSpawnChunks = sw1.getChunkSource().chunkMap.size();
            validPlayers = countValidPlayers(world);

            cachedCounters.clear();
            countPerMod.clear();
            cachedNpcCounters.clear();
            localCountsByRadius.clear();
            countPassive = 0;
            countHostile = 0;
            countNeutral = 0;

            ServerLevel sw = Tools.getServerWorld(world);

            sw.getEntities().getAll().forEach(this::addCountedMob);
        }

        public void addCountedMob(Entity entity) {
            if (entity instanceof Mob) {
                int cnt = cachedCounters.getOrDefault(entity.getType(), 0) + 1;
                cachedCounters.put(entity.getType(), cnt);

                    String mod = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace();
                    CountPerMod count = countPerMod.computeIfAbsent(mod, s -> new CountPerMod());
                    count.total++;

                if (entity instanceof Enemy) {
                    count.hostile++;
                    countHostile++;
                } else if (entity instanceof Animal) {
                    count.passive++;
                    countPassive++;
                } else {
                    count.neutral++;
                    countNeutral++;
                }

                if (ModSetup.customnpcs) {
                    if (CustomNPCSupport.hasNPCInterface(entity) && entity.getPersistentData().contains("InControlNatSpawnName") && entity.getPersistentData().contains("InControlNatSpawnTab")) {
                        CNPCMob mob = new CNPCMob(entity.getPersistentData().getInt("InControlNatSpawnTab"), entity.getPersistentData().getString("InControlNatSpawnName"));
                        cnt = cachedNpcCounters.getOrDefault(mob, 0) + 1;
                        cachedNpcCounters.put(mob, cnt);
                    }
                }
                boolean isHostile = entity instanceof Enemy;
                boolean isPassive = entity instanceof Animal;
                updateLocalCounts(entity, mod, isHostile, isPassive, +1);
            }
        }

        // This function returns false if the cache is probably invalid and needs to be recalculated
        public boolean removeCountedMob(Entity entity) {
            if (entity instanceof Mob) {
                int cnt = cachedCounters.getOrDefault(entity.getType(), 0);
                if (cnt > 0) {
                    cachedCounters.put(entity.getType(), cnt - 1);
                } else {
                    // We can't go negative so the cache is probably not up to date
                    return false;
                }

                String mod = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace();
                CountPerMod count = countPerMod.get(mod);
                if (count != null) {
                    count.total--;
                    if (entity instanceof Enemy) {
                        if (count.hostile <= 0 || countHostile <= 0) {
                            return false;
                        }
                        count.hostile--;
                        countHostile--;
                    } else if (entity instanceof Animal) {
                        if (count.passive <= 0 || countPassive <= 0) {
                            return false;
                        }
                        count.passive--;
                        countPassive--;
                    } else {
                        if (count.neutral <= 0 || countNeutral <= 0) {
                            return false;
                        }
                        count.neutral--;
                        countNeutral--;
                    }
                }
                boolean isHostile = entity instanceof Enemy;
                boolean isPassive = entity instanceof Animal;
                updateLocalCounts(entity, mod, isHostile, isPassive, -1);
            }
            return true;
        }

        // For a given entity, finds which players have it within each registered radius using the
        // PlayerDistanceMap and adds/removes it to/from their PlayerLocalCounts for that radius.
        private void updateLocalCounts(Entity entity, String mod, boolean isHostile, boolean isPassive, int delta) {
            long chunkKey = entity.chunkPosition().toLong();
            for (Map.Entry<Integer, PlayerDistanceMap> entry : distanceMaps.entrySet()) {
                PlayerDistanceMap map = entry.getValue();
                Map<ServerPlayer, PlayerLocalCounts> localCounts = localCountsByRadius.computeIfAbsent(entry.getKey(), r -> new HashMap<>());

                for (ServerPlayer player : map.getPlayersInRange(chunkKey)) {
                    PlayerLocalCounts counts = localCounts.computeIfAbsent(player, p -> new PlayerLocalCounts());
                    counts.perType.merge(entity.getType(), delta, Integer::sum);
                    CountPerMod cpm = counts.perMod.computeIfAbsent(mod, s -> new CountPerMod());
                    cpm.total += delta;
                    if (isHostile) {
                        counts.hostile += delta;
                        cpm.hostile += delta;
                    } else if (isPassive) {
                        counts.passive += delta;
                        cpm.passive += delta;
                    } else {
                        counts.neutral += delta;
                        cpm.neutral += delta;
                    }

                    if (ModSetup.customnpcs && CustomNPCSupport.hasNPCInterface(entity) && entity.getPersistentData().contains("InControlNatSpawnName") && entity.getPersistentData().contains("InControlNatSpawnTab")) {
                        CNPCMob mob = new CNPCMob(entity.getPersistentData().getInt("InControlNatSpawnTab"), entity.getPersistentData().getString("InControlNatSpawnName"));
                        counts.perNpc.merge(mob, delta, Integer::sum);
                    }
                }
            }
        }

        // For a given entity and radius range, it looks at every player who has that entity's chunk within their max-radius,
        // subtracts the min-radius count if applicable and returns the maximum across all nearby players.
        // The max is used because we want to know the most crowded player's neighborhood.
        private int getMaxLocal(Entity entity, int minChunks, int maxChunks, ToIntFunction<PlayerLocalCounts> field) {
            PlayerDistanceMap maxMap = distanceMaps.get(maxChunks);
            if (maxMap == null) {
                return 0;
            }
            Map<ServerPlayer, PlayerLocalCounts> maxCounts = localCountsByRadius.get(maxChunks);
            Map<ServerPlayer, PlayerLocalCounts> minCounts = minChunks >= 0 ? localCountsByRadius.get(minChunks) : null;

            long chunkKey = entity.chunkPosition().toLong();
            int best = 0;
            for (ServerPlayer player : maxMap.getPlayersInRange(chunkKey)) {
                PlayerLocalCounts maxC = maxCounts == null ? null : maxCounts.get(player);
                int maxVal = maxC == null ? 0 : field.applyAsInt(maxC);

                int minVal = 0;
                if (minCounts != null) {
                    PlayerLocalCounts minC = minCounts.get(player);
                    minVal = minC == null ? 0 : field.applyAsInt(minC);
                }
                best = Math.max(best, maxVal - minVal);
            }
            return best;
        }

        public int getLocalCountAll(Entity entity, int minChunks, int maxChunks) {
            return getMaxLocal(entity, minChunks, maxChunks, c -> c.hostile + c.passive + c.neutral);
        }

        public int getLocalCountPassive(Entity entity, int minChunks, int maxChunks) {
            return getMaxLocal(entity, minChunks, maxChunks, c -> c.passive);
        }

        public int getLocalCountHostile(Entity entity, int minChunks, int maxChunks) {
            return getMaxLocal(entity, minChunks, maxChunks, c -> c.hostile);
        }

        public int getLocalCountNeutral(Entity entity, int minChunks, int maxChunks) {
            return getMaxLocal(entity, minChunks, maxChunks, c -> c.neutral);
        }

        public int getLocalCount(Entity entity, EntityType entityType, int minChunks, int maxChunks) {
            return getMaxLocal(entity, minChunks, maxChunks, c -> c.perType.getOrDefault(entityType, 0));
        }

        public int getLocalNpcCount(Entity entity, int minChunks, int maxChunks) {
            if (ModSetup.customnpcs && CustomNPCSupport.hasNPCInterface(entity) && entity.getPersistentData().contains("InControlNatSpawnName") && entity.getPersistentData().contains("InControlNatSpawnTab")) {
                CNPCMob mob = new CNPCMob( entity.getPersistentData().getInt("InControlNatSpawnTab"), entity.getPersistentData().getString("InControlNatSpawnName"));
                return getMaxLocal(entity, minChunks, maxChunks, c -> c.perNpc.getOrDefault(mob, 0));
            }
            return getLocalCount(entity, entity.getType(), minChunks, maxChunks);
        }

        public int getLocalCountPerMod(Entity entity, String mod, int minChunks, int maxChunks) {
            return getMaxLocal(entity, minChunks, maxChunks, c -> {
                CountPerMod m = c.perMod.get(mod);
                return m == null ? 0 : m.total;
            });
        }

        public int getLocalCountPerModHostile(Entity entity, String mod, int minChunks, int maxChunks) {
            return getMaxLocal(entity, minChunks, maxChunks, c -> {
                CountPerMod m = c.perMod.get(mod);
                return m == null ? 0 : m.hostile;
            });
        }

        public int getLocalCountPerModPassive(Entity entity, String mod, int minChunks, int maxChunks) {
            return getMaxLocal(entity, minChunks, maxChunks, c -> {
                CountPerMod m = c.perMod.get(mod);
                return m == null ? 0 : m.passive;
            });
        }

        public int getLocalCountPerModAll(Entity entity, String mod, int minChunks, int maxChunks) {
            return getLocalCountPerMod(entity, mod, minChunks, maxChunks);
        }

        public int getCount(EntityType entityType) {
            return cachedCounters.getOrDefault(entityType, 0);
        }

        public CountPerMod getCountPerMod(String mod) {
            return countPerMod.get(mod);
        }

        // This function is only called if ModSetup.customnpcs is true
        public int getNpcCount(Entity entity) {
            if (CustomNPCSupport.hasNPCInterface(entity) && entity.getPersistentData().contains("InControlNatSpawnName") && entity.getPersistentData().contains("InControlNatSpawnTab")){
                CNPCMob mob = new CNPCMob(entity.getPersistentData().getInt("InControlNatSpawnTab"), entity.getPersistentData().getString("InControlNatSpawnName"));
                return cachedNpcCounters.getOrDefault(mob, 0);
            }
            return getCount(entity.getType());
        }


//        public void registerSpawn(EntityType entityType) {
//            cachedCounters.put(entityType, cachedCounters.getOrDefault(entityType, 0) + 1);
//        }
//
//        public void registerDespawn(EntityType entityType) {
//            Integer cnt = cachedCounters.getOrDefault(entityType, 0);
//            if (cnt > 0) {
//                cachedCounters.put(entityType, cnt-1);
//            }
//        }

        // Per-player, per-radius counter storage
        // Stores category totals, a per-EntityType map, a per-mod map and a per-npc map.
        private static class PlayerLocalCounts {
            private int hostile;
            private int passive;
            private int neutral;
            private final Map<EntityType, Integer> perType = new HashMap<>();
            private final Map<String, CountPerMod> perMod = new HashMap<>();
            private final Map<CNPCMob, Integer> perNpc = new HashMap<>();
        }
    }

}
