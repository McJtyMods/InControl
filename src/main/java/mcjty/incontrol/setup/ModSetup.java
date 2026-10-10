package mcjty.incontrol.setup;

import mcjty.incontrol.InControl;
import mcjty.incontrol.areas.AreaParser;
import mcjty.incontrol.compat.EnigmaSupport;
import mcjty.incontrol.compat.LostCitySupport;
import mcjty.incontrol.events.EventsParser;
import mcjty.incontrol.rules.support.RuleCache;
import mcjty.incontrol.rules.RulesManager;
import mcjty.incontrol.spawner.SpawnerParser;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModSetup {

    public static boolean lostcities = false;
    public static boolean gamestages = false;
    public static boolean sereneSeasons = false;
    public static boolean baubles = false;
    public static boolean enigma = false;
    public static boolean customnpcs = false;
    public static boolean kubejs = false;

    private Logger logger;
    public RuleCache cache = new RuleCache();

    public static final TagKey<Block> CAVE_BLOCK = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(InControl.MODID, "cave_block"));

    public void init() {
        logger = LogManager.getLogger(InControl.MODID);
        Path configDir = FabricLoader.getInstance().getConfigDir();
        try { Files.createDirectories(configDir.resolve("incontrol")); }
        catch (IOException e) { throw new IllegalStateException("Cannot create InControl rule directory", e); }
        setupModCompat();

        RulesManager.setRulePath(configDir);
        SpawnerParser.setRulePath(configDir);
        EventsParser.setRulePath(configDir);
        AreaParser.setRulePath(configDir);
    }

    public Logger getLogger() {
        return logger;
    }

    private void setupModCompat() {
        lostcities = FabricLoader.getInstance().isModLoaded("lostcities");
        gamestages = FabricLoader.getInstance().isModLoaded("gamestages");
        sereneSeasons = FabricLoader.getInstance().isModLoaded("sereneseasons");
        baubles = FabricLoader.getInstance().isModLoaded("baubles");
        enigma = FabricLoader.getInstance().isModLoaded("enigma");
        customnpcs = FabricLoader.getInstance().isModLoaded("customnpcs");
        kubejs = FabricLoader.getInstance().isModLoaded("kubejs");

        if (ModSetup.lostcities) {
            LostCitySupport.register();
            logger.log(Level.INFO, "Enabling support for Lost Cities");
        }
        if (ModSetup.gamestages) {
            logger.log(Level.INFO, "Enabling support for Game Stages");
        }
        if (ModSetup.sereneSeasons) {
            logger.log(Level.INFO, "Enabling support for Serene Seasons");
        }
        if (ModSetup.baubles) {
            logger.log(Level.INFO, "Enabling support for Baubles");
        }
        if (ModSetup.enigma) {
            EnigmaSupport.register();
            logger.log(Level.INFO, "Enabling support for EnigmaScript");
        }
        if (ModSetup.customnpcs) {
            logger.log(Level.INFO, "Enabling support for CustomNPCs");
        }
        if (ModSetup.kubejs) {
            logger.log(Level.INFO, "Enabling support for KubeJS");
        }
    }
}
