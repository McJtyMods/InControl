package mcjty.incontrol.rules;

import mcjty.incontrol.InControl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RuleCache {

    private Map<Integer, CachePerWorld> caches = new HashMap<>();

    public void reset(World world) {
        CachePerWorld cache = caches.get(world.provider.getDimension());
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


    public int getCount(World world, Class<? extends Entity> entityType) {
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

    public void registerSpawn(World world, Class<? extends Entity> entityType) {
        CachePerWorld cache = getOrCreateCache(world);
        cache.registerSpawn(world, entityType);
    }

    public void registerDespawn(World world, Class<? extends Entity> entityType) {
        CachePerWorld cache = getOrCreateCache(world);
        cache.registerDespawn(world, entityType);
    }

    private CachePerWorld getOrCreateCache(World world) {
        CachePerWorld cache = caches.get(world.provider.getDimension());
        if (cache == null) {
            cache = new CachePerWorld();
            caches.put(world.provider.getDimension(), cache);
        }
        return cache;
    }


    private static class CountPerMod {
        private int hostile;
        private int passive;
        private int total;
    }

    private class CachePerWorld {

        private Map<Class, Integer> cachedCounters = new HashMap<>();
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
                validSpawnChunks = countValidSpawnChunks((WorldServer) world);
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
            for (EntityPlayer entityplayer : world.playerEntities) {
                if (!entityplayer.isSpectator()) {
                    cnt++;
                }
            }
            return cnt;
        }

        private int countValidSpawnChunks(WorldServer world) {
            Set<ChunkPos> eligibleChunksForSpawning = new HashSet<>();

            for (EntityPlayer entityplayer : world.playerEntities) {
                if (!entityplayer.isSpectator()) {
                    int chunkX = MathHelper.floor(entityplayer.posX / 16.0D);
                    int chunkZ = MathHelper.floor(entityplayer.posZ / 16.0D);

                    for (int dx = -8; dx <= 8; ++dx) {
                        for (int dz = -8; dz <= 8; ++dz) {
                            boolean flag = dx == -8 || dx == 8 || dz == -8 || dz == 8;
                            ChunkPos chunkpos = new ChunkPos(dx + chunkX, dz + chunkZ);

                            if (!eligibleChunksForSpawning.contains(chunkpos)) {

                                if (!flag && world.getWorldBorder().contains(chunkpos)) {
                                    PlayerChunkMapEntry entry = world.getPlayerChunkMap().getEntry(chunkpos.x, chunkpos.z);

                                    if (entry != null && entry.isSentToPlayers()) {
                                        eligibleChunksForSpawning.add(chunkpos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return eligibleChunksForSpawning.size();
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

            for (Entity entity : world.loadedEntityList) {
                if (entity instanceof EntityLiving) {
                    int cnt = cachedCounters.getOrDefault(entity.getClass(), 0)+1;
                    cachedCounters.put(entity.getClass(), cnt);

                    String mod = InControl.instance.modCache.getMod(entity);
                    CountPerMod count = countPerMod.computeIfAbsent(mod, s -> new CountPerMod());
                    count.total++;

                    if (entity instanceof IMob) {
                        count.hostile++;
                        countHostile++;
                    } else if (entity instanceof IAnimals) {
                        count.passive++;
                        countPassive++;
                    }
                }
            }
        }

        public int getCount(World world, Class<? extends Entity> entityType) {
            count(world);
            return cachedCounters.getOrDefault(entityType, 0);
        }

        public CountPerMod getCountPerMod(World world, String mod) {
            count(world);
            return countPerMod.get(mod);
        }

        public void registerSpawn(World world, Class<? extends Entity> entityType) {
            count(world);
            cachedCounters.put(entityType, cachedCounters.getOrDefault(entityType, 0) + 1);
        }

        public void registerDespawn(World world, Class<? extends Entity> entityType) {
            count(world);
            Integer cnt = cachedCounters.getOrDefault(entityType, 0);
            if (cnt > 0) {
                cachedCounters.put(entityType, cnt-1);
            }
        }
    }
}
