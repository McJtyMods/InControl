package mcjty.incontrol.compat;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.IStageData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public class GameStageSupport {

    public static boolean hasGameStage(DamageSource source, String stage) {
        Entity entity = source.getTrueSource();
        if (entity instanceof EntityPlayer) {
            IStageData stageData = GameStageHelper.getPlayerData((EntityPlayer) entity);
            return stageData.hasStage(stage);
        } else {
            return false;
        }
    }
}
