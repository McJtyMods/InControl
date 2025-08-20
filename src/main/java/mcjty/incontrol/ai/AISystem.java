package mcjty.incontrol.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.incontrol.ErrorHandler;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
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
        CLEARGOALS,
        CLEARTARGETS,
        GOALS,
        TARGETS
    }

    enum Goals {
        LOOK_AT_PLAYER,
        HURT_BY_TARGET,
        NEAREST_ATTACKABLE_TARGET,
        MELEE_ATTACK
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
                case CLEARGOALS:
                    action = combineConsumer(action, e -> e.goalSelector.removeAllGoals(g -> true));
                    break;
                case CLEARTARGETS:
                    action = combineConsumer(action, e -> e.targetSelector.removeAllGoals(g -> true));
                    break;
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

    private static boolean getDefaultBoolean(JsonObject object, String key, boolean defaultValue) {
        if (object.has(key)) {
            JsonElement element = object.get(key);
            return element.getAsBoolean();
        }
        return defaultValue;
    }

    private static BiConsumer<GoalSelector, Mob> parseGoal(JsonElement el) {
        if (!el.isJsonObject()) {
            ErrorHandler.error("Invalid goal in 'goals' action! Expected a JSON object.");
            return (s, e) -> {};
        }
        JsonObject goal = el.getAsJsonObject();
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
            case NEAREST_ATTACKABLE_TARGET:
                return combineConsumer(goalSelector, (s, e) -> s.addGoal(priority, new NearestAttackableTargetGoal(e, Player.class, true)));
            case MELEE_ATTACK:
                return combineConsumer(goalSelector, (s, e) -> {
                    if (e instanceof PathfinderMob pf) {
                        s.addGoal(priority, new MeleeAttackGoal(pf, 1.0f, false));
                    }
                });
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
