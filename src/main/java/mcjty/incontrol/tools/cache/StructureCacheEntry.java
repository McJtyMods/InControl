package mcjty.incontrol.tools.cache;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public record StructureCacheEntry(@Nonnull String structure,
                                  ResourceKey<Level> dimension,
                                  long chunkpos) {
    public StructureCacheEntry(@Nonnull String structure, ResourceKey<Level> dimension, long chunkpos) {
        this.structure = structure;
        this.dimension = dimension;
        this.chunkpos = chunkpos;
    }
}
