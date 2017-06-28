package mcjty.incontrol.compat;

import mcjty.incontrol.rules.support.IEventQuery;
import mcjty.lostcities.api.ILostChunkGenerator;
import mcjty.lostcities.api.ILostChunkInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class LostCitySupport {
    public static <T> boolean isCity(IEventQuery<T> query, T event) {
        World world = query.getWorld(event);
        WorldServer ws = (WorldServer) world;
        if (ws.getChunkProvider().chunkGenerator instanceof ILostChunkGenerator) {
            ILostChunkGenerator gen = (ILostChunkGenerator) ws.getChunkProvider().chunkGenerator;
            BlockPos pos = query.getPos(event);
            ILostChunkInfo chunkInfo = gen.getChunkInfo(pos.getX() >> 4, pos.getZ() >> 4);
            return chunkInfo.isCity();
        }
        return false;
    }

    public static <T> boolean isStreet(IEventQuery<T> query, T event) {
        World world = query.getWorld(event);
        WorldServer ws = (WorldServer) world;
        if (ws.getChunkProvider().chunkGenerator instanceof ILostChunkGenerator) {
            ILostChunkGenerator gen = (ILostChunkGenerator) ws.getChunkProvider().chunkGenerator;
            BlockPos pos = query.getPos(event);
            ILostChunkInfo chunkInfo = gen.getChunkInfo(pos.getX() >> 4, pos.getZ() >> 4);
            return chunkInfo.isCity() && chunkInfo.getBuildingType() == null;
        }
        return false;
    }

    public static <T> boolean isBuilding(IEventQuery<T> query, T event) {
        World world = query.getWorld(event);
        WorldServer ws = (WorldServer) world;
        if (ws.getChunkProvider().chunkGenerator instanceof ILostChunkGenerator) {
            ILostChunkGenerator gen = (ILostChunkGenerator) ws.getChunkProvider().chunkGenerator;
            BlockPos pos = query.getPos(event);
            ILostChunkInfo chunkInfo = gen.getChunkInfo(pos.getX() >> 4, pos.getZ() >> 4);
            return chunkInfo.isCity() && chunkInfo.getBuildingType() != null;
        }
        return false;
    }
}
