package mcjty.incontrol.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.incontrol.ErrorHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AISystem {

    enum Cmd {
        GOALS,
        TARGETS
    }

    enum Goals {
        LOOK_AT_PLAYER,
        HURT_BY_TARGET,
        NEAREST_ATTACKABLE_TARGET,
        MELEE_ATTACK,
        RANDOM_LOOK_AROUND,
        AVOID_ENTITY_GOAL
    }

    private static final Map<String, Cmd> ACTIONS = new HashMap<>();
    static {
        for (Cmd cmd : Cmd.values()) {
            ACTIONS.put(cmd.name().toLowerCase(), cmd);
        }
    }

    private static final Map<String, Goals> GOALS = new HashMap<>();
    static {
        for (Goals goal : Goals.values()) {
            GOALS.put(goal.name().toLowerCase(), goal);
        }
    }

    public static Consumer<Mob> parse(String json) {
        Consumer<Mob> action = null;

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        if (!element.isJsonObject()) {
            ErrorHandler.error("Invalid ai action! Expected a JSON object.");
            return e -> {};
        }
        JsonObject object = element.getAsJsonObject();

        for (String attr : object.keySet()) {
            Cmd cmd = ACTIONS.get(attr);
            if (cmd == null) {
                ErrorHandler.error("Invalid action '" + attr + "' for ai rule!");
                return e -> {};
            }
            JsonElement value = object.get(attr);
            switch (cmd) {
                case GOALS:
                    if (value.isJsonArray()) {
                        for (JsonElement el : value.getAsJsonArray()) {
                            BiConsumer<GoalSelector, Mob> consumer = parseGoal(el);
                            action = combineConsumer(action, e -> consumer.accept(e.goalSelector, e));
                        }
                    } else {
                        ErrorHandler.error("Invalid value for 'goals' action! Expected a JSON array.");
                        return combineConsumer(action, e -> {});
                    }
                    break;
                case TARGETS:
                    if (value.isJsonArray()) {
                        for (JsonElement el : value.getAsJsonArray()) {
                            BiConsumer<GoalSelector, Mob> consumer = parseGoal(el);
                            action = combineConsumer(action, e -> consumer.accept(e.targetSelector, e));
                        }
                    } else {
                        ErrorHandler.error("Invalid value for 'targets' action! Expected a JSON array.");
                        return combineConsumer(action, e -> {});
                    }
                    break;
            }
        }
        return combineConsumer(action, e -> {});
    }

    private static int getDefaultInt(JsonObject object, String key, int defaultValue) {
        if (object.has(key)) {
            JsonElement element = object.get(key);
            return element.getAsInt();
        }
        return defaultValue;
    }

    private static float getDefaultFloat(JsonObject object, String key, float defaultValue) {
        if (object.has(key)) {
            JsonElement element = object.get(key);
            return element.getAsFloat();
        }
        return defaultValue;
    }

    private static double getDefaultDouble(JsonObject object, String key, double defaultValue) {
        if (object.has(key)) {
            JsonElement element = object.get(key);
            return element.getAsDouble();
        }
        return defaultValue;
    }

    private static boolean getDefaultBoolean(JsonObject object, String key, boolean defaultValue) {
        if (object.has(key)) {
            JsonElement element = object.get(key);
            return element.getAsBoolean();
        }
        return defaultValue;
    }

    private static Class getEntityClass(JsonObject object, String key, Class defaultClass) {
        if (!object.has(key)) {
            return defaultClass;
        }
        JsonElement element = object.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            ErrorHandler.error("Invalid entity class in '" + key + "' action! Expected a string.");
            return null;
        }
        String entityName = element.getAsString();
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityName));
        if (entityType == null) {
            ErrorHandler.error("Unknown entity '" + entityName + "'!");
            return null;
        }
        return entityType.getBaseClass();
    }

    private static BiConsumer<GoalSelector, Mob> parseGoal(JsonElement el) {
        if (!el.isJsonObject()) {
            ErrorHandler.error("Invalid goal in 'goals' action! Expected a JSON object.");
            return (s, e) -> {};
        }
        JsonObject goal = el.getAsJsonObject();
        // If it has the atttributes 'removeall' then we remove all goals in this action
        if (goal.has("removeall")) {
            return (s, e) -> e.goalSelector.removeAllGoals(g -> true);
        }
        // It has the attributes 'goal' and 'priority'. Then specific attributes for the goal.
        if (!goal.has("goal") || !goal.has("priority")) {
            ErrorHandler.error("Invalid goal in 'goals' action! Expected 'goal' and 'priority' attributes.");
            return (s, e) -> {};
        }
        BiConsumer<GoalSelector, Mob> goalSelector = null;
        String goalName = goal.get("goal").getAsString();
        int priority = goal.get("priority").getAsInt();
        Goals g = GOALS.get(goalName.toLowerCase());
        if (g == null) {
            ErrorHandler.error("Invalid goal '" + goalName + "' in 'goals' action!");
            return (s, e) -> {};
        }
        switch (g) {
            case LOOK_AT_PLAYER: {
                float lookdistance = getDefaultFloat(goal, "lookdistance", 8.0f);
                float probability = getDefaultFloat(goal, "probability", 0.02f);
                boolean onlyHorizontal = getDefaultBoolean(goal, "onlyhorizontal", false);
                return combineConsumer(goalSelector, (s, e) -> s.addGoal(priority, new LookAtPlayerGoal(e, Player.class, lookdistance, probability, onlyHorizontal)));
            }
            case HURT_BY_TARGET:
                return combineConsumer(goalSelector, (s, e) -> {
                    if (e instanceof PathfinderMob pf) {
                        s.addGoal(priority, new HurtByTargetGoal(pf));
                    }
                });
            case NEAREST_ATTACKABLE_TARGET: {
                boolean mustsee = getDefaultBoolean(goal, "mustsee", true);
                boolean mustreach = getDefaultBoolean(goal, "mustreach", false);
                int randomInterval = getDefaultInt(goal, "randominterval", 10);
                return combineConsumer(goalSelector, (s, e) -> s.addGoal(priority, new NearestAttackableTargetGoal(e, Player.class, randomInterval, mustsee, mustreach, null)));
            }
            case MELEE_ATTACK: {
                float speed = getDefaultFloat(goal, "speed", 1.0f);
                boolean followingTargetEvenNotSeen = getDefaultBoolean(goal, "followingtarget", false);
                return combineConsumer(goalSelector, (s, e) -> {
                    if (e instanceof PathfinderMob pf) {
                        s.addGoal(priority, new MeleeAttackGoal(pf, speed, followingTargetEvenNotSeen));
                    }
                });
            }
            case RANDOM_LOOK_AROUND: {
                return combineConsumer(goalSelector, (s, e) -> s.addGoal(priority, new RandomLookAroundGoal(e)));
            }
            case AVOID_ENTITY_GOAL: {
                Class entityClass = getEntityClass(goal, "entity", Player.class);
                if (entityClass == null) {
                    return (s, e) -> {};
                }
                float maxDistance = getDefaultFloat(goal, "maxdistance", 10.0f);
                double walkSpeedModifier = getDefaultDouble(goal, "walkspeedmodifier", 1.0f);
                double sprintSpeedModifier = getDefaultDouble(goal, "sprintspeedmodifier", 1.2f);
                boolean onlyWhenTargeting = getDefaultBoolean(goal, "onlyWhenTargeting", false);
                return combineConsumer(goalSelector, (s, e) -> {
                    if (e instanceof PathfinderMob pf) {
                        s.addGoal(priority, new AvoidEntityGoal<>(pf, entityClass, maxDistance, walkSpeedModifier, sprintSpeedModifier));
                    }
                });
            }
            default:
                ErrorHandler.error("Unknown goal '" + g + "' in 'goals' action!");
                return (s, e) -> {};
        }
    }

    private static <T> Consumer<T> combineConsumer(@Nullable Consumer<T> first, @Nonnull Consumer<T> second) {
        if (first == null) {
            return second;
        } else {
            return first.andThen(second);
        }
    }

    private static <T,S> BiConsumer<T,S> combineConsumer(@Nullable BiConsumer<T, S> first, @Nonnull BiConsumer<T, S> second) {
        if (first == null) {
            return second;
        } else {
            return first.andThen(second);
        }
    }
}
