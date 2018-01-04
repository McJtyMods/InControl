package mcjty.incontrol.compat;

import net.darkhax.gamestages.capabilities.PlayerDataHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public class GameStageSupport {

    public static boolean hasGameStage(DamageSource source, String stage) {
        Entity entity = source.getTrueSource();
        if (entity instanceof EntityPlayer) {
            PlayerDataHandler.IStageData stageData = PlayerDataHandler.getStageData((EntityPlayer) entity);
            return stageData.hasUnlockedStage(stage);
        } else {
            return false;
        }
    }
}
