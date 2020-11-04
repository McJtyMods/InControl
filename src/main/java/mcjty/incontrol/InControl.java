package mcjty.incontrol;


import mcjty.incontrol.commands.*;
import mcjty.incontrol.rules.EntityModCache;
import mcjty.incontrol.rules.RulesManager;
import mcjty.incontrol.setup.IProxy;
import mcjty.incontrol.setup.ModSetup;
import mcjty.tools.cache.StructureCache;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;

@Mod(modid = InControl.MODID, name = InControl.MODNAME,
        dependencies =
                "after:forge@[" + InControl.MIN_FORGE11_VER + ",)",
        version = InControl.VERSION,
        acceptedMinecraftVersions = "[1.12,1.13)",
        acceptableRemoteVersions = "*")
public class InControl {

    public static final String MODID = "incontrol";
    public static final String MODNAME = "InControl";
    public static final String VERSION = "3.9.18";
    public static final String MIN_FORGE11_VER = "13.19.0.2176";

    @SidedProxy(clientSide = "mcjty.incontrol.setup.ClientProxy", serverSide = "mcjty.incontrol.setup.ServerProxy")
    public static IProxy proxy;
    public static ModSetup setup = new ModSetup();

    @Mod.Instance
    public static InControl instance;

    public EntityModCache modCache = new EntityModCache();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        setup.preInit(event);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        setup.init(e);
        proxy.init(e);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        setup.postInit(e);
        proxy.postInit(e);
    }

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent e) {
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
    public void serverStopped(FMLServerStoppedEvent event) {
        StructureCache.CACHE.clean();
    }
}
