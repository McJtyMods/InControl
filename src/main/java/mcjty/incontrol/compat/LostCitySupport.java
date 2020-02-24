package mcjty.incontrol.compat;

import mcjty.tools.rules.IEventQuery;

public class LostCitySupport {

//    private static ILostCities lostCities;

    public static void register() {
        // @todo 1.15
//        FMLInterModComms.sendFunctionMessage("lostcities", "getLostCities", "mcjty.incontrol.compat.LostCitySupport$GetLostCities");
    }


    public static <T> boolean isCity(IEventQuery<T> query, T event) {
        // @todo 1.15
//        World world = query.getWorld(event);
//        if (world.isRemote) {
//            return false;   // This test don't work client side
//        }
//        ILostChunkGenerator generator = lostCities.getLostGenerator(world.provider.getDimension());
//        if (generator != null) {
//            BlockPos pos = query.getPos(event);
//            ILostChunkInfo chunkInfo = generator.getChunkInfo(pos.getX() >> 4, pos.getZ() >> 4);
//            return chunkInfo.isCity();
//        }
        return false;
    }

    public static <T> boolean isStreet(IEventQuery<T> query, T event) {
        // @todo 1.15
//        World world = query.getWorld(event);
//        if (world.isRemote) {
//            return false;   // This test don't work client side
//        }
//        ILostChunkGenerator generator = lostCities.getLostGenerator(world.provider.getDimension());
//        if (generator != null) {
//            BlockPos pos = query.getPos(event);
//            ILostChunkInfo chunkInfo = generator.getChunkInfo(pos.getX() >> 4, pos.getZ() >> 4);
//            return chunkInfo.isCity() && chunkInfo.getBuildingType() == null;
//        }
        return false;
    }

    public static <T> boolean inSphere(IEventQuery<T> query, T event) {
        // @todo 1.15
//        World world = query.getWorld(event);
//        if (world.isRemote) {
//            return false;   // This test don't work client side
//        }
//        ILostChunkGenerator generator = lostCities.getLostGenerator(world.provider.getDimension());
//        if (generator != null) {
//            BlockPos pos = query.getPos(event);
//            ILostChunkInfo chunkInfo = generator.getChunkInfo(pos.getX() >> 4, pos.getZ() >> 4);
//            return chunkInfo.getSphere() != null;
//        }
        return false;
    }

    public static <T> boolean isBuilding(IEventQuery<T> query, T event) {
        // @todo 1.15
//        World world = query.getWorld(event);
//        if (world.isRemote) {
//            return false;   // This test don't work client side
//        }
//        ILostChunkGenerator generator = lostCities.getLostGenerator(world.provider.getDimension());
//        if (generator != null) {
//            BlockPos pos = query.getPos(event);
//            ILostChunkInfo chunkInfo = generator.getChunkInfo(pos.getX() >> 4, pos.getZ() >> 4);
//            return chunkInfo.isCity() && chunkInfo.getBuildingType() != null;
//        }
        return false;
    }

    // @todo 1.15
//    public static class GetLostCities implements Function<ILostCities, Void> {
//        @Nullable
//        @Override
//        public Void apply(ILostCities lc) {
//            lostCities = lc;
//            return null;
//        }
//    }
//
}

