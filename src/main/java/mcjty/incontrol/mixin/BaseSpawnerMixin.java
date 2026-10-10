package mcjty.incontrol.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mcjty.incontrol.platform.SpawnHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.SpawnData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.core.BlockPos;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin {
    // Defer the first half of the vanilla short-circuit expression, allowing position
    // rules to override the combined spawn-rules/obstruction result exactly once.
    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkSpawnRules(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;)Z"))
    private boolean incontrol$deferRules(Mob mob, LevelAccessor level, MobSpawnType type, Operation<Boolean> original) {
        return true;
    }
    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkSpawnObstruction(Lnet/minecraft/world/level/LevelReader;)Z"))
    private boolean incontrol$position(Mob mob, net.minecraft.world.level.LevelReader reader, Operation<Boolean> original, @Local SpawnData data) {
        ServerLevel level = (ServerLevel) reader;
        boolean vanilla = (data.getCustomSpawnRules().isPresent() || mob.checkSpawnRules(level, MobSpawnType.SPAWNER)) && original.call(mob, reader);
        return SpawnHooks.position(mob, level, MobSpawnType.SPAWNER, vanilla);
    }
    @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/SpawnData;getEquipment()Ljava/util/Optional;"))
    private void incontrol$nbtFinalization(ServerLevel level, BlockPos pos, CallbackInfo ci, @Local Mob mob, @Local SpawnData data) {
        if (data.getEntityToSpawn().size() != 1 || !data.getEntityToSpawn().contains("id", 8)) {
            // NeoForge also fires the event for NBT-defined mobs, while retaining their
            // supplied data instead of running vanilla finalization.
            SpawnHooks.finalize(mob, level, MobSpawnType.SPAWNER, null, () -> null);
        }
    }
}
