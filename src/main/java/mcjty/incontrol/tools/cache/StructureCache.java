package mcjty.incontrol.tools.cache;

import it.unimi.dsi.fastutil.longs.LongSet;
import mcjty.incontrol.tools.varia.Tools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;

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

    public boolean isInStructure(LevelAccessor world, String structure, BlockPos pos) {
        ResourceKey<Level> dimension = Tools.getDimensionKey(world);
        ChunkPos cp = new ChunkPos(pos);
        long cplong = ChunkPos.asLong(cp.x, cp.z);
        StructureCacheEntry entry = new StructureCacheEntry(structure, dimension, cplong);
        if (structureCache.containsKey(entry)) {
            return structureCache.get(entry);
        }

        ServerLevel sw = Tools.getServerWorld(world);
        ChunkAccess chunk = sw.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_REFERENCES, false);
        if (chunk == null) {
            return false;
        }
        Map<Structure, LongSet> references = chunk.getAllReferences();
        for (Map.Entry<Structure, LongSet> e : references.entrySet()) {
            LongSet longs = e.getValue();
            if (!longs.isEmpty()) {
                Structure struct = e.getKey();
                ResourceLocation key = sw.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY).getKey(struct);
                structureCache.put(new StructureCacheEntry(key.toString(), dimension, cplong), true);
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
