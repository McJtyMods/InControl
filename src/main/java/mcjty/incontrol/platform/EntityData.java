package mcjty.incontrol.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public interface EntityData {
    CompoundTag incontrol$getData();
    static CompoundTag get(Entity entity) { return ((EntityData) entity).incontrol$getData(); }
}
