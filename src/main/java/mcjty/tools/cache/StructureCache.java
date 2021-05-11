package mcjty.tools.cache;

import it.unimi.dsi.fastutil.longs.LongSet;
import mcjty.tools.varia.Tools;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;

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

    public boolean isInStructure(IWorld world, String structure, BlockPos pos) {
        RegistryKey<World> dimension = Tools.getDimensionKey(world);
        ChunkPos cp = new ChunkPos(pos);
        long cplong = ChunkPos.asLong(cp.x, cp.z);
        StructureCacheEntry entry = new StructureCacheEntry(structure, dimension, cplong);
        if (structureCache.containsKey(entry)) {
            return structureCache.get(entry);
        }

        ServerWorld sw = Tools.getServerWorld(world);
        IChunk chunk = sw.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_REFERENCES, false);
        if (chunk == null) {
            return false;
        }
        Map<Structure<?>, LongSet> references = chunk.getStructureReferences();
        for (Map.Entry<Structure<?>, LongSet> e : references.entrySet()) {
            LongSet longs = e.getValue();
            if (!longs.isEmpty()) {
                structureCache.put(new StructureCacheEntry(e.getKey().getRegistryName().toString(), dimension, cplong), true);
            }
        }

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
