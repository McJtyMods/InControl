package mcjty.incontrol.spawner;

import mcjty.incontrol.data.DataStorage;
import mcjty.incontrol.InControl;
import mcjty.tools.varia.Box;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.util.*;

public class SpawnerSystem {

    private static Map<RegistryKey<World>, WorldSpawnerData> worldData = new HashMap<>();

    private static Random random = new Random();

    public static MobEntity busySpawning = null;

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
        WorldSpawnerData spawnerData = worldData.get(world.dimension());
        if (spawnerData == null) {
            return;
        }
        if (spawnerData.rules.isEmpty()) {
            return;
        }

        spawnerData.counter--;
        if (spawnerData.counter <= 0) {
            spawnerData.counter = 20;
            DataStorage data = DataStorage.getData(world);
            for (SpawnerRule rule : spawnerData.rules) {
                executeRule(rule, world, data);
            }
        }
    }

    private static void executeRule(SpawnerRule rule, World world, DataStorage data) {
        if (!data.getPhases().containsAll(rule.getPhases())) {
            return;
        }
        SpawnerConditions conditions = rule.getConditions();
        if (conditions.getMaxtotal() != -1) {
            int count = InControl.setup.cache.getCountHostile(world);
            count += InControl.setup.cache.getCountPassive(world);
            count += InControl.setup.cache.getCountNeutral(world);
            if (count >= conditions.getMaxtotal()) {
                return;
            }
        }
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

        int daycounter = data.getDaycounter();
        if (daycounter < conditions.getMindaycount() && daycounter >= conditions.getMaxdaycount()) {
            return;
        }

        List<EntityType<?>> mobs = rule.getMobs();
        List<Float> weights = rule.getWeights();
        float maxWeight = rule.getMaxWeight();
        for (int i = 0 ; i < mobs.size() ; i++) {
            EntityType<?> mob = mobs.get(i);
            float weight = i < weights.size() ? weights.get(i) : 1.0f;
            executeRule(rule, (ServerWorld)world, mob, weight / maxWeight);
        }
    }

    private static void executeRule(SpawnerRule rule, ServerWorld world, EntityType<?> mob, float weight) {
        if (random.nextFloat() > rule.getPersecond()) {
            return;
        }

        if (random.nextFloat() > weight) {
            return;
        }

        SpawnerConditions conditions = rule.getConditions();

        if (checkTooMany(world, mob, conditions)) {
            return;
        }

        int minspawn = rule.getMinSpawn();
        int maxspawn = rule.getMaxSpawn();
        int desiredAmount = minspawn + ((minspawn == maxspawn) ? 0 : random.nextInt(maxspawn-minspawn));
        int spawned = 0;

        for (int i = 0 ; i < rule.getAttempts() ; i++) {
            BlockPos pos = getRandomPosition(world, mob, conditions);
            if (pos != null) {
                if (world.hasChunkAt(pos)) {
                    boolean nocollisions = world.noCollision(mob.getAABB(pos.getX(), pos.getY(), pos.getZ()));
                    if (nocollisions) {
                        Entity entity = mob.create(world);
                        if (entity instanceof MobEntity) {
                            if (!(entity instanceof IMob) || world.getDifficulty() != Difficulty.PEACEFUL) {
                                MobEntity mobEntity = (MobEntity) entity;
                                entity.moveTo(pos.getX(), pos.getY(), pos.getZ(), random.nextFloat() * 360.0F, 0.0F);
                                busySpawning = mobEntity;   // @todo check in spawn rule
                                int result = ForgeHooks.canEntitySpawn(mobEntity, world, pos.getX(), pos.getY(), pos.getZ(), null, SpawnReason.NATURAL);
                                busySpawning = null;
                                if (result != -1) {
                                    if (canSpawn(world, mobEntity, conditions) && isNotColliding(world, mobEntity, conditions)) {
                                        mobEntity.finalizeSpawn(world, world.getCurrentDifficultyAt(entity.blockPosition()), SpawnReason.NATURAL, null, null);
                                        world.addFreshEntityWithPassengers(entity);
                                        spawned++;
                                        if (spawned >= desiredAmount) {
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean checkTooMany(ServerWorld world, EntityType<?> mob, SpawnerConditions conditions) {
        if (conditions.getMaxthis() != -1) {
            int count = InControl.setup.cache.getCount(world, mob);
            if (count >= conditions.getMaxthis()) {
                return true;
            }
        }
        return false;
    }

    private static boolean canSpawn(World world, MobEntity mobEntity, SpawnerConditions conditions) {
        if (conditions.isNoRestrictions()) {
            return true;
        } else {
            return mobEntity.checkSpawnRules(world, SpawnReason.NATURAL);
        }
    }

    private static boolean isNotColliding(World world, MobEntity mobEntity, SpawnerConditions conditions) {
        if (conditions.isInWater()) {
            return world.containsAnyLiquid(mobEntity.getBoundingBox()) && world.isUnobstructed(mobEntity);
        } else {
            return mobEntity.checkSpawnObstruction(world);
        }
    }


    @Nullable
    private static BlockPos getRandomPosition(World world, EntityType<?> mob, SpawnerConditions conditions) {
        boolean inAir = conditions.isInAir();
        boolean inWater = conditions.isInWater();

        if (inAir || inWater) {
            return getRandomPositionInBox(world, mob, conditions);
        } else {
            return getRandomPositionOnGround(world, mob, conditions);
        }
    }

    @Nullable
    private static BlockPos getRandomPositionInBox(World world, EntityType<?> mob, SpawnerConditions conditions) {
        List<? extends PlayerEntity> players = world.players();
        PlayerEntity player = players.get(random.nextInt(players.size()));

        int mindist = conditions.getMindist();
        int maxdist = conditions.getMaxdist();
        Box box = createSpawnBox(conditions, player.blockPosition());

        if (!box.isValid()) {
            return null;
        }

        if (checkLocalCount((ServerWorld) world, mob, conditions, box)) {
            return null;
        }

        BlockPos pos = box.randomPos(random);
        double sqdist = pos.distSqr(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ(), true);

        while (sqdist < mindist * mindist || sqdist > maxdist * maxdist) {
            pos = box.randomPos(random);
            sqdist = pos.distSqr(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ(), true);
        }

        return pos;
    }

    @Nullable
    private static BlockPos getRandomPositionOnGround(World world, EntityType<?> mob, SpawnerConditions conditions) {
        List<? extends PlayerEntity> players = world.players();
        PlayerEntity player = players.get(random.nextInt(players.size()));

        int minheight = conditions.getMinheight();
        int maxheight = conditions.getMaxheight();

        int mindist = conditions.getMindist();
        int maxdist = conditions.getMaxdist();
        Box box = createSpawnBox(conditions, player.blockPosition());

        if (!box.isValid()) {
            return null;
        }

        if (checkLocalCount((ServerWorld) world, mob, conditions, box)) {
            return null;
        }

        BlockPos pos = box.randomPos(random);
        pos = getValidSpawnablePosition(world, pos.getX(), pos.getZ(), minheight, maxheight);
        double sqdist = pos == null ? Double.MAX_VALUE : pos.distSqr(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ(), true);

        int counter = 100;
        while (sqdist < mindist * mindist || sqdist > maxdist * maxdist) {
            pos = box.randomPos(random);
            pos = getValidSpawnablePosition(world, pos.getX(), pos.getZ(), minheight, maxheight);
            sqdist = pos == null ? Double.MAX_VALUE : pos.distSqr(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ(), true);
            counter--;
            if (counter <= 0) {
                return null;
            }
        }

        return pos;
    }

    private static boolean checkLocalCount(ServerWorld world, EntityType<?> mob, SpawnerConditions conditions, Box box) {
        if (conditions.getMaxlocal() != -1) {
            long count = world.getEntities().filter(e -> e.getType() == mob && box.in(e.blockPosition())).count();
            if (count >= conditions.getMaxlocal()) {
                return true;
            }
        }
        return false;
    }

    private static Box createSpawnBox(SpawnerConditions conditions, BlockPos center) {
        int maxdist = conditions.getMaxdist();
        return Box.create()
                .center(center, maxdist, maxdist, maxdist)
                .clampY(conditions.getMinheight(), conditions.getMaxheight())
                .build();
    }

    private static BlockPos getValidSpawnablePosition(IWorldReader worldIn, int x, int z, int minHeight, int maxHeight) {
        int height = worldIn.getHeight(Heightmap.Type.WORLD_SURFACE, x, z) + 1;
        height = Math.min(height, maxHeight);
        height = random.nextInt(height + 1);
        BlockPos blockPos = new BlockPos(x, height-1, z);
        while (blockPos.getY() >= minHeight && !isValidSpawnPos(worldIn, blockPos)) {
            blockPos = blockPos.below();
        }
        return blockPos.getY() < minHeight ? null : blockPos;
    }

    private static boolean isValidSpawnPos(IWorldReader world, BlockPos pos) {
        if (!world.getBlockState(pos).isPathfindable(world, pos, PathType.LAND)) {
            return false;
        }
        return world.getBlockState(pos.below()).canOcclude();
    }


    public static class WorldSpawnerData {
        private final List<SpawnerRule> rules = new ArrayList<>();
        private int counter = 1;
    }

}
