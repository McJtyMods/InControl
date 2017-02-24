package mcjty.incontrol.proxy;

import com.google.common.util.concurrent.ListenableFuture;
import mcjty.incontrol.ForgeEventHandlers;
import mcjty.incontrol.config.ConfigSetup;
import mcjty.incontrol.rules.RulesManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.concurrent.Callable;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent e) {
        //PacketHandler.registerMessages("incontrol");
        ConfigSetup.preInit(e);
        RulesManager.setRulePath(e.getModConfigurationDirectory());
    }

    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
    }

    public void postInit(FMLPostInitializationEvent e) {
        ConfigSetup.postInit();
        RulesManager.readRules();
    }

    public World getClientWorld() {
        throw new IllegalStateException("This should only be called from client side");
    }

    public EntityPlayer getClientPlayer() {
        throw new IllegalStateException("This should only be called from client side");
    }

    public <V> ListenableFuture<V> addScheduledTaskClient(Callable<V> callableToSchedule) {
        throw new IllegalStateException("This should only be called from client side");
    }

    public ListenableFuture<Object> addScheduledTaskClient(Runnable runnableToSchedule) {
        throw new IllegalStateException("This should only be called from client side");
    }

}
