package mcjty.incontrol.compat;

import mcjty.incontrol.InControl;
import mcjty.incontrol.tools.rules.IEventQuery;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

public final class LostCitySupport {
    private static Object lostCities;
    public static void register() {
        Class<?> type = OptionalApi.type("mcjty.lostcities.LostCities");
        if (type != null) {
            try { lostCities = type.getField("lostCitiesImp").get(null); }
            catch (ReflectiveOperationException e) { InControl.setup.getLogger().warn("Compatible Fabric Lost Cities API not found", e); }
        }
    }
    private static Object info(Level level) { return level == null ? null : OptionalApi.invoke(lostCities, "getLostInfo", level); }
    private static Object chunk(Level level, BlockPos pos) { return OptionalApi.invoke(info(level), "getChunkInfo", pos.getX() >> 4, pos.getZ() >> 4); }
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


    public static <T> boolean isCity(IEventQuery<T> query, T event) { return isCity(getWorld(query, event), query.getPos(event)); }
    public static <T> boolean isStreet(IEventQuery<T> query, T event) { return isStreet(getWorld(query, event), query.getPos(event)); }
    public static <T> boolean inSphere(IEventQuery<T> query, T event) { return inSphere(getWorld(query, event), query.getPos(event)); }
    public static <T> boolean isBuilding(IEventQuery<T> query, T event) { return isBuilding(getWorld(query, event), query.getPos(event)); }
    public static <T> boolean isMultiBuilding(IEventQuery<T> query, T event) { return isMultiBuilding(getWorld(query, event), query.getPos(event)); }
    public static <T> String getBuildingName(IEventQuery<T> query, T event) { return getBuildingName(getWorld(query, event), query.getPos(event)); }
    public static <T> String getMultiBuildingName(IEventQuery<T> query, T event) { return getMultiBuildingName(getWorld(query, event), query.getPos(event)); }
    public static boolean isCity(Level level, BlockPos pos) { return Boolean.TRUE.equals(OptionalApi.invoke(chunk(level, pos), "isCity")); }
    public static boolean isStreet(Level level, BlockPos pos) { return isCity(level, pos) && getBuildingName(level, pos) == null; }
    public static boolean inSphere(Level level, BlockPos pos) { return OptionalApi.invoke(info(level), "getSphere", pos.getX(), pos.getZ()) != null; }
    public static boolean isBuilding(Level level, BlockPos pos) { return isCity(level, pos) && getBuildingName(level, pos) != null; }
    public static boolean isMultiBuilding(Level level, BlockPos pos) { return isCity(level, pos) && OptionalApi.invoke(chunk(level, pos), "getMultiBuildingInfo") != null; }
    public static String getBuildingName(Level level, BlockPos pos) {
        Object name = OptionalApi.invoke(chunk(level, pos), "getBuildingType");
        return name == null ? null : name.toString();
    }
    public static String getMultiBuildingName(Level level, BlockPos pos) {
        Object multi = OptionalApi.invoke(chunk(level, pos), "getMultiBuildingInfo");
        Object name = OptionalApi.invoke(multi, "buildingType");
        return name == null ? null : name.toString();
    }
}
