package mcjty.incontrol.events;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static mcjty.incontrol.ForgeEventHandlers.debug;

public class InControlLoodModifier extends LootModifier {

    public static final MapCodec<InControlLoodModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(l -> l.conditions)
    ).apply(instance, InControlLoodModifier::new));

    public InControlLoodModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ServerLevel level = context.getLevel();
        int looting = 0;
        if (context.getParamOrNull(LootContextParams.ATTACKING_ENTITY) instanceof LivingEntity attacker) {
            Optional<Holder.Reference<Enchantment>> holder = level.registryAccess().holder(Enchantments.LOOTING);
            if (holder.isPresent()) {
                looting = EnchantmentHelper.getTagEnchantmentLevel(holder.get(), attacker.getWeaponItem());
            }
        }

        int i = 0;
        for (LootRule rule : RulesManager.getFilteredLootRules(level)) {
            if (rule.match(context)) {
                if (debug) {
                    Entity entity = context.getParam(LootContextParams.THIS_ENTITY);
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

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
