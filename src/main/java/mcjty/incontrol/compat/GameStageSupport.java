package mcjty.incontrol.compat;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.IStageData;
import net.minecraft.world.entity.player.Player;

public class GameStageSupport {

    public static boolean hasGameStage(Player player, String stage) {
        if (player != null) {
            IStageData stageData = GameStageHelper.getPlayerData(player);
            return stageData.hasStage(stage);
        } else {
            return false;
        }
    }

    public static void addGameStage(Player player, String stage) {
        if (player != null) {
            IStageData stageData = GameStageHelper.getPlayerData(player);
            stageData.addStage(stage);
        }
    }

    public static void removeGameStage(Player player, String stage) {
        if (player != null) {
            IStageData stageData = GameStageHelper.getPlayerData(player);
            stageData.addStage(stage);
        }
    }
}
