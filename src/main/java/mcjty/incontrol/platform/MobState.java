package mcjty.incontrol.platform;

import net.minecraft.world.entity.MobSpawnType;

public interface MobState {
    MobSpawnType incontrol$getSpawnType();
    void incontrol$setSpawnType(MobSpawnType type);
    boolean incontrol$isSpawnCancelled();
    void incontrol$setSpawnCancelled(boolean canceled);
}
