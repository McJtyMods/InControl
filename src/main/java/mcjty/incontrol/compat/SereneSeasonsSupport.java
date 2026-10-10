package mcjty.incontrol.compat;

import net.minecraft.world.level.Level;

public final class SereneSeasonsSupport {
    private SereneSeasonsSupport() {}
    private static boolean season(Level level, String expected) {
        Object state = OptionalApi.invoke(OptionalApi.type("sereneseasons.api.season.SeasonHelper"), "getSeasonState", level);
        Object season = OptionalApi.invoke(state, "getSeason");
        return season != null && expected.equals(season.toString());
    }
    public static boolean isSpring(Level level) { return season(level, "SPRING"); }
    public static boolean isSummer(Level level) { return season(level, "SUMMER"); }
    public static boolean isWinter(Level level) { return season(level, "WINTER"); }
    public static boolean isAutumn(Level level) { return season(level, "AUTUMN"); }
}
