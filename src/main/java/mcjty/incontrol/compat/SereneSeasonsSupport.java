package mcjty.incontrol.compat;

import net.minecraft.world.World;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

public class SereneSeasonsSupport {

    public static boolean isSpring(World world) {
        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
        return Season.SPRING.equals(seasonState.getSeason());
    }

    public static boolean isSummer(World world) {
        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
        return Season.SUMMER.equals(seasonState.getSeason());
    }

    public static boolean isWinter(World world) {
        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
        return Season.WINTER.equals(seasonState.getSeason());
    }

    public static boolean isAutumn(World world) {
        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
        return Season.AUTUMN.equals(seasonState.getSeason());
    }
}
