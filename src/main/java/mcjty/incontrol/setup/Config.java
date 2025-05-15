package mcjty.incontrol.setup;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {

    public static ForgeConfigSpec.IntValue CACHE_RETENTION_TICKS;
    public static ForgeConfigSpec.IntValue PERPLAYER_RADIUS;

    public static void register() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("General settings").push("general");

        CACHE_RETENTION_TICKS = builder
                .comment("How many ticks that the count cache is kept alive. This is used to avoid recalculating the cache every tick. Use 0 to recalculate every tick.")
                .defineInRange("cacheRetentionTicks", 10, 0, 10000);
        PERPLAYER_RADIUS = builder
                .comment("The radius that is used for player specific conditions (to find the nearest player)")
                .defineInRange("perPlayerRadius", 100, 1, 10000);

        builder.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, builder.build());
    }
}
