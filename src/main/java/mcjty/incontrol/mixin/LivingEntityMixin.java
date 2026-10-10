package mcjty.incontrol.mixin;

import mcjty.incontrol.InControl;
import mcjty.incontrol.platform.RuleEvents.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow public abstract Player getLastHurtByPlayer();
    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;handleKillingBlow()V", shift = At.Shift.AFTER))
    private void incontrol$death(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!entity.level().isClientSide()) InControl.HANDLERS.onEntityKilled(new LivingDeathEvent(entity, source));
    }
    @Redirect(method = "dropExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"))
    private void incontrol$experience(ServerLevel level, Vec3 pos, int amount) {
        LivingExperienceDropEvent event = new LivingExperienceDropEvent((LivingEntity) (Object) this, getLastHurtByPlayer(), amount);
        InControl.HANDLERS.onLivingExperienceDrop(event);
        if (!event.isCanceled() && event.getDroppedExperience() > 0) ExperienceOrb.award(level, pos, event.getDroppedExperience());
    }
}
