package mcjty.incontrol.cache;

import javax.annotation.Nonnull;

public class StructureCacheEntry {
    @Nonnull private final String structure;
    private final int dimension;
    private final long chunkpos;

    public StructureCacheEntry(@Nonnull String structure, int dimension, long chunkpos) {
        this.structure = structure;
        this.dimension = dimension;
        this.chunkpos = chunkpos;
    }

    @Nonnull
    public String getStructure() {
        return structure;
    }

    public int getDimension() {
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
        result = 31 * result + dimension;
        result = 31 * result + (int) (chunkpos ^ (chunkpos >>> 32));
        return result;
    }
}
