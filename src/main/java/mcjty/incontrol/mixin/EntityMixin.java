package mcjty.incontrol.mixin;

import mcjty.incontrol.platform.EntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityData {
    @Unique private CompoundTag incontrol$data = new CompoundTag();
    public CompoundTag incontrol$getData() { return incontrol$data; }
    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void incontrol$save(ValueOutput output, CallbackInfo ci) {
        if (!incontrol$data.isEmpty()) output.store("InControlData", CompoundTag.CODEC, incontrol$data.copy());
    }
    @Inject(method = "load", at = @At("TAIL"))
    private void incontrol$load(ValueInput input, CallbackInfo ci) {
        incontrol$data = input.read("InControlData", CompoundTag.CODEC).orElseGet(CompoundTag::new).copy();
    }
}
