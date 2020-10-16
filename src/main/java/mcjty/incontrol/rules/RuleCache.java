package mcjty.incontrol.rules;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

import java.util.HashMap;
import java.util.Map;

public class RuleCache {

    private Map<DimensionType, CachePerWorld> caches = new HashMap<>();

    public void reset(World world) {
        CachePerWorld cache = caches.get(world.getDimension().getType());
        if (cache != null) {
            cache.reset();
        }
    }

    public int getValidSpawnChunks(World world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getValidSpawnChunks(world);
    }

    public int getValidPlayers(World world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getValidPlayers(world);
    }

    public int getCountPassive(World world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountPassive(world);
    }

    public int getCountHostile(World world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountHostile(world);
    }


    public int getCount(World world, EntityType entityType) {
        CachePerWorld cache = getOrCreateCache(world);
        int count = cache.getCount(world, entityType);
        return count;
    }

    public int getCountPerMod(World world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(world, mod);
        return countPerMod == null ? 0 : countPerMod.total;
    }

    public int getCountPerModHostile(World world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(world, mod);
        return countPerMod == null ? 0 : countPerMod.hostile;
    }

    public int getCountPerModPassive(World world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(world, mod);
        return countPerMod == null ? 0 : countPerMod.passive;
    }

    public void registerSpawn(World world, EntityType entityType) {
        CachePerWorld cache = getOrCreateCache(world);
        cache.registerSpawn(world, entityType);
    }

    public void registerDespawn(World world, EntityType entityType) {
        CachePerWorld cache = getOrCreateCache(world);
        cache.registerDespawn(world, entityType);
    }

    private CachePerWorld getOrCreateCache(World world) {
        CachePerWorld cache = caches.get(world.getDimension().getType());
        if (cache == null) {
            cache = new CachePerWorld();
            caches.put(world.getDimension().getType(), cache);
        }
        return cache;
    }


    private static class CountPerMod {
        private int hostile;
        private int passive;
        private int total;
    }

    private class CachePerWorld {

        private Map<EntityType, Integer> cachedCounters = new HashMap<>();
        private Map<String, CountPerMod> countPerMod = new HashMap<>();
        private int countPassive = -1;
        private int countHostile = -1;
        private int validSpawnChunks = -1;
        private int validPlayers = -1;
        private boolean countDone = false;

        public void reset() {
            cachedCounters.clear();
            countPerMod.clear();
            countPassive = -1;
            countHostile = -1;
            validSpawnChunks = -1;
            validPlayers = -1;
            countDone = false;
        }

        public int getValidSpawnChunks(World world) {
            if (validSpawnChunks == -1) {
                validSpawnChunks = countValidSpawnChunks(world);
            }
            return validSpawnChunks;
        }

        public int getValidPlayers(World world) {
            if (validPlayers == -1) {
                validPlayers = countValidPlayers(world);
            }
            return validPlayers;
        }

        private int countValidPlayers(World world) {
            int cnt = 0;
            for (PlayerEntity entityplayer : world.getPlayers()) {
                if (!entityplayer.isSpectator()) {
                    cnt++;
                }
            }
            return cnt;
        }

        private int countValidSpawnChunks(World world) {
            return ((ServerWorld)world).getChunkProvider().chunkManager.getLoadedChunkCount();
        }

        public int getCountPassive(World world) {
            count(world);
            return countPassive;
        }

        public int getCountHostile(World world) {
            count(world);
            return countHostile;
        }

        private void count(World world) {
            if (countDone) {
                return;
            }
            countDone = true;
            cachedCounters.clear();
            countPerMod.clear();
            countPassive = 0;
            countHostile = 0;

            ((ServerWorld)world).getEntities().forEach(entity -> {
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
                    }
                }
            });
        }

        public int getCount(World world, EntityType entityType) {
            count(world);
            return cachedCounters.getOrDefault(entityType, 0);
        }

        public CountPerMod getCountPerMod(World world, String mod) {
            count(world);
            return countPerMod.get(mod);
        }

        public void registerSpawn(World world, EntityType entityType) {
            count(world);
            cachedCounters.put(entityType, cachedCounters.getOrDefault(entityType, 0) + 1);
        }

        public void registerDespawn(World world, EntityType entityType) {
            count(world);
            Integer cnt = cachedCounters.getOrDefault(entityType, 0);
            if (cnt > 0) {
                cachedCounters.put(entityType, cnt-1);
            }
        }
    }
}
