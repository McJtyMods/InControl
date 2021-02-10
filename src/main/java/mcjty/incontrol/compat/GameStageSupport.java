package mcjty.incontrol.compat;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.IStageData;
import net.minecraft.entity.player.PlayerEntity;

public class GameStageSupport {

    public static boolean hasGameStage(PlayerEntity player, String stage) {
        if (player != null) {
            IStageData stageData = GameStageHelper.getPlayerData(player);
            return stageData.hasStage(stage);
        } else {
            return false;
        }
    }

    public static void addGameStage(PlayerEntity player, String stage) {
        if (player != null) {
            IStageData stageData = GameStageHelper.getPlayerData(player);
            stageData.addStage(stage);
        }
    }

    public static void removeGameStage(PlayerEntity player, String stage) {
        if (player != null) {
            IStageData stageData = GameStageHelper.getPlayerData(player);
            stageData.addStage(stage);
        }
    }
}
