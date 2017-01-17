package mcjty.incontrol;

import mcjty.incontrol.rules.PotentialSpawnRule;
import mcjty.incontrol.rules.SpawnRule;
import mcjty.incontrol.rules.SpawnRules;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
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
        for (SpawnRule rule : SpawnRules.rules) {
            if (rule.match(event)) {
                Event.Result result = rule.getResult();
                if (debug) {
                    InControl.logger.log(Level.INFO, "Rule " + i + ": "+ result
                            + " entity: " + event.getEntity().getName()
                            + " y: " + event.getY()
                            + " biome: " + event.getWorld().getBiome(new BlockPos(event.getX(), event.getY(), event.getZ())).getBiomeName());
                }
                event.setResult(result);
                if (result == Event.Result.ALLOW) {
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
        for (PotentialSpawnRule rule : SpawnRules.potentialSpawnRules) {
            if (rule.match(event)) {
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
}
