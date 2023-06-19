package mcjty.incontrol;

import mcjty.incontrol.commands.ModCommands;
import mcjty.incontrol.data.DataStorage;
import mcjty.incontrol.data.Statistics;
import mcjty.incontrol.rules.*;
import mcjty.incontrol.spawner.SpawnerSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class ForgeEventHandlers {

    public static boolean debug = false;

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        int i = 0;
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            return;
        }
        if (event.getLevel().isClientSide) {
            return;
        }
        for (SpawnRule rule : RulesManager.getFilteredRules(event.getLevel())) {
            if (rule.isOnJoin() && rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Join Rule " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getEntity().blockPosition().getY());
                }
                if (result != Event.Result.DENY) {
                    Statistics.addSpawnStat(i, false);
                    rule.action(event);
                } else {
                    Statistics.addSpawnStat(i, true);
                    event.setCanceled(true);
                }
                if (!rule.isDoContinue()) {
                    return;
                }
            }
            i++;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinWorldLast(EntityJoinLevelEvent event) {
        // We register spawns in a high priority event so that we take things that other mods
        // do into account
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity) {
            if (!(event.getEntity() instanceof Player)) {
                InControl.setup.cache.registerSpawn(event.getLevel(), event.getEntity().getType());
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.level.isClientSide) {
            // For every world tick we reset the cache
            InControl.setup.cache.reset(event.level);

            if (!event.level.players().isEmpty()) {
                // If a world has players we do mob spawning in it
                SpawnerSystem.checkRules(event);
            }

            if (event.level.dimension().equals(Level.OVERWORLD)) {
                DataStorage.getData(event.level).tick(event.level);
            }
        }
    }

    @SubscribeEvent
    public void onEntitySpecialSpawnEvent(MobSpawnEvent.FinalizeSpawn event) {
        int i = 0;
        ServerLevel world = (ServerLevel) event.getEntity().getCommandSenderWorld();
        for (SpecialRule rule : RulesManager.getFilteredSpecialRules(world)) {
            if (rule.match(event)) {
                SpecialRule.SpecialResult result = rule.getResult();
                if (debug) {
                    Biome biome = event.getLevel().getBiome(new BlockPos((int) event.getX(), (int) event.getY(), (int) event.getZ())).value();
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Special " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getY()
                            + " biome: " + ForgeRegistries.BIOMES.getKey(biome));
                }

                switch (result) {
                    case BEFORE -> {
                        event.setCanceled(false);
                    }
                    case AFTER -> {
                        event.getEntity().finalizeSpawn(world, world.getCurrentDifficultyAt(event.getEntity().blockPosition()),
                                MobSpawnType.NATURAL, null, null);
                        event.setCanceled(true);
                    }
                    case REPLACE -> {
                        event.setCanceled(true);
                    }
                }
                rule.action(event);

                if (!rule.isDoContinue()) {
                    return;
                }
            }
            i++;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawnEvent(MobSpawnEvent.FinalizeSpawn event) {
        int i = 0;
        for (SpawnRule rule : RulesManager.getFilteredRules(event.getEntity().getCommandSenderWorld())) {
            if (rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    Biome biome = event.getLevel().getBiome(new BlockPos((int) event.getX(), (int) event.getY(), (int) event.getZ())).value();
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Rule " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getY()
                            + " biome: " + ForgeRegistries.BIOMES.getKey(biome));
                }
                if (result != null) {
                    event.setResult(result);
                }
                if (result != Event.Result.DENY) {
                    Statistics.addSpawnStat(i, false);
                    rule.action(event);
                } else {
                    Statistics.addSpawnStat(i, true);
                }
                if (!rule.isDoContinue()) {
                    return;
                }
            }
            i++;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSummonAidEvent(ZombieEvent.SummonAidEvent event) {
        int i = 0;
        for (SummonAidRule rule : RulesManager.getFilteredSummonAidRules(event.getLevel())) {
            if (rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    Biome biome = event.getLevel().getBiome(new BlockPos(event.getX(), event.getY(), event.getZ())).value();
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "SummonAid " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getY()
                            + " biome: " + ForgeRegistries.BIOMES.getKey(biome));
                }
                event.setResult(result);
                if (result != Event.Result.DENY) {
                    rule.action(event);
                }
                return;
            }
            i++;
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        int i = 0;
        for (ExperienceRule rule : RulesManager.getFilteredExperienceRuiles(event.getEntity().level())) {
            if (rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Experience Rule " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getEntity().blockPosition().getY());
                }
                if (result != Event.Result.DENY) {
                    int newxp = rule.modifyXp(event.getDroppedExperience());
                    event.setDroppedExperience(newxp);
                } else {
                    event.setCanceled(true);
                }
                return;
            }
            i++;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDrops(LivingDropsEvent event) {
        Level world = event.getEntity().getCommandSenderWorld();
        int i = 0;
        for (LootRule rule : RulesManager.getFilteredLootRules(world)) {
            if (rule.match(event)) {
                if (debug) {
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Loot " + i + ": "
                            + " entity: " + event.getEntity().getName());
                }

                if (rule.isRemoveAll()) {
                    event.getDrops().clear();
                } else {
                    List<ItemEntity> toRemove = null;
                    for (Predicate<ItemStack> stackTest : rule.getToRemoveItems()) {
                        Collection<ItemEntity> drops = event.getDrops();
                        for (ItemEntity drop : drops) {
                            ItemStack stack = drop.getItem();
                            if (stackTest.test(stack)) {
                                if (toRemove == null) {
                                    toRemove = new ArrayList<>();
                                };
                                toRemove.add(drop);
                            }
                        }
                    }
                    if (toRemove != null) {
                        Collection<ItemEntity> drops = event.getDrops();
                        for (ItemEntity entity : toRemove) {
                            drops.remove(entity);
                        }

                    }
                }

                for (Pair<ItemStack, Function<Integer, Integer>> pair : rule.getToAddItems()) {
                    ItemStack item = pair.getLeft();
                    int fortune = event.getLootingLevel();
                    int amount = pair.getValue().apply(fortune);
                    BlockPos pos = event.getEntity().blockPosition();
                    while (amount > item.getMaxStackSize()) {
                        ItemStack copy = item.copy();
                        copy.setCount(item.getMaxStackSize());
                        amount -= item.getMaxStackSize();
                        event.getDrops().add(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(),
                                copy));
                    }
                    if (amount > 0) {
                        ItemStack copy = item.copy();
                        copy.setCount(amount);
                        event.getDrops().add(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(),
                                copy));
                    }
                }
            }
            i++;
        }
    }

}
