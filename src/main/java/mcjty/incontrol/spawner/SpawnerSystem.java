package mcjty.incontrol.spawner;

import mcjty.incontrol.InControl;
import mcjty.incontrol.data.DataStorage;
import mcjty.incontrol.data.Statistics;
import mcjty.incontrol.tools.varia.Box;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.util.*;

public class SpawnerSystem {

    private static final Map<ResourceKey<Level>, WorldSpawnerData> worldData = new HashMap<>();

    private static final Random random = new Random();

    public static Mob busySpawning = null;

    public static void reloadRules() {
        worldData.clear();
        SpawnerParser.readRules("spawner.json");
    }

    public static void addRule(SpawnerRule rule) {
        for (ResourceKey<Level> dimension : rule.getConditions().getDimensions()) {
            worldData.computeIfAbsent(dimension, key -> new WorldSpawnerData()).rules.add(rule);
        }
    }

    public static void checkRules(TickEvent.LevelTickEvent event) {
        Level world = event.level;
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
            int i = 0;
            for (SpawnerRule rule : spawnerData.rules) {
                executeRule(i, rule, world, data);
                i++;
            }
        }
    }

    private static void executeRule(int ruleNr, SpawnerRule rule, Level world, DataStorage data) {
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

        if (rule.getMobsFromBiome() != null) {
            executeRule(ruleNr, rule, (ServerLevel) world, null, rule.getMobsFromBiome(), 1.0f);
        } else {
            List<EntityType<?>> mobs = rule.getMobs();
            List<Float> weights = rule.getWeights();
            float maxWeight = rule.getMaxWeight();
            for (int i = 0; i < mobs.size(); i++) {
                EntityType<?> mob = mobs.get(i);
                float weight = i < weights.size() ? weights.get(i) : 1.0f;
                executeRule(ruleNr, rule, (ServerLevel) world, mob, null, weight / maxWeight);
            }
        }
    }

    // Note: if 'mob' is null we spawn a random mob from the biome spawn list
    private static void executeRule(int ruleNr, SpawnerRule rule, ServerLevel world, @Nullable EntityType<?> mob, @Nullable MobCategory classification, float weight) {
        if (random.nextFloat() > rule.getPersecond()) {
            return;
        }

        if (random.nextFloat() > weight) {
            return;
        }

        SpawnerConditions conditions = rule.getConditions();

        if (mob != null) {
            if (checkTooMany(world, mob, conditions)) {
                return;
            }
        }

        int minspawn = rule.getMinSpawn();
        int maxspawn = rule.getMaxSpawn();
        int desiredAmount = minspawn + ((minspawn == maxspawn) ? 0 : random.nextInt(maxspawn-minspawn));
        int spawned = 0;

        for (int i = 0 ; i < rule.getAttempts() ; i++) {
            BlockPos pos = getRandomPosition(world, mob, conditions);
            if (pos != null) {
                if (world.hasChunkAt(pos)) {
                    EntityType<?> spawnable = selectMob(world, mob, classification, conditions, pos);
                    if (spawnable == null) {
                        return;
                    }
                    boolean nocollisions = world.noCollision(spawnable.getAABB(pos.getX(), pos.getY(), pos.getZ()));
                    if (nocollisions) {
                        Entity entity = spawnable.create(world);
                        if (entity instanceof Mob) {
                            if (!(entity instanceof Enemy) || world.getDifficulty() != Difficulty.PEACEFUL) {
                                Mob mobEntity = (Mob) entity;
                                entity.moveTo(pos.getX(), pos.getY(), pos.getZ(), random.nextFloat() * 360.0F, 0.0F);
                                busySpawning = mobEntity;   // @todo check in spawn rule
                                int result = ForgeHooks.canEntitySpawn(mobEntity, world, pos.getX(), pos.getY(), pos.getZ(), null, MobSpawnType.NATURAL);
                                busySpawning = null;
                                if (result != -1) {
                                    if (canSpawn(world, mobEntity, conditions) && isNotColliding(world, mobEntity, conditions)) {
                                        if (!ForgeEventFactory.doSpecialSpawn(mobEntity, (LevelAccessor) world, pos.getX(), pos.getY(), pos.getZ(), null, MobSpawnType.NATURAL)) {
                                            mobEntity.finalizeSpawn(world, world.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.NATURAL, null, null);
                                            world.addFreshEntityWithPassengers(entity);
                                            Statistics.addSpawnerStat(ruleNr);
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
    }

    private static EntityType<?> selectMob(ServerLevel world, EntityType<?> mob, MobCategory classification, SpawnerConditions conditions, BlockPos pos) {
        EntityType<?> spawnable = mob;
        if (spawnable == null && classification != null) {
            WeightedRandomList<MobSpawnSettings.SpawnerData> spawners = world.getBiome(pos).value().getMobSettings().getMobs(classification);
            if (spawners.isEmpty()) {
                return null;
            }
            spawnable = spawners.getRandom(world.random).map(item -> {
                EntityType<?> type = item.type;
                if (checkTooMany(world, type, conditions)) {
                    return null;
                } else {
                    return type;
                }
            }).orElse(null);
        }
        return spawnable;
    }

    private static boolean checkTooMany(ServerLevel world, EntityType<?> mob, SpawnerConditions conditions) {
        if (conditions.getMaxthis() != -1) {
            int count = InControl.setup.cache.getCount(world, mob);
            if (count >= conditions.getMaxthis()) {
                return true;
            }
        }
        return false;
    }

    private static boolean canSpawn(Level world, Mob mobEntity, SpawnerConditions conditions) {
        if (conditions.isNoRestrictions()) {
            return true;
        } else {
            return mobEntity.checkSpawnRules(world, MobSpawnType.NATURAL);
        }
    }

    private static boolean isNotColliding(Level world, Mob mobEntity, SpawnerConditions conditions) {
        if (conditions.isInLiquid()) {
            return world.containsAnyLiquid(mobEntity.getBoundingBox()) && world.isUnobstructed(mobEntity);
        } else if (conditions.isInWater()) {
            return containsLiquid(world, mobEntity.getBoundingBox(), FluidTags.WATER);
        } else if (conditions.isInLava()) {
            return containsLiquid(world, mobEntity.getBoundingBox(), FluidTags.LAVA);
        } else {
            return mobEntity.checkSpawnObstruction(world);
        }
    }

    private static boolean containsLiquid(Level world, AABB box, TagKey<Fluid> liquid) {
        int x1 = Mth.floor(box.minX);
        int x2 = Mth.ceil(box.maxX);
        int y1 = Mth.floor(box.minY);
        int y2 = Mth.ceil(box.maxY);
        int z1 = Mth.floor(box.minZ);
        int z2 = Mth.ceil(box.maxZ);
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

        for(int x = x1; x < x2; ++x) {
            for(int y = y1; y < y2; ++y) {
                for(int z = z1; z < z2; ++z) {
                    BlockState blockstate = world.getBlockState(mpos.set(x, y, z));
                    if (blockstate.getFluidState().is(liquid)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Nullable
    private static BlockPos getRandomPosition(Level world, EntityType<?> mob, SpawnerConditions conditions) {
        boolean inAir = conditions.isInAir();
        boolean inWater = conditions.isInWater();
        boolean inLava = conditions.isInLava();
        boolean inLiquid = conditions.isInLiquid();

        if (inAir || inWater || inLava || inLiquid) {
            return getRandomPositionInBox(world, mob, conditions);
        } else {
            return getRandomPositionOnGround(world, mob, conditions);
        }
    }

    @Nullable
    private static BlockPos getRandomPositionInBox(Level world, EntityType<?> mob, SpawnerConditions conditions) {
        List<? extends Player> players = world.players();
        Player player = players.get(random.nextInt(players.size()));

        Box box = createSpawnBox(conditions, player.blockPosition());

        if (!box.isValid()) {
            return null;
        }

        if (checkLocalCount((ServerLevel) world, mob, conditions, box)) {
            return null;
        }

        int mindist = conditions.getMindist();
        int maxdist = conditions.getMaxdist();

        BlockPos pos = null;
        double sqdist = Double.MAX_VALUE;

        int counter = 40;
        while (pos == null || sqdist < mindist * mindist || sqdist > maxdist * maxdist) {
            pos = box.randomPos(random);
            LevelChunk c = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (c != null && c.getStatus() == ChunkStatus.FULL) {
                sqdist = pos.distToCenterSqr(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ());
            }
            counter--;
            if (counter <= 0) {
                return null;
            }
        }

        return pos;
    }

    @Nullable
    private static BlockPos getRandomPositionOnGround(Level world, EntityType<?> mob, SpawnerConditions conditions) {
        List<? extends Player> players = world.players();
        Player player = players.get(random.nextInt(players.size()));

        Box box = createSpawnBox(conditions, player.blockPosition());

        if (!box.isValid()) {
            return null;
        }

        if (checkLocalCount((ServerLevel) world, mob, conditions, box)) {
            return null;
        }

        int minheight = conditions.getMinheight();
        int maxheight = conditions.getMaxheight();

        int mindist = conditions.getMindist();
        int maxdist = conditions.getMaxdist();

        BlockPos pos = null;
        double sqdist = Double.MAX_VALUE;

        int counter = 40;
        while (pos == null || sqdist < mindist * mindist || sqdist > maxdist * maxdist) {
            pos = box.randomPos(random);
            LevelChunk c = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (c != null && c.getStatus() == ChunkStatus.FULL) {
                pos = getValidSpawnablePosition(world, pos.getX(), pos.getZ(), minheight, maxheight);
                sqdist = pos == null ? Double.MAX_VALUE : pos.distToCenterSqr(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ());
            }
            counter--;
            if (counter <= 0) {
                return null;
            }
        }

        return pos;
    }

    private static boolean checkLocalCount(ServerLevel world, EntityType<?> mob, SpawnerConditions conditions, Box box) {
        if (conditions.getMaxlocal() != -1) {
            LevelEntityGetter<Entity> entities = world.getEntities();
            long count = 0;
            for (Entity entity : entities.getAll()) {
                if (entity.getType() == mob && box.in(entity.blockPosition())) {
                    count++;
                    if (count >= conditions.getMaxlocal()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Box createSpawnBox(SpawnerConditions conditions, BlockPos center) {
        int maxdist = conditions.getMaxdist();
        int minX = center.getX() - (maxdist+1);
        int minY = center.getY() - (maxdist+1);
        int minZ = center.getZ() - (maxdist+1);
        int maxX = center.getX() + (maxdist+1);
        int maxY = center.getY() + (maxdist+1);
        int maxZ = center.getZ() + (maxdist+1);
        minY = Math.max(minY, conditions.getMinheight()-1);
        maxY = Math.min(maxY, conditions.getMaxheight()+1);
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static BlockPos getValidSpawnablePosition(LevelReader worldIn, int x, int z, int minHeight, int maxHeight) {
        int height = worldIn.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
        height = Math.min(height, maxHeight);
        int minBuildHeight = worldIn.getMinBuildHeight();
        height = random.nextInt(height + 1 - minBuildHeight) + minBuildHeight;
        BlockPos blockPos = new BlockPos(x, height-1, z);
        while (blockPos.getY() >= minHeight && !isValidSpawnPos(worldIn, blockPos)) {
            blockPos = blockPos.below();
        }
        return blockPos.getY() < minHeight ? null : blockPos;
    }

    private static boolean isValidSpawnPos(LevelReader world, BlockPos pos) {
        if (!world.getBlockState(pos).isPathfindable(world, pos, PathComputationType.LAND)) {
            return false;
        }
        return world.getBlockState(pos.below()).canOcclude();
    }


    public static class WorldSpawnerData {
        private final List<SpawnerRule> rules = new ArrayList<>();
        private int counter = 1;
    }

}
