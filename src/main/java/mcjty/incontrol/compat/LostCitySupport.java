package mcjty.incontrol.compat;

import mcjty.incontrol.InControl;
import mcjty.incontrol.tools.rules.IEventQuery;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.function.Function;

public class LostCitySupport {

    private static boolean registered = false;
    private static Object lostCities;
    private static Method getLostInfo;
    private static Method getChunkInfo;
    private static Method getSphere;
    private static Method isCity;
    private static Method getBuildingType;
    private static Method getMultiBuildingInfo;
    private static Method buildingType;

    public static void register() {
        if (registered || !ModList.get().isLoaded("lostcities")) {
            return;
        }
        try {
            // Use the public API from the installed mod, so an unpublished API jar
            // or a development checkout is not required to build In Control.
            getLostInfo = Class.forName("mcjty.lostcities.api.ILostCities").getMethod("getLostInfo", Level.class);
            Class<?> info = Class.forName("mcjty.lostcities.api.ILostCityInformation");
            getChunkInfo = info.getMethod("getChunkInfo", int.class, int.class);
            getSphere = info.getMethod("getSphere", int.class, int.class);
            Class<?> chunk = Class.forName("mcjty.lostcities.api.ILostChunkInfo");
            isCity = chunk.getMethod("isCity");
            getBuildingType = chunk.getMethod("getBuildingType");
            getMultiBuildingInfo = chunk.getMethod("getMultiBuildingInfo");
            buildingType = Class.forName("mcjty.lostcities.api.ILostChunkInfo$MultiBuildingInfo").getMethod("buildingType");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unsupported Lost Cities API", e);
        }
        registered = true;
        InterModComms.sendTo("lostcities", "getLostCities", GetLostCities::new);
        InControl.setup.getLogger().info("Enabling support for Lost Cities");
    }

    private static Object call(Method method, Object target, Object... arguments) {
        try {
            return method.invoke(target, arguments);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Error querying Lost Cities", e);
        }
    }

    private static <T> Level getWorld(IEventQuery<T> query, T event) {
        LevelAccessor world = query.getWorld(event);
        if (world.isClientSide()) {
            return null;
        }
        if (world instanceof Level level) {
            return level;
        }
        if (world instanceof ServerLevelAccessor accessor) {
            return accessor.getLevel();
        }
        throw new IllegalStateException("Bad world!");
    }

    private static Object getInfo(Level world) {
        return world == null || lostCities == null ? null : call(getLostInfo, lostCities, world);
    }

    private static Object getChunk(Level world, BlockPos pos) {
        Object info = getInfo(world);
        return info == null ? null : call(getChunkInfo, info, pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static <T> boolean isCity(IEventQuery<T> query, T event) {
        return isCity(getWorld(query, event), query.getPos(event));
    }

    public static boolean isCity(Level world, BlockPos pos) {
        Object chunk = getChunk(world, pos);
        return chunk != null && (boolean) call(isCity, chunk);
    }

    public static <T> boolean isStreet(IEventQuery<T> query, T event) {
        return isStreet(getWorld(query, event), query.getPos(event));
    }

    public static boolean isStreet(Level world, BlockPos pos) {
        Object chunk = getChunk(world, pos);
        return chunk != null && (boolean) call(isCity, chunk) && call(getBuildingType, chunk) == null;
    }

    public static <T> boolean inSphere(IEventQuery<T> query, T event) {
        return inSphere(getWorld(query, event), query.getPos(event));
    }

    public static boolean inSphere(Level world, BlockPos pos) {
        Object info = getInfo(world);
        return info != null && call(getSphere, info, pos.getX(), pos.getZ()) != null;
    }

    public static <T> boolean isBuilding(IEventQuery<T> query, T event) {
        return isBuilding(getWorld(query, event), query.getPos(event));
    }

    public static boolean isBuilding(Level world, BlockPos pos) {
        Object chunk = getChunk(world, pos);
        return chunk != null && (boolean) call(isCity, chunk) && call(getBuildingType, chunk) != null;
    }

    public static <T> boolean isMultiBuilding(IEventQuery<T> query, T event) {
        return isMultiBuilding(getWorld(query, event), query.getPos(event));
    }

    public static boolean isMultiBuilding(Level world, BlockPos pos) {
        Object chunk = getChunk(world, pos);
        return chunk != null && (boolean) call(isCity, chunk) && call(getMultiBuildingInfo, chunk) != null;
    }

    public static <T> String getBuildingName(IEventQuery<T> query, T event) {
        return getBuildingName(getWorld(query, event), query.getPos(event));
    }

    public static String getBuildingName(Level world, BlockPos pos) {
        Object chunk = getChunk(world, pos);
        return chunk == null ? null : (String) call(getBuildingType, chunk);
    }

    public static <T> String getMultiBuildingName(IEventQuery<T> query, T event) {
        return getMultiBuildingName(getWorld(query, event), query.getPos(event));
    }

    public static String getMultiBuildingName(Level world, BlockPos pos) {
        Object chunk = getChunk(world, pos);
        Object multi = chunk == null ? null : call(getMultiBuildingInfo, chunk);
        return multi == null ? null : call(buildingType, multi).toString();
    }

    public static class GetLostCities implements Function<Object, Void> {
        @Override
        public Void apply(Object lc) {
            lostCities = lc;
            return null;
        }
    }
}
