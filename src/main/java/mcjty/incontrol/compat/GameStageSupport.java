package mcjty.incontrol.compat;

import net.minecraft.entity.player.PlayerEntity;

public class GameStageSupport {

    public static boolean hasGameStage(PlayerEntity player, String stage) {
        // @todo 1.15
//        if (player != null) {
//            IStageData stageData = GameStageHelper.getPlayerData(player);
//            return stageData.hasStage(stage);
//        } else {
//            return false;
//        }
        return false;
    }

    public static void addGameStage(PlayerEntity player, String stage) {
        // @todo 1.15
//        if (player != null) {
//            IStageData stageData = GameStageHelper.getPlayerData(player);
//            stageData.addStage(stage);
//        }
    }

    public static void removeGameStage(PlayerEntity player, String stage) {
        // @todo 1.15
//        if (player != null) {
//            IStageData stageData = GameStageHelper.getPlayerData(player);
//            stageData.addStage(stage);
//        }
    }
}
