package mcjty.incontrol.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mcjty.incontrol.InControl;
import mcjty.incontrol.platform.RuleEvents.BlockEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;placeBlock(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean incontrol$place(BlockItem item, BlockPlaceContext context, BlockState state, Operation<Boolean> original) {
        if (context.getLevel().isClientSide() || context.getPlayer() == null) return original.call(item, context, state);
        var level = context.getLevel();
        var pos = context.getClickedPos();
        BlockState before = level.getBlockState(pos);
        var blockEntity = level.getBlockEntity(pos);
        var beforeData = blockEntity == null ? null : blockEntity.saveWithFullMetadata(level.registryAccess());
        if (!original.call(item, context, state)) return false;
        BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(level, pos, context.getPlayer());
        InControl.HANDLERS.onBlockPaceEvent(event);
        if (!event.isCanceled()) return true;
        level.setBlock(pos, before, 3);
        var restored = level.getBlockEntity(pos);
        if (restored != null && beforeData != null) restored.loadWithComponents(beforeData, level.registryAccess());
        return false;
    }
}
