package mcjty.incontrol.compat;

import net.minecraft.world.IWorld;

public class SereneSeasonsSupport {

    public static boolean isSpring(IWorld world) {
        // @todo 1.15
        return false;
//        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
//        return Season.SPRING.equals(seasonState.getSeason());
    }

    public static boolean isSummer(IWorld world) {
        // @todo 1.15
        return false;
//        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
//        return Season.SUMMER.equals(seasonState.getSeason());
    }

    public static boolean isWinter(IWorld world) {
        // @todo 1.15
        return false;
//        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
//        return Season.WINTER.equals(seasonState.getSeason());
    }

    public static boolean isAutumn(IWorld world) {
        // @todo 1.15
        return false;
//        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
//        return Season.AUTUMN.equals(seasonState.getSeason());
    }
}
