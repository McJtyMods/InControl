package mcjty.incontrol;


import mcjty.incontrol.setup.ModSetup;
import mcjty.incontrol.tools.cache.StructureCache;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@Mod(InControl.MODID)
public class InControl {

    public static final String MODID = "incontrol";

    public static ModSetup setup = new ModSetup();

    public InControl(IEventBus bus) {
        bus.addListener((FMLCommonSetupEvent event) -> setup.init());
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> StructureCache.CACHE.clean());
        NeoForge.EVENT_BUS.addListener(ErrorHandler::onPlayerJoinWorld);
    }
}
