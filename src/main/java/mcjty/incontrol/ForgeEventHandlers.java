package mcjty.incontrol;

import mcjty.incontrol.rules.SpawnRule;
import mcjty.incontrol.rules.SpawnRules;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;

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
                return;
            }
            i++;
        }
    }
}
