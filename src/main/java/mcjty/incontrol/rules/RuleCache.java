package mcjty.incontrol.rules;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class RuleCache {

    private Map<Integer, CachePerWorld> caches = new HashMap<>();

    public void reset(World world) {
        CachePerWorld cache = caches.get(world.provider.getDimension());
        if (cache != null) {
            cache.reset();
        }
        System.out.println("[" + world.provider.getDimension() + "]RESET");
    }

    public int getCount(World world, Class<? extends Entity> entityType) {
        CachePerWorld cache = getOrCreateCache(world);
        int count = cache.getCount(world, entityType);
        System.out.println("    [" + world.provider.getDimension() + "] count " + entityType.getSimpleName() + " = " + count);
        return count;
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



    private class CachePerWorld {

        private Map<Class, Integer> cachedCounters = new HashMap<>();
        private boolean countDone = false;

        public void reset() {
            cachedCounters.clear();
            countDone = false;
        }

        private void count(World world) {
            if (countDone) {
                return;
            }
            countDone = true;
            cachedCounters.clear();

            for (Entity entity : world.loadedEntityList) {
                if (entity instanceof EntityLiving) {
                    if (!((EntityLiving) entity).isNoDespawnRequired()) {
                        int cnt = 0;
                        if (cachedCounters.containsKey(entity.getClass())) {
                            cnt = cachedCounters.get(entity.getClass());
                        }
                        cnt++;
                        cachedCounters.put(entity.getClass(), cnt);
                    }
                }
            }
        }

        public int getCount(World world, Class<? extends Entity> entityType) {
            count(world);
            return cachedCounters.getOrDefault(entityType, 0);
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
