package mcjty.incontrol.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mcjty.incontrol.InControl;
import mcjty.incontrol.platform.RuleEvents.PlayerInteractEvent;
import mcjty.incontrol.platform.RuleEvents.TriState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Unique private boolean incontrol$forceBlockUse;
    @WrapMethod(method = "useItemOn")
    private InteractionResult incontrol$rightClick(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit, Operation<InteractionResult> original) {
        if (player.isSpectator()) return original.call(player, level, stack, hand, hit);
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hit.getBlockPos(), hand);
        InControl.HANDLERS.onRightClickEvent(event);
        if (event.isCanceled()) return InteractionResult.FAIL;
        boolean previous = incontrol$forceBlockUse;
        incontrol$forceBlockUse = event.getUseBlock() == TriState.TRUE;
        try { return original.call(player, level, stack, hand, hit); }
        finally { incontrol$forceBlockUse = previous; }
    }
    @ModifyExpressionValue(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSecondaryUseActive()Z"))
    private boolean incontrol$allowBlockUse(boolean vanilla) { return !incontrol$forceBlockUse && vanilla; }
}
