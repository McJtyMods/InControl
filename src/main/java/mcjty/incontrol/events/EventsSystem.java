package mcjty.incontrol.events;

import mcjty.incontrol.spawner.SpawnerSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class EventsSystem {

    // All rules
    private static final Map<EventType.Type, List<EventsRule>> rules = new HashMap<>();

    // A map indexed by mob type that contains all rules for that mob type
    private static final Map<ResourceLocation, List<EventsRule>> rulesByMob = new HashMap<>();

    private static final Random rnd = new Random();

    public static Mob busySpawning = null;

    public static void reloadRules() {
        rules.clear();
        rulesByMob.clear();
        EventsParser.readRules("events.json");
    }

    public static void addRule(EventsRule rule) {
        rules.computeIfAbsent(rule.getEventType().type(), k -> new ArrayList<>()).add(rule);
        if (rule.getEventType().type() == EventType.Type.MOB_KILLED) {
            for (ResourceLocation mob : rule.getAction().mobid()) {
                rulesByMob.computeIfAbsent(mob, k -> new ArrayList<>()).add(rule);
            }
        }
    }

    public static void onEntityKilled(LivingEntity entity) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        List<EventsRule> rules = EventsSystem.rulesByMob.get(key);
        if (rules != null) {
            for (EventsRule rule : rules) {
                EventsConditions conditions = rule.getConditions();
                float random = conditions.getRandom();
                if (random >= 0 && rnd.nextFloat() >= random) {
                    continue;
                }
                Set<ResourceKey<Level>> dimensions = conditions.getDimensions();
                if (dimensions.isEmpty() || dimensions.contains(entity.level().dimension())) {
                    SpawnEventAction action = rule.getAction();
                    List<ResourceLocation> mobs = action.mobid();
                    // Pick a random mob
                    ResourceLocation mob = mobs.get(rnd.nextInt(mobs.size()));
                    EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(mob);
                    if (entityType != null) {
                        // Get a random count
                        int count = action.minamount() + rnd.nextInt(action.maxamount() - action.minamount() + 1);
                        for (int i = 0 ; i < count ; i++) {
                            for (int a = 0 ; a < action.attempts() ; a++) {
                                BlockPos randomPos = getRandomPos(entity.blockPosition(), action.mindistance(), action.maxdistance());
                                if (spawn(entityType, conditions, (ServerLevel) entity.level(), randomPos)) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static BlockPos getRandomPos(BlockPos center, float mindistance, float maxdistance) {
        float distance = mindistance + rnd.nextFloat() * (maxdistance - mindistance);
        double angle = rnd.nextFloat() * Math.PI * 2.0;
        int x = (int) (center.getX() + distance * Math.cos(angle));
        int z = (int) (center.getZ() + distance * Math.sin(angle));
        return new BlockPos(x, center.getY(), z);
    }

    private static boolean spawn(EntityType<?> entityType, EventsConditions conditions, ServerLevelAccessor world, BlockPos pos) {
        Entity entity = entityType.create(world.getLevel());
        Mob mobEntity = (Mob) entity;
        entity.moveTo(pos.getX(), pos.getY(), pos.getZ(), rnd.nextFloat() * 360.0F, 0.0F);
        busySpawning = mobEntity;
        if (canSpawn(world.getLevel(), mobEntity, conditions) && isNotColliding(world.getLevel(), mobEntity, conditions)) {
            ForgeEventFactory.onFinalizeSpawn(mobEntity, world, world.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null, null);
            busySpawning = null;
            if (!((Mob) entity).isSpawnCancelled()) {
                world.addFreshEntityWithPassengers(entity);
                busySpawning = null;
                return true;
//                Statistics.addSpawnerStat(ruleNr);
//                spawned++;
//                if (groupCenterPos == null) {
//                    groupCenterPos = pos;
//                }
//                if (spawned >= desiredAmount) {
//                    return;
//                }
            }
        }
        busySpawning = null;
        return false;
    }

    private static boolean canSpawn(Level world, Mob mobEntity, EventsConditions conditions) {
//        if (conditions.isNoRestrictions()) {
//            return true;
//        } else {
            return ForgeEventFactory.checkSpawnPosition(mobEntity, (ServerLevelAccessor) world, MobSpawnType.NATURAL);
//        }
    }

    private static boolean isNotColliding(Level world, Mob mobEntity, EventsConditions conditions) {
//        if (conditions.isInLiquid()) {
//            return world.containsAnyLiquid(mobEntity.getBoundingBox()) && world.isUnobstructed(mobEntity);
//        } else if (conditions.isInWater()) {
//            return containsLiquid(world, mobEntity.getBoundingBox(), FluidTags.WATER);
//        } else if (conditions.isInLava()) {
//            return containsLiquid(world, mobEntity.getBoundingBox(), FluidTags.LAVA);
//        } else {
            return mobEntity.checkSpawnObstruction(world);
//        }
    }


}
