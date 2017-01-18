package mcjty.incontrol.cache;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Remember where structures are
 */
public class StructureCache {

    public static final StructureCache CACHE = new StructureCache();

    private final Map<StructureCacheEntry, Boolean> structureCache = new HashMap<>();

    public void clean() {
        structureCache.clear();
    }

    public boolean isInStructure(World world, String structure, BlockPos pos) {
        int dimension = world.provider.getDimension();
        ChunkPos cp = new ChunkPos(pos);
        long cplong = ChunkPos.asLong(cp.chunkXPos, cp.chunkZPos);
        StructureCacheEntry entry = new StructureCacheEntry(structure, dimension, cplong);
        if (structureCache.containsKey(entry)) {
            return structureCache.get(entry);
        }

        MapGenStructureData data = (MapGenStructureData) world.getPerWorldStorage().getOrLoadData(MapGenStructureData.class, structure);
        if (data == null) {
            return false;
        }

        Set<Long> longs = parseStructureData(data);
        for (Long l : longs) {
            structureCache.put(new StructureCacheEntry(structure, dimension, l), true);
        }
        if (structureCache.containsKey(entry)) {
            return true;
        } else {
            structureCache.put(entry, false);
            return false;
        }
    }

    private static Set<Long> parseStructureData(MapGenStructureData data) {
        Set<Long> chunks = new HashSet<>();
        NBTTagCompound nbttagcompound = data.getTagCompound();

        for (String s : nbttagcompound.getKeySet()) {
            NBTBase nbtbase = nbttagcompound.getTag(s);

            if (nbtbase.getId() == 10) {
                NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbtbase;

                if (nbttagcompound1.hasKey("ChunkX") && nbttagcompound1.hasKey("ChunkZ")) {
                    int i = nbttagcompound1.getInteger("ChunkX");
                    int j = nbttagcompound1.getInteger("ChunkZ");
                    chunks.add(ChunkPos.asLong(i, j));
                }
            }
        }
        return chunks;
    }
}
