package mcjty.incontrol.setup;

import mcjty.incontrol.ForgeEventHandlers;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public static final TagKey<Block> CAVE_BLOCK = TagKey.create(Registries.BLOCK, new ResourceLocation(InControl.MODID, "cave_block"));

    public void init() {
        logger = LogManager.getLogger(InControl.MODID);
        setupModCompat();

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        RulesManager.setRulePath(FMLPaths.CONFIGDIR.get());
        SpawnerParser.setRulePath(FMLPaths.CONFIGDIR.get());
        EventsParser.setRulePath(FMLPaths.CONFIGDIR.get());
        AreaParser.setRulePath(FMLPaths.CONFIGDIR.get());
    }

    public Logger getLogger() {
        return logger;
    }

    private void setupModCompat() {
        lostcities = ModList.get().isLoaded("lostcities");
        gamestages = ModList.get().isLoaded("gamestages");
        sereneSeasons = ModList.get().isLoaded("sereneseasons");
        baubles = ModList.get().isLoaded("baubles");
        enigma = ModList.get().isLoaded("enigma");
        customnpcs = ModList.get().isLoaded("customnpcs");
        kubejs = ModList.get().isLoaded("kubejs");

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
