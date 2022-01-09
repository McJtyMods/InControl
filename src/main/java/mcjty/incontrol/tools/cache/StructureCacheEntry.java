package mcjty.incontrol.tools.cache;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class StructureCacheEntry {
    @Nonnull private final String structure;
    private final ResourceKey<Level> dimension;
    private final long chunkpos;

    public StructureCacheEntry(@Nonnull String structure, ResourceKey<Level> dimension, long chunkpos) {
        this.structure = structure;
        this.dimension = dimension;
        this.chunkpos = chunkpos;
    }

    @Nonnull
    public String getStructure() {
        return structure;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public long getChunkpos() {
        return chunkpos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StructureCacheEntry that = (StructureCacheEntry) o;

        if (dimension != that.dimension) return false;
        if (chunkpos != that.chunkpos) return false;
        if (!structure.equals(that.structure)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = structure.hashCode();
        result = 31 * result + dimension.hashCode();
        result = 31 * result + (int) (chunkpos ^ (chunkpos >>> 32));
        return result;
    }
}
