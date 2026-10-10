package mcjty.incontrol.setup;

import mcjty.incontrol.InControl;
import mcjty.incontrol.platform.ServerLifecycleHooks;
import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** The two server settings retain the original per-world TOML file and keys. */
public final class Config {
    public static final IntValue CACHE_RETENTION_TICKS = new IntValue(10);
    public static final IntValue PERPLAYER_RADIUS = new IntValue(100);
    public static final class IntValue {
        private int value;
        private IntValue(int value) { this.value = value; }
        public int get() { return value; }
    }
    private Config() {}
    public static void load() {
        CACHE_RETENTION_TICKS.value = 10;
        PERPLAYER_RADIUS.value = 100;
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        Path path = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig/incontrol-server.toml");
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Path defaults = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().resolve("defaultconfigs/incontrol-server.toml");
                if (Files.exists(defaults)) Files.copy(defaults, path);
                else Files.writeString(path, """
                        [general]
                        # Ticks to retain the mob count cache (0 recalculates every tick).
                        cacheRetentionTicks = 10
                        # Radius for finding the nearest player for player-specific conditions.
                        perPlayerRadius = 100
                        """);
            }
            String section = "";
            int retention = 10;
            int radius = 100;
            for (String line : Files.readAllLines(path)) {
                String value = line.split("#", 2)[0].trim();
                if (value.startsWith("[") && value.endsWith("]")) { section = value.substring(1, value.length() - 1); continue; }
                if (!section.equals("general")) continue;
                String[] entry = value.split("=", 2);
                if (entry.length != 2) continue;
                switch (entry[0].trim()) {
                    case "cacheRetentionTicks" -> retention = checked(entry[1], 0, 10000);
                    case "perPlayerRadius" -> radius = checked(entry[1], 1, 10000);
                    default -> { }
                }
            }
            CACHE_RETENTION_TICKS.value = retention;
            PERPLAYER_RADIUS.value = radius;
        } catch (IOException | RuntimeException e) {
            InControl.setup.getLogger().error("Cannot load InControl settings from " + path + "; using defaults", e);
        }
    }
    private static int checked(String text, int min, int max) {
        int value = Integer.parseInt(text.trim().replace("_", ""));
        if (value < min || value > max) throw new IllegalArgumentException("Setting must be between " + min + " and " + max);
        return value;
    }
}
