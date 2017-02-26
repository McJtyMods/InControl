package mcjty.incontrol;

import mcjty.incontrol.rules.*;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;

import java.util.List;

public class ForgeEventHandlers {

    public static boolean debug = false;

    @SubscribeEvent
    public void onEntitySpawnEvent(LivingSpawnEvent.CheckSpawn event) {
        int i = 0;
        for (SpawnRule rule : RulesManager.rules) {
            if (rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    InControl.logger.log(Level.INFO, "Rule " + i + ": "+ result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getY()
                            + " biome: " + event.getWorld().getBiome(new BlockPos(event.getX(), event.getY(), event.getZ())).getBiomeName());
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

    @SubscribeEvent
    public void onSummonAidEvent(ZombieEvent.SummonAidEvent event) {
        int i = 0;
        for (SummonAidRule rule : RulesManager.summonAidRules) {
            if (rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    InControl.logger.log(Level.INFO, "SummonAid " + i + ": "+ result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getY()
                            + " biome: " + event.getWorld().getBiome(new BlockPos(event.getX(), event.getY(), event.getZ())).getBiomeName());
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

    @SubscribeEvent
    public void onPotentialSpawns(WorldEvent.PotentialSpawns event) {
        int i = 0;
        for (PotentialSpawnRule rule : RulesManager.potentialSpawnRules) {
            if (rule.match(event)) {

                // First remove mob entries if needed
                for (Class clazz : rule.getToRemoveMobs()) {
                    for (int idx = event.getList().size()-1 ; idx >= 0 ; idx--) {
                        if (event.getList().get(idx).entityClass == clazz) {
                            event.getList().remove(idx);
                        }
                    }
                }

                List<Biome.SpawnListEntry> spawnEntries = rule.getSpawnEntries();
                for (Biome.SpawnListEntry entry : spawnEntries) {
                    if (debug) {
                        InControl.logger.log(Level.INFO, "Potential " + i + ": "+ entry.entityClass.toString());
                    }
                    event.getList().add(entry);
                }
            }
            i++;
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        int i = 0;
        for (LootRule rule : RulesManager.lootRules) {
            if (rule.match(event)) {
                if (debug) {
                    InControl.logger.log(Level.INFO, "Loot " + i + ": "
                            + " entity: " + event.getEntity().getName());
                }

                if (rule.isRemoveAll()) {
                    event.getDrops().clear();
                } else {
                    for (ItemStack item : rule.getToRemoveItems()) {
                        for (int idx = event.getDrops().size() - 1; idx >= 0; idx--) {
                            ItemStack stack = event.getDrops().get(idx).getEntityItem();
                            if (ItemStackTools.isValid(stack) && stack.isItemEqual(item)) {
                                event.getDrops().remove(idx);
                            }
                        }
                    }
                }

                for (ItemStack item : rule.getToAddItems()) {
                    BlockPos pos = event.getEntity().getPosition();
                    event.getDrops().add(new EntityItem(event.getEntity().getEntityWorld(), pos.getX(), pos.getY(), pos.getZ(),
                            item));
                }
            }
            i++;
        }
    }

}
