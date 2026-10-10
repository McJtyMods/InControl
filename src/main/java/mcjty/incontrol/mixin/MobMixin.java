package mcjty.incontrol.mixin;

import mcjty.incontrol.InControl;
import mcjty.incontrol.platform.MobState;
import mcjty.incontrol.platform.RuleEvents.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin implements MobState {
    @Unique private MobSpawnType incontrol$spawnType;
    @Unique private boolean incontrol$spawnCancelled;
    public MobSpawnType incontrol$getSpawnType() { return incontrol$spawnType; }
    public void incontrol$setSpawnType(MobSpawnType type) { incontrol$spawnType = type; }
    public boolean incontrol$isSpawnCancelled() { return incontrol$spawnCancelled; }
    public void incontrol$setSpawnCancelled(boolean canceled) { incontrol$spawnCancelled = canceled; }

    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void incontrol$despawn(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (mob.level().isClientSide()) return;
        MobDespawnEvent event = new MobDespawnEvent(mob);
        InControl.HANDLERS.onCheckDespawn(event);
        if (event.getResult() == MobDespawnEvent.Result.ALLOW) { mob.discard(); ci.cancel(); }
        else if (event.getResult() == MobDespawnEvent.Result.DENY) { mob.setNoActionTime(0); ci.cancel(); }
    }
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void incontrol$saveType(CompoundTag tag, CallbackInfo ci) {
        if (incontrol$spawnType != null) tag.putString("InControlSpawnType", incontrol$spawnType.name());
    }
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void incontrol$loadType(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("InControlSpawnType")) {
            try { incontrol$spawnType = MobSpawnType.valueOf(tag.getString("InControlSpawnType")); }
            catch (IllegalArgumentException ignored) { incontrol$spawnType = null; }
        }
    }
}
