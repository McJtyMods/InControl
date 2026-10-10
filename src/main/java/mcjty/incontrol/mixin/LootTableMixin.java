package mcjty.incontrol.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mcjty.incontrol.events.InControlLootModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.function.Consumer;

@Mixin(LootTable.class)
public abstract class LootTableMixin {
    // All public collection/consumer overloads reach one of these calls. Nested raw tables
    // remain raw, so additions and removals run once per generated top-level loot result.
    @WrapOperation(method = {
            "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V",
            "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V"))
    private void incontrol$loot(LootTable table, LootContext context, Consumer<ItemStack> output, Operation<Void> original) {
        ObjectArrayList<ItemStack> loot = new ObjectArrayList<>();
        original.call(table, context, (Consumer<ItemStack>) loot::add);
        InControlLootModifier.apply(loot, context).forEach(output);
    }
    @WrapOperation(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootParams;Ljava/util/function/Consumer;)V"))
    private void incontrol$lootParams(LootTable table, net.minecraft.world.level.storage.loot.LootParams params, Consumer<ItemStack> output, Operation<Void> original) {
        // Route through the context overload above, preserving the table's random sequence.
        table.getRandomItems(params, 0L, output);
    }
}
