package mcjty.incontrol;

import mcjty.incontrol.commands.ModCommands;
import mcjty.incontrol.rules.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class ForgeEventHandlers {

    public static boolean debug = false;

    @SubscribeEvent
    public void serverLoad(FMLServerStartingEvent event) {
        ModCommands.register(event.getCommandDispatcher());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        int i = 0;
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        if (event.getWorld().isRemote) {
            return;
        }
        for (SpawnRule rule : RulesManager.rules) {
            if (rule.isOnJoin() && rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    InControl.setup.getLogger().log(Level.INFO, "Join Rule " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getEntity().getPosition().getY());
                }
                if (result != Event.Result.DENY) {
                    rule.action(event);
                } else {
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
    public void onEntityJoinWorldLast(EntityJoinWorldEvent event) {
        // We register spawns in a high priority event so that we take things that other mods
        // do into account
        if (!event.getWorld().isRemote() && event.getEntity() instanceof LivingEntity) {
            InControl.setup.cache.registerSpawn(event.getWorld(), event.getEntity().getType());
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        // For every world tick we reset the cache
        if (event.phase == TickEvent.Phase.START && !event.world.isRemote) {
            InControl.setup.cache.reset(event.world);
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawnEvent(LivingSpawnEvent.CheckSpawn event) {
        int i = 0;
        for (SpawnRule rule : RulesManager.rules) {
            if (rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    InControl.setup.getLogger().log(Level.INFO, "Rule " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getY()
                            + " biome: " + new TranslationTextComponent(event.getWorld().getBiome(new BlockPos(event.getX(), event.getY(), event.getZ())).getTranslationKey()).getFormattedText());
                }
                if (result != null) {
                    event.setResult(result);
                }
                if (result != Event.Result.DENY) {
                    rule.action(event);
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
        for (SummonAidRule rule : RulesManager.summonAidRules) {
            if (rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    InControl.setup.getLogger().log(Level.INFO, "SummonAid " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getY()
                            + " biome: " + new TranslationTextComponent(event.getWorld().getBiome(new BlockPos(event.getX(), event.getY(), event.getZ())).getTranslationKey()).getFormattedText());
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
    public void onPotentialSpawns(WorldEvent.PotentialSpawns event) {
        int i = 0;
        for (PotentialSpawnRule rule : RulesManager.potentialSpawnRules) {
            if (rule.match(event)) {

                // First remove mob entries if needed
                for (int idx = event.getList().size() - 1; idx >= 0; idx--) {
                    if (rule.getToRemoveMobs().contains(event.getList().get(idx).entityType)) {
                        event.getList().remove(idx);
                    }
                }

                List<Biome.SpawnListEntry> spawnEntries = rule.getSpawnEntries();
                for (Biome.SpawnListEntry entry : spawnEntries) {
                    if (debug) {
                        InControl.setup.getLogger().log(Level.INFO, "Potential " + i + ": " + entry.entityType.getRegistryName().toString());
                    }
                    event.getList().add(entry);
                }
            }
            i++;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        int i = 0;
        for (ExperienceRule rule : RulesManager.experienceRules) {
            if (rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    InControl.setup.getLogger().log(Level.INFO, "Experience Rule " + i + ": " + result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getEntity().getPosition().getY());
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
        int i = 0;
        for (LootRule rule : RulesManager.lootRules) {
            if (rule.match(event)) {
                if (debug) {
                    InControl.setup.getLogger().log(Level.INFO, "Loot " + i + ": "
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
                    BlockPos pos = event.getEntity().getPosition();
                    while (amount > item.getMaxStackSize()) {
                        ItemStack copy = item.copy();
                        copy.setCount(item.getMaxStackSize());
                        amount -= item.getMaxStackSize();
                        event.getDrops().add(new ItemEntity(event.getEntity().getEntityWorld(), pos.getX(), pos.getY(), pos.getZ(),
                                copy));
                    }
                    if (amount > 0) {
                        ItemStack copy = item.copy();
                        copy.setCount(amount);
                        event.getDrops().add(new ItemEntity(event.getEntity().getEntityWorld(), pos.getX(), pos.getY(), pos.getZ(),
                                copy));
                    }
                }
            }
            i++;
        }
    }

}
