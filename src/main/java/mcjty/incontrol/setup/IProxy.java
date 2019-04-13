package mcjty.incontrol.setup;

import com.google.common.util.concurrent.ListenableFuture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.concurrent.Callable;

public interface IProxy {

    World getClientWorld();

    EntityPlayer getClientPlayer();

    <V> ListenableFuture<V> addScheduledTaskClient(Callable<V> callableToSchedule);

    ListenableFuture<Object> addScheduledTaskClient(Runnable runnableToSchedule);
}
