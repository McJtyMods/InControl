package mcjty.incontrol.mixin;

import mcjty.incontrol.InControl;
import mcjty.incontrol.platform.MobState;
import mcjty.incontrol.platform.RuleEvents.EntityJoinLevelEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentEntitySectionManager.class)
public abstract class EntitySectionManagerMixin {
    @Inject(method = "addEntity(Lnet/minecraft/world/level/entity/EntityAccess;Z)Z", at = @At("HEAD"), cancellable = true)
    private void incontrol$join(EntityAccess access, boolean existing, CallbackInfoReturnable<Boolean> cir) {
        if (!(access instanceof Entity entity) || entity.level().isClientSide()) return;
        if (entity instanceof MobState mob && mob.incontrol$isSpawnCancelled()) { cir.setReturnValue(false); return; }
        EntityJoinLevelEvent event = new EntityJoinLevelEvent(entity, entity.level());
        InControl.HANDLERS.onEntityJoinWorld(event);
        if (event.isCanceled()) cir.setReturnValue(false);
    }
}
