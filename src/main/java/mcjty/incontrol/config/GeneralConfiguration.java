package mcjty.incontrol.config;

import net.minecraftforge.common.config.Configuration;

public class GeneralConfiguration {

    public static final String CATEGORY_GENERAL = "general";

    public static int MAX_PLAYER_DISTANCE = 100;

    public static void init(Configuration cfg) {

        MAX_PLAYER_DISTANCE = cfg.getInt("maxPlayerDistance", CATEGORY_GENERAL, MAX_PLAYER_DISTANCE, 0, Integer.MAX_VALUE, "The maximum range for finding the nearest player for the spawn rules");
    }

}
