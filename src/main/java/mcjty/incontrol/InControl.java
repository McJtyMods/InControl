package mcjty.incontrol;


import mcjty.incontrol.setup.Config;
import mcjty.incontrol.setup.ModSetup;
import mcjty.incontrol.tools.cache.StructureCache;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(InControl.MODID)
public class InControl {

    public static final String MODID = "incontrol";

    public static ModSetup setup = new ModSetup();

    public InControl() {
        Config.register();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener((FMLCommonSetupEvent event) -> setup.init());
        bus.addListener(ModBusHandler::addEntityAttributes);
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> StructureCache.CACHE.clean());
        MinecraftForge.EVENT_BUS.addListener(ErrorHandler::onPlayerJoinWorld);
    }
}
