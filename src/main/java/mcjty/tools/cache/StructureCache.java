package mcjty.tools.cache;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.HashMap;
import java.util.Map;

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
        DimensionType dimension = world.getDimension().getType();
        ChunkPos cp = new ChunkPos(pos);
        long cplong = ChunkPos.asLong(cp.x, cp.z);
        StructureCacheEntry entry = new StructureCacheEntry(structure, dimension, cplong);
        if (structureCache.containsKey(entry)) {
            return structureCache.get(entry);
        }

        // @todo 1.15
//        MapGenStructureData data = (MapGenStructureData) world.getPerWorldStorage().getOrLoadData(MapGenStructureData.class, structure);
//        if (data == null) {
//            return false;
//        }
//
//        Set<Long> longs = parseStructureData(data);
//        for (Long l : longs) {
//            structureCache.put(new StructureCacheEntry(structure, dimension, l), true);
//        }
        if (structureCache.containsKey(entry)) {
            return true;
        } else {
            structureCache.put(entry, false);
            return false;
        }
    }

//    private static Set<Long> parseStructureData(MapGenStructureData data) {
//        Set<Long> chunks = new HashSet<>();
//        CompoundNBT nbttagcompound = data.getTagCompound();
//
//        for (String s : nbttagcompound.getKeySet()) {
//            NBTBase nbtbase = nbttagcompound.getTag(s);
//
//            if (nbtbase.getId() == 10) {
//                CompoundNBT nbttagcompound1 = (CompoundNBT) nbtbase;
//
//                if (nbttagcompound1.hasKey("ChunkX") && nbttagcompound1.hasKey("ChunkZ")) {
//                    int i = nbttagcompound1.getInteger("ChunkX");
//                    int j = nbttagcompound1.getInteger("ChunkZ");
//                    chunks.add(ChunkPos.asLong(i, j));
//                }
//            }
//        }
//        return chunks;
//    }
}
