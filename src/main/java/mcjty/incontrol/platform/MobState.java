package mcjty.incontrol.platform;

import net.minecraft.world.entity.EntitySpawnReason;

public interface MobState {
    EntitySpawnReason incontrol$getSpawnType();
    void incontrol$setSpawnType(EntitySpawnReason type);
    boolean incontrol$isSpawnCancelled();
    void incontrol$setSpawnCancelled(boolean canceled);
}
