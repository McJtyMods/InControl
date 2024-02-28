package mcjty.incontrol;

import mcjty.incontrol.areas.AreaSystem;
import mcjty.incontrol.commands.ModCommands;
import mcjty.incontrol.data.DataStorage;
import mcjty.incontrol.data.Statistics;
import mcjty.incontrol.events.EventsSystem;
import mcjty.incontrol.rules.*;
import mcjty.incontrol.rules.support.ICResult;
import mcjty.incontrol.rules.support.SpawnWhen;
import mcjty.incontrol.spawner.SpawnerSystem;
import mcjty.incontrol.tools.varia.Tools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.eventbus.api.Event;
import net.neoforged.neoforge.eventbus.api.EventPriority;
import net.neoforged.neoforge.eventbus.api.SubscribeEvent;
import net.neoforged.neoforge.fml.LogicalSide;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class ForgeEventHandlers {

    public static boolean debug = false;

    public static boolean loaded = false;

    @SubscribeEvent
    public void onLevelLoad(LevelEvent.Load event) {
        tryLoadRules();
    }

    private static void tryLoadRules() {
        if (!loaded) {
            loaded = true;
            AreaSystem.reloadRules();
            RulesManager.reloadRules();
            SpawnerSystem.reloadRules();
            EventsSystem.reloadRules();
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppedEvent event) {
        loaded = false;
    }

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
        for (SpawnRule rule : RulesManager.getFilteredRules(event.getLevel(), SpawnWhen.ONJOIN)) {
            if (rule.match(event)) {
                ICResult result = rule.getResult();
                if (debug) {
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Join Rule " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getEntity().blockPosition().getY());
                }
                switch (result) {
                    case ALLOW:
                    case DEFAULT:
                        Statistics.addSpawnStat(i, false);
                        rule.action(event);
                        break;
                    case DENY:
                        Statistics.addSpawnStat(i, true);
                        event.setCanceled(true);
                        break;
                    case DENY_WITH_ACTIONS:
                        Statistics.addSpawnStat(i, true);
                        rule.action(event);
                        event.setCanceled(true);
                        break;
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
            InControl.setup.cache.performCount(event.level);

            if (!event.level.players().isEmpty()) {
                // If a world has players we do mob spawning in it
                SpawnerSystem.checkRules(event);
            }

            if (event.level.dimension().equals(Level.OVERWORLD)) {
                DataStorage.getData(event.level).tick(event.level);
            }

            EventsSystem.onLevelTick(event);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawnEvent(MobSpawnEvent.FinalizeSpawn event) {
        int i = 0;
        for (SpawnRule rule : RulesManager.getFilteredRules(event.getEntity().getCommandSenderWorld(), SpawnWhen.FINALIZE)) {
            if (rule.match(event)) {
                ICResult result = rule.getResult();
                if (debug) {
                    Holder<Biome> biome = event.getLevel().getBiome(new BlockPos((int) event.getX(), (int) event.getY(), (int) event.getZ()));
                    String biomeId = Tools.getBiomeId(biome);
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Finalize Rule " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getY()
                            + " biome: " + biomeId);
                }
                switch (result) {
                    case ALLOW:
                        // We perform the actions but don't allow the default finalize to occur
                        rule.action(event);
                        event.setCanceled(true);
                        break;
                    case DEFAULT:
                        // We perform the actions and also allow the default finalize to occur
                        rule.action(event);
                        break;
                    case DENY:
                        // We cancel the event and also the spawn
                        event.setCanceled(true);
                        event.setSpawnCancelled(true);
                        break;
                    case DENY_WITH_ACTIONS:
                        // We cancel the event and also the spawn but allow actions
                        rule.action(event);
                        event.setCanceled(true);
                        event.setSpawnCancelled(true);
                        break;
                }
                if (!rule.isDoContinue()) {
                    return;
                }
            }
            i++;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        int i = 0;
        for (SpawnRule rule : RulesManager.getFilteredRules(event.getEntity().getCommandSenderWorld(), SpawnWhen.POSITION)) {
            if (rule.match(event)) {
                ICResult result = rule.getResult();
                if (debug) {
                    Holder<Biome> biome = event.getLevel().getBiome(new BlockPos((int) event.getX(), (int) event.getY(), (int) event.getZ()));
                    String biomeId = Tools.getBiomeId(biome);
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Position Rule " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getY()
                            + " biome: " + biomeId);
                }
                switch (result) {
                    case ALLOW:
                        event.setResult(Event.Result.ALLOW);
                        Statistics.addSpawnStat(i, false);
                        rule.action(event);
                        break;
                    case DEFAULT:
                        event.setResult(Event.Result.DEFAULT);
                        Statistics.addSpawnStat(i, false);
                        rule.action(event);
                        break;
                    case DENY:
                        event.setResult(Event.Result.DENY);
                        Statistics.addSpawnStat(i, true);
                        break;
                    case DENY_WITH_ACTIONS:
                        event.setResult(Event.Result.DENY);
                        Statistics.addSpawnStat(i, true);
                        rule.action(event);
                        break;
                }

                if (!rule.isDoContinue()) {
                    return;
                }
            }
            i++;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCheckDespawn(MobSpawnEvent.AllowDespawn event) {
        int i = 0;
        for (SpawnRule rule : RulesManager.getFilteredRules(event.getEntity().getCommandSenderWorld(), SpawnWhen.DESPAWN)) {
            if (rule.match(event)) {
                ICResult result = rule.getResult();
                if (debug) {
                    Holder<Biome> biome = event.getLevel().getBiome(new BlockPos((int) event.getX(), (int) event.getY(), (int) event.getZ()));
                    String biomeId = Tools.getBiomeId(biome);
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Despawn Rule " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getY()
                            + " biome: " + biomeId);
                }
                switch (result) {
                    case ALLOW:
                        event.setResult(Event.Result.ALLOW);
                        rule.action(event);
                        break;
                    case DEFAULT:
                        event.setResult(Event.Result.DEFAULT);
                        rule.action(event);
                        break;
                    case DENY:
                        event.setResult(Event.Result.DENY);
                        break;
                    case DENY_WITH_ACTIONS:
                        event.setResult(Event.Result.DENY);
                        rule.action(event);
                        break;
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
                    Holder<Biome> biome = event.getLevel().getBiome(new BlockPos((int) event.getX(), (int) event.getY(), (int) event.getZ()));
                    String biomeId = Tools.getBiomeId(biome);
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "SummonAid " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getY()
                            + " biome: " + biomeId);
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

    public static Map<Integer, Integer> tickCounters = new HashMap<>();

    @SubscribeEvent
    public void onRightClickEvent(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        int i = 0;
        for (RightClickRule rule : RulesManager.getFilteredRightClickRules(event.getLevel())) {
            if (rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    Holder<Biome> biome = event.getLevel().getBiome(event.getPos());
                    String biomeId = Tools.getBiomeId(biome);
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Rule " + i + ": "+ result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getPos().getY()
                            + " biome: " + biomeId);
                }
                rule.action(event);
                event.setUseBlock(result);
                if (result == Event.Result.DENY) {
                    event.setCanceled(true);
                }
                return;
            }
            i++;
        }
    }

    @SubscribeEvent
    public void onLeftClickEvent(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        int i = 0;
        for (LeftClickRule rule : RulesManager.getFilteredLeftClickRules(event.getLevel())) {
            if (rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    Holder<Biome> biome = event.getLevel().getBiome(event.getPos());
                    String biomeId = Tools.getBiomeId(biome);
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Rule " + i + ": "+ result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getPos().getY()
                            + " biome: " + biomeId);
                }
                rule.action(event);
                event.setUseBlock(result);
                if (result == Event.Result.DENY) {
                    event.setCanceled(true);
                }
                return;
            }
            i++;
        }
    }


    @SubscribeEvent
    public void onBlockPaceEvent(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        int i = 0;
        for (PlaceRule rule : RulesManager.getFilteredPlaceRules(event.getLevel())) {
            if (rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    Holder<Biome> biome = event.getLevel().getBiome(event.getPos());
                    String biomeId = Tools.getBiomeId(biome);
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Rule " + i + ": "+ result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getPos().getY()
                            + " biome: " + biomeId);
                }
                rule.action(event);
                if (result == Event.Result.DENY) {
                    event.setCanceled(true);
                }
                return;
            }
            i++;
        }
    }

    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        int i = 0;
        for (HarvestRule rule : RulesManager.getFilteredHarvestRules(event.getLevel())) {
            if (rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    Holder<Biome> biome = event.getLevel().getBiome(event.getPos());
                    String biomeId = Tools.getBiomeId(biome);
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Rule " + i + ": "+ result
                            + " entity: " + event.getPlayer().getName()
                            + " y: " + event.getPos().getY()
                            + " biome: " + biomeId);
                }
                rule.action(event);
                if (result == Event.Result.DENY) {
                    event.setCanceled(true);
                }
                return;
            }
            i++;
        }

        EventsSystem.onBlockBreak(event);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.side != LogicalSide.SERVER) {
            return;
        }

        int id = event.player.getId();
        if (!tickCounters.containsKey(id)) {
            tickCounters.put(id, 0);
        }
        int tickCounter = tickCounters.get(id) + 1;
        tickCounters.put(id, tickCounter);
        int i = 0;
        for (EffectRule rule : RulesManager.getFilteredEffectRules(event.player.getCommandSenderWorld())) {
            if (tickCounter % rule.getTimeout() == 0 && rule.match(event)) {
                if (debug) {
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Join Rule " + i
                            + " entity: " + event.player.getName()
                            + " y: " + event.player.blockPosition().getY());
                }
                rule.action(event);
                return;
            }
            i++;
        }
    }

    @SubscribeEvent
    public void onEntityKilled(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide) {
            EventsSystem.onEntityKilled(event);
        }
    }
}
