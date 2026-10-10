package mcjty.incontrol.mixin;

import mcjty.incontrol.platform.SpawnHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
    @Inject(method = "isValidPositionForMob", at = @At("RETURN"), cancellable = true)
    private static void incontrol$position(ServerLevel level, Mob mob, double distance, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(SpawnHooks.position(mob, level, MobSpawnType.NATURAL, cir.getReturnValue()));
    }
    @WrapOperation(method = "spawnMobsForChunkGeneration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkSpawnRules(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;)Z"))
    private static boolean incontrol$deferRules(Mob mob, LevelAccessor level, MobSpawnType type, Operation<Boolean> original) {
        return true;
    }
    @WrapOperation(method = "spawnMobsForChunkGeneration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkSpawnObstruction(Lnet/minecraft/world/level/LevelReader;)Z"))
    private static boolean incontrol$worldgen(Mob mob, LevelReader level, Operation<Boolean> original) {
        boolean vanilla = mob.checkSpawnRules((LevelAccessor) level, MobSpawnType.CHUNK_GENERATION) && original.call(mob, level);
        return SpawnHooks.position(mob, (ServerLevelAccessor) level, MobSpawnType.CHUNK_GENERATION, vanilla);
    }
}
