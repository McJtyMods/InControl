package mcjty.incontrol.compat;

import mcjty.incontrol.InControl;
import mcjty.incontrol.tools.rules.IEventQuery;
import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.api.ILostCityInformation;
import mcjty.lostcities.api.ILostSphere;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;

import java.util.function.Function;

public class LostCitySupport {

    private static boolean registered = false;
    private static ILostCities lostCities;

    public static void register() {
        if (ModList.get().isLoaded("lostcities")) {
            registerInternal();
        }
    }

    private static void registerInternal() {
        if (registered) {
            return;
        }
        registered = true;
        InterModComms.sendTo("lostcities", "getLostCities", GetLostCities::new);
        InControl.setup.getLogger().info("Enabling support for Lost Cities");
    }

    private static <T> Level getWorld(IEventQuery<T> query, T event) {
        LevelAccessor world = query.getWorld(event);
        if (world.isClientSide()) {
            return null;
        }
        Level w;
        if (world instanceof Level) {
            w = (Level) world;
        } else if (world instanceof ServerLevelAccessor) {
            w = ((ServerLevelAccessor) world).getLevel();
        } else {
            throw new IllegalStateException("Bad world!");
        }
        return w;
    }


    public static <T> boolean isCity(IEventQuery<T> query, T event) {
        Level w = getWorld(query, event);
        if (w == null) {
            return false;   // This test don't work client side
        }
        ILostCityInformation info = lostCities.getLostInfo(w);
        if (info != null) {
            BlockPos pos = query.getPos(event);
            ILostChunkInfo chunkInfo = info.getChunkInfo(pos.getX() >> 4, pos.getZ() >> 4);
            return chunkInfo.isCity();
        }
        return false;
    }

    public static <T> boolean isStreet(IEventQuery<T> query, T event) {
        Level w = getWorld(query, event);
        if (w == null) {
            return false;   // This test don't work client side
        }
        ILostCityInformation info = lostCities.getLostInfo(w);
        if (info != null) {
            BlockPos pos = query.getPos(event);
            ILostChunkInfo chunkInfo = info.getChunkInfo(pos.getX() >> 4, pos.getZ() >> 4);
            return chunkInfo.isCity() && chunkInfo.getBuildingType() == null;
        }
        return false;
    }

    public static <T> boolean inSphere(IEventQuery<T> query, T event) {
        Level w = getWorld(query, event);
        if (w == null) {
            return false;   // This test don't work client side
        }
        ILostCityInformation info = lostCities.getLostInfo(w);
        if (info != null) {
            BlockPos pos = query.getPos(event);
            ILostSphere sphere = info.getSphere(pos.getX(), pos.getZ());
            return sphere != null;
        }
        return false;
    }


    public static <T> boolean isBuilding(IEventQuery<T> query, T event) {
        Level w = getWorld(query, event);
        if (w == null) {
            return false;   // This test don't work client side
        }
        ILostCityInformation info = lostCities.getLostInfo(w);
        if (info != null) {
            BlockPos pos = query.getPos(event);
            ILostChunkInfo chunkInfo = info.getChunkInfo(pos.getX() >> 4, pos.getZ() >> 4);
            return chunkInfo.isCity() && chunkInfo.getBuildingType() != null;
        }
        return false;
    }

    public static class GetLostCities implements Function<ILostCities, Void> {

        @Override
        public Void apply(ILostCities lc) {
            lostCities = lc;
            return null;
        }
    }

}

