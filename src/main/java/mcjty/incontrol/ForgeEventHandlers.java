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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;

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

//        if (event.getEntity() instanceof Mob mob) {
//            if (mob instanceof Cow cow) {
//                cow.targetSelector.addGoal(1, (new HurtByTargetGoal(cow)).setAlertOthers(ZombifiedPiglin.class));
//                cow.targetSelector.addGoal(2, new NearestAttackableTargetGoal(cow, Player.class, true));
//                cow.goalSelector.addGoal(1, new MeleeAttackGoal(cow, (double)1.0F, false));
//            }
//        } else if (event.getEntity() instanceof Sheep sheep) {
//        }


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
                InControl.setup.cache.addMob(event.getLevel(), event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public void onEntityLeavesWorld(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }
        if (event.getLevel().isClientSide) {
            return;
        }
        InControl.setup.cache.removeMob(event.getLevel(), event.getEntity());
    }

    @SubscribeEvent
    public void onWorldTick(LevelTickEvent.Pre event) {
        if (!event.getLevel().isClientSide) {
            // For every world tick we reset the cache
            InControl.setup.cache.performCount(event.getLevel());

            if (!event.getLevel().players().isEmpty()) {
                // If a world has players we do mob spawning in it
                SpawnerSystem.checkRules(event);
            }

            if (event.getLevel().dimension().equals(Level.OVERWORLD)) {
                DataStorage.getData(event.getLevel()).tick(event.getLevel());
            }

            EventsSystem.onLevelTick(event);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawnEvent(FinalizeSpawnEvent event) {
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
                        event.setResult(MobSpawnEvent.PositionCheck.Result.SUCCEED);
                        Statistics.addSpawnStat(i, false);
                        rule.action(event);
                        break;
                    case DEFAULT:
                        event.setResult(MobSpawnEvent.PositionCheck.Result.DEFAULT);
                        Statistics.addSpawnStat(i, false);
                        rule.action(event);
                        break;
                    case DENY:
                        event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
                        Statistics.addSpawnStat(i, true);
                        break;
                    case DENY_WITH_ACTIONS:
                        event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
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
    public void onCheckDespawn(MobDespawnEvent event) {
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
                        event.setResult(MobDespawnEvent.Result.ALLOW);
                        rule.action(event);
                        break;
                    case DEFAULT:
                        event.setResult(MobDespawnEvent.Result.DEFAULT);
                        rule.action(event);
                        break;
                    case DENY:
                        event.setResult(MobDespawnEvent.Result.DENY);
                        break;
                    case DENY_WITH_ACTIONS:
                        event.setResult(MobDespawnEvent.Result.DENY);
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
    public void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        int i = 0;
        for (ExperienceRule rule : RulesManager.getFilteredExperienceRuiles(event.getEntity().level())) {
            if (rule.match(event)) {
                MobSpawnEvent.SpawnPlacementCheck.Result result = rule.getResult();
                if (debug) {
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Experience Rule " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getEntity().blockPosition().getY());
                }
                if (result != MobSpawnEvent.SpawnPlacementCheck.Result.FAIL) {
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

    public static Map<Integer, Integer> tickCounters = new HashMap<>();

    @SubscribeEvent
    public void onRightClickEvent(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        int i = 0;
        for (RightClickRule rule : RulesManager.getFilteredRightClickRules(event.getLevel())) {
            if (rule.match(event)) {
                MobSpawnEvent.SpawnPlacementCheck.Result result = rule.getResult();
                if (debug) {
                    Holder<Biome> biome = event.getLevel().getBiome(event.getPos());
                    String biomeId = Tools.getBiomeId(biome);
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Rule " + i + ": "+ result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getPos().getY()
                            + " biome: " + biomeId);
                }
                rule.action(event);
                switch (result) {
                    case SUCCEED -> {
                        event.setUseBlock(TriState.TRUE);
                    }
                    case DEFAULT -> {
                        event.setUseBlock(TriState.DEFAULT);
                    }
                    case FAIL -> {
                        event.setUseBlock(TriState.FALSE);
                        event.setCanceled(true);
                    }
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
                MobSpawnEvent.SpawnPlacementCheck.Result result = rule.getResult();
                if (debug) {
                    Holder<Biome> biome = event.getLevel().getBiome(event.getPos());
                    String biomeId = Tools.getBiomeId(biome);
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Rule " + i + ": "+ result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getPos().getY()
                            + " biome: " + biomeId);
                }
                rule.action(event);
                switch (result) {
                    case SUCCEED -> {
                        event.setUseBlock(TriState.TRUE);
                    }
                    case DEFAULT -> {
                        event.setUseBlock(TriState.DEFAULT);
                    }
                    case FAIL -> {
                        event.setUseBlock(TriState.FALSE);
                        event.setCanceled(true);
                    }
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
                MobSpawnEvent.SpawnPlacementCheck.Result result = rule.getResult();
                if (debug) {
                    Holder<Biome> biome = event.getLevel().getBiome(event.getPos());
                    String biomeId = Tools.getBiomeId(biome);
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Rule " + i + ": "+ result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getPos().getY()
                            + " biome: " + biomeId);
                }
                rule.action(event);
                if (result == MobSpawnEvent.SpawnPlacementCheck.Result.FAIL) {
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
                MobSpawnEvent.SpawnPlacementCheck.Result result = rule.getResult();
                if (debug) {
                    Holder<Biome> biome = event.getLevel().getBiome(event.getPos());
                    String biomeId = Tools.getBiomeId(biome);
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Rule " + i + ": "+ result
                            + " entity: " + event.getPlayer().getName()
                            + " y: " + event.getPos().getY()
                            + " biome: " + biomeId);
                }
                rule.action(event);
                if (result == MobSpawnEvent.SpawnPlacementCheck.Result.FAIL) {
                    event.setCanceled(true);
                }
                return;
            }
            i++;
        }

        EventsSystem.onBlockBreak(event);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        int id = event.getEntity().getId();
        if (!tickCounters.containsKey(id)) {
            tickCounters.put(id, 0);
        }
        int tickCounter = tickCounters.get(id) + 1;
        tickCounters.put(id, tickCounter);
        int i = 0;
        for (EffectRule rule : RulesManager.getFilteredEffectRules(event.getEntity().getCommandSenderWorld())) {
            if (tickCounter % rule.getTimeout() == 0 && rule.match(event)) {
                if (debug) {
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.INFO, "Join Rule " + i
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getEntity().blockPosition().getY());
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
