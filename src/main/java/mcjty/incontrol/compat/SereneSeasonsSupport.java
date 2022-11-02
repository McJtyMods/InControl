package mcjty.incontrol.compat;

import net.minecraft.world.level.Level;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

public class SereneSeasonsSupport {

    public static boolean isSpring(Level world) {
        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
        return Season.SPRING.equals(seasonState.getSeason());
    }

    public static boolean isSummer(Level world) {
        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
        return Season.SUMMER.equals(seasonState.getSeason());
    }

    public static boolean isWinter(Level world) {
        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
        return Season.WINTER.equals(seasonState.getSeason());
    }

    public static boolean isAutumn(Level world) {
        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
        return Season.AUTUMN.equals(seasonState.getSeason());
    }
}
