package mcjty.incontrol.compat;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.IStageData;
import net.minecraft.entity.player.EntityPlayer;

class GameStageSupport {

    public static boolean hasGameStage(EntityPlayer player, String stage) {
        if (player != null) {
            IStageData stageData = GameStageHelper.getPlayerData(player);
            return stageData.hasStage(stage);
        } else {
            return false;
        }
    }
}
