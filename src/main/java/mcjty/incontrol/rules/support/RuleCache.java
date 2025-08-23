package mcjty.incontrol.rules.support;

import mcjty.incontrol.compat.CustomNPCSupport;
import mcjty.incontrol.mob.CNPCMob;
import mcjty.incontrol.setup.Config;
import mcjty.incontrol.setup.ModSetup;
import mcjty.incontrol.tools.varia.Tools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;

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

        private final Map<EntityType, Integer> cachedCounters = new HashMap<>();
        private final Map<CNPCMob, Integer> cachedNpcCounters = new HashMap<>();
        private final Map<String, CountPerMod> countPerMod = new HashMap<>();
        private int countPassive = -1;
        private int countHostile = -1;
        private int countNeutral = -1;
        private int validSpawnChunks = -1;
        private int validPlayers = -1;
        private int dirtyCounter = 0;

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

                String mod = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).getNamespace();
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

                String mod = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).getNamespace();
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
            }
            return true;
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
    }

}
