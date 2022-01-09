package mcjty.incontrol.compat;

import net.minecraft.world.level.LevelAccessor;

public class SereneSeasonsSupport {

    public static boolean isSpring(LevelAccessor world) {
        // @todo 1.15
        return false;
//        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
//        return Season.SPRING.equals(seasonState.getSeason());
    }

    public static boolean isSummer(LevelAccessor world) {
        // @todo 1.15
        return false;
//        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
//        return Season.SUMMER.equals(seasonState.getSeason());
    }

    public static boolean isWinter(LevelAccessor world) {
        // @todo 1.15
        return false;
//        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
//        return Season.WINTER.equals(seasonState.getSeason());
    }

    public static boolean isAutumn(LevelAccessor world) {
        // @todo 1.15
        return false;
//        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
//        return Season.AUTUMN.equals(seasonState.getSeason());
    }
}
