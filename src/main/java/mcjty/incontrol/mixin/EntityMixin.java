package mcjty.incontrol.mixin;

import mcjty.incontrol.platform.EntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityData {
    @Unique private CompoundTag incontrol$data = new CompoundTag();
    public CompoundTag incontrol$getData() { return incontrol$data; }
    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void incontrol$save(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (!incontrol$data.isEmpty()) tag.put("InControlData", incontrol$data.copy());
    }
    @Inject(method = "load", at = @At("TAIL"))
    private void incontrol$load(CompoundTag tag, CallbackInfo ci) {
        incontrol$data = tag.getCompound("InControlData").copy();
    }
}
