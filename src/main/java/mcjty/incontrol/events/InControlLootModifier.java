package mcjty.incontrol.events;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mcjty.incontrol.InControl;
import mcjty.incontrol.rules.LootRule;
import mcjty.incontrol.rules.RulesManager;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static mcjty.incontrol.FabricEventHandlers.debug;

public final class InControlLootModifier {

    public static @NotNull ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Entity entity = context.getOptional(LootContextParams.THIS_ENTITY);
        if (entity == null) {
            return generatedLoot;
        }
        ServerLevel level = context.getLevel();
        int looting = 0;
        if (context.getOptional(LootContextParams.ATTACKING_ENTITY) instanceof LivingEntity attacker) {
            Optional<Holder.Reference<Enchantment>> holder = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).get(Enchantments.LOOTING);
            if (holder.isPresent()) {
                looting = EnchantmentHelper.getItemEnchantmentLevel(holder.get(), attacker.getWeaponItem());
            }
        }

        int i = 0;
        for (LootRule rule : RulesManager.getFilteredLootRules(level)) {
            if (rule.match(context)) {
                if (debug) {
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Loot " + i + ": "
                            + " entity: " + entity.getName());
                }
                if (rule.isRemoveAll()) {
                    generatedLoot.clear();
                } else {
                    for (Predicate<ItemStack> stackTest : rule.getToRemoveItems()) {
                        // Traverse backwards so we can safely remove items
                        for (int l = generatedLoot.size() - 1; l >= 0 ; l--) {
                            if (stackTest.test(generatedLoot.get(l))) {
                                generatedLoot.remove(l);
                            }
                        }
                    }
                }

                for (Pair<ItemStack, Function<Integer, Integer>> pair : rule.getToAddItems()) {
                    ItemStack item = pair.getLeft();
                    int amount = pair.getValue().apply(looting);
                    while (amount > item.getMaxStackSize()) {
                        ItemStack copy = item.copy();
                        copy.setCount(item.getMaxStackSize());
                        amount -= item.getMaxStackSize();
                        generatedLoot.add(copy);
                    }
                    if (amount > 0) {
                        ItemStack copy = item.copy();
                        copy.setCount(amount);
                        generatedLoot.add(copy);
                    }
                }
            }
            i++;
        }
        return generatedLoot;
    }

}
