package mcjty.incontrol;


import mcjty.incontrol.setup.ModSetup;
import mcjty.incontrol.tools.cache.StructureCache;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(InControl.MODID)
public class InControl {

    public static final String MODID = "incontrol";

    public static ModSetup setup = new ModSetup();

    public InControl() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> setup.init());
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> StructureCache.CACHE.clean());
        MinecraftForge.EVENT_BUS.addListener(ErrorHandler::onPlayerJoinWorld);
    }
}
