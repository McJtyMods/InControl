package mcjty.incontrol;


import mcjty.incontrol.commands.*;
import mcjty.incontrol.rules.RulesManager;
import mcjty.incontrol.setup.IProxy;
import mcjty.incontrol.setup.ModSetup;
import mcjty.tools.cache.StructureCache;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = InControl.MODID, name = InControl.MODNAME,
        dependencies =
                "after:forge@[" + InControl.MIN_FORGE14_VER + ",)",
        version = InControl.VERSION,
        acceptedMinecraftVersions = "[1.12,1.13)",
        acceptableRemoteVersions = "*")
public class InControl {

    public static final String MODID = "incontrol";
    public static final String MODNAME = "InControl";
    public static final String VERSION = "3.9.8";
    public static final String MIN_FORGE14_VER = "14.23.5.2768";

    @SidedProxy(clientSide = "mcjty.incontrol.setup.ClientProxy", serverSide = "mcjty.incontrol.setup.ServerProxy")
    public static IProxy proxy;
    public static final ModSetup setup = new ModSetup();

    @Mod.Instance
    public static InControl instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        setup.preInit(event);
    }

    @Mod.EventHandler
    public void postInit() {
        setup.postInit();
    }

    @Mod.EventHandler
    public void onLoadComplete() {
        RulesManager.readRules();
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CmdReload());
        event.registerServerCommand(new CmdDebug());
        event.registerServerCommand(new CmdLoadSpawn());
        event.registerServerCommand(new CmdLoadPotentialSpawn());
        event.registerServerCommand(new CmdLoadSummonAid());
        event.registerServerCommand(new CmdLoadLoot());
        event.registerServerCommand(new CmdShowMobs());
        event.registerServerCommand(new CmdKillMobs());
    }

    @Mod.EventHandler
    public void serverStopped() {
        ForgeEventHandlers.debugtype = 0;
        StructureCache.CACHE.clean();
    }
}
