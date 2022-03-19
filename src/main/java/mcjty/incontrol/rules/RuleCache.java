package mcjty.incontrol.rules;

import mcjty.incontrol.tools.varia.Tools;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.HashMap;
import java.util.Map;

public class RuleCache {

    private Map<RegistryKey<World>, CachePerWorld> caches = new HashMap<>();

    public void reset(IWorld world) {
        RegistryKey<World> key = Tools.getDimensionKey(world);
        CachePerWorld cache = caches.get(key);
        if (cache != null) {
            cache.reset();
        }
    }

    public int getValidSpawnChunks(IWorld world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getValidSpawnChunks(world);
    }

    public int getValidPlayers(IWorld world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getValidPlayers(world);
    }

    public int getCountPassive(IWorld world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountPassive(world);
    }

    public int getCountHostile(IWorld world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountHostile(world);
    }

    public int getCountNeutral(IWorld world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountNeutral(world);
    }


    public int getCount(IWorld world, EntityType entityType) {
        CachePerWorld cache = getOrCreateCache(world);
        int count = cache.getCount(world, entityType);
        return count;
    }

    public int getCountPerMod(IWorld world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(world, mod);
        return countPerMod == null ? 0 : countPerMod.total;
    }

    public int getCountPerModHostile(IWorld world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(world, mod);
        return countPerMod == null ? 0 : countPerMod.hostile;
    }

    public int getCountPerModPassive(IWorld world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(world, mod);
        return countPerMod == null ? 0 : countPerMod.passive;
    }

    public void registerSpawn(IWorld world, EntityType entityType) {
        CachePerWorld cache = getOrCreateCache(world);
        cache.registerSpawn(world, entityType);
    }

    public void registerDespawn(IWorld world, EntityType entityType) {
        CachePerWorld cache = getOrCreateCache(world);
        cache.registerDespawn(world, entityType);
    }

    private CachePerWorld getOrCreateCache(IWorld world) {
        RegistryKey<World> key = Tools.getDimensionKey(world);
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

    private class CachePerWorld {

        private Map<EntityType, Integer> cachedCounters = new HashMap<>();
        private Map<String, CountPerMod> countPerMod = new HashMap<>();
        private int countPassive = -1;
        private int countHostile = -1;
        private int countNeutral = -1;
        private int validSpawnChunks = -1;
        private int validPlayers = -1;
        private boolean countDone = false;

        public void reset() {
            cachedCounters.clear();
            countPerMod.clear();
            countPassive = -1;
            countHostile = -1;
            countNeutral = -1;
            validSpawnChunks = -1;
            validPlayers = -1;
            countDone = false;
        }

        public int getValidSpawnChunks(IWorld world) {
            if (validSpawnChunks == -1) {
                validSpawnChunks = countValidSpawnChunks(world);
            }
            return validSpawnChunks;
        }

        public int getValidPlayers(IWorld world) {
            if (validPlayers == -1) {
                validPlayers = countValidPlayers(world);
            }
            return validPlayers;
        }

        private int countValidPlayers(IWorld world) {
            int cnt = 0;
            for (PlayerEntity entityplayer : world.players()) {
                if (!entityplayer.isSpectator()) {
                    cnt++;
                }
            }
            return cnt;
        }

        private int countValidSpawnChunks(IWorld world) {
            ServerWorld sw = Tools.getServerWorld(world);
            return sw.getChunkSource().chunkMap.size();
        }

        public int getCountPassive(IWorld world) {
            count(world);
            return countPassive;
        }

        public int getCountHostile(IWorld world) {
            count(world);
            return countHostile;
        }

        public int getCountNeutral(IWorld world) {
            count(world);
            return countNeutral;
        }

        private void count(IWorld world) {
            if (countDone) {
                return;
            }
            countDone = true;
            cachedCounters.clear();
            countPerMod.clear();
            countPassive = 0;
            countHostile = 0;
            countNeutral = 0;

            ServerWorld sw = Tools.getServerWorld(world);

            sw.getEntities().forEach(entity -> {
                if (entity instanceof MobEntity) {
                    int cnt = cachedCounters.getOrDefault(entity.getType(), 0)+1;
                    cachedCounters.put(entity.getType(), cnt);

                    String mod = entity.getType().getRegistryName().getNamespace();
                    CountPerMod count = countPerMod.computeIfAbsent(mod, s -> new CountPerMod());
                    count.total++;

                    if (entity instanceof IMob) {
                        count.hostile++;
                        countHostile++;
                    } else if (entity instanceof AnimalEntity) {
                        count.passive++;
                        countPassive++;
                    } else {
                        count.neutral++;
                        countNeutral++;
                    }
                }
            });
        }

        public int getCount(IWorld world, EntityType entityType) {
            count(world);
            return cachedCounters.getOrDefault(entityType, 0);
        }

        public CountPerMod getCountPerMod(IWorld world, String mod) {
            count(world);
            return countPerMod.get(mod);
        }

        public void registerSpawn(IWorld world, EntityType entityType) {
            count(world);
            cachedCounters.put(entityType, cachedCounters.getOrDefault(entityType, 0) + 1);
        }

        public void registerDespawn(IWorld world, EntityType entityType) {
            count(world);
            Integer cnt = cachedCounters.getOrDefault(entityType, 0);
            if (cnt > 0) {
                cachedCounters.put(entityType, cnt-1);
            }
        }
    }

}
