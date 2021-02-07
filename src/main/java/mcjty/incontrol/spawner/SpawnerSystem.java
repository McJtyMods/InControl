package mcjty.incontrol.spawner;

import mcjty.incontrol.InControl;
import net.minecraft.entity.EntityType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.TickEvent;

import java.util.*;

public class SpawnerSystem {

    private static Map<RegistryKey<World>, WorldSpawnerData> worldData = new HashMap<>();

    public static void reloadRules() {
        worldData.clear();
        SpawnerParser.readRules("spawner.json");
    }

    public static void addRule(SpawnerRule rule) {
        for (RegistryKey<World> dimension : rule.getConditions().getDimensions()) {
            worldData.computeIfAbsent(dimension, key -> new WorldSpawnerData()).rules.add(rule);
        }
    }

    public static void checkRules(TickEvent.WorldTickEvent event) {
        World world = event.world;
        WorldSpawnerData spawnerData = worldData.get(world.getDimensionKey());
        if (spawnerData == null) {
            return;
        }
        if (spawnerData.rules.isEmpty()) {
            return;
        }

        spawnerData.counter--;
        if (spawnerData.counter <= 0) {
            spawnerData.counter = 20;
            for (SpawnerRule rule : spawnerData.rules) {
                executeRule(rule, world);
            }
        }
    }

    private static void executeRule(SpawnerRule rule, World world) {
        SpawnerConditions conditions = rule.getConditions();
        if (conditions.getMaxhostile() != -1) {
            int count = InControl.setup.cache.getCountHostile(world);
            if (count >= conditions.getMaxhostile()) {
                return;
            }
        }
        if (conditions.getMaxpeaceful() != -1) {
            int count = InControl.setup.cache.getCountPassive(world);
            if (count >= conditions.getMaxpeaceful()) {
                return;
            }
        }
        if (conditions.getMaxneutral() != -1) {
            int count = InControl.setup.cache.getCountNeutral(world);
            if (count >= conditions.getMaxneutral()) {
                return;
            }
        }

        List<EntityType<?>> mobs = rule.getMobs();
        for (EntityType<?> mob : mobs) {
            executeRule(rule, world, mob);
        }
    }

    private static void executeRule(SpawnerRule rule, World world, EntityType<?> mob) {
        SpawnerConditions conditions = rule.getConditions();
        if (conditions.getMaxthis() != -1) {
            int count = InControl.setup.cache.getCount(world, mob);
            if (count >= conditions.getMaxthis()) {
                return;
            }
        }
        for (int i = 0 ; i < rule.getAttempts() ; i++) {
            if (conditions.isInAir()) {

            }
        }
    }

    private static BlockPos getValidSpawnablePosition(Random random, IWorldReader worldIn, int x, int z) {
        int height = worldIn.getHeight(Heightmap.Type.WORLD_SURFACE, x, z) + 1;
        height = random.nextInt(height + 1);
        BlockPos blockPos = new BlockPos(x, height-1, z);
        while (blockPos.getY() > 1 && !isValidSpawnPos(worldIn, blockPos)) {
            blockPos = blockPos.down();
        }
        return blockPos;
    }

    private static boolean isValidSpawnPos(IWorldReader world, BlockPos pos) {
        if (!world.getBlockState(pos).allowsMovement(world, pos, PathType.LAND)) {
            return false;
        }
        return world.getBlockState(pos.down()).isSolid();
    }


    public static class WorldSpawnerData {
        private final List<SpawnerRule> rules = new ArrayList<>();
        private int counter = 1;
    }

}
