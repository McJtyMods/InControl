package mcjty.incontrol.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.incontrol.ErrorHandler;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AISystem {

    enum Cmd {
        CLEARGOALS,
        CLEARTARGETS,
        GOALS,
        TARGETS
    }

    private static final Map<String, Cmd> ACTIONS = new HashMap<>();
    static {
        for (Cmd cmd : Cmd.values()) {
            ACTIONS.put(cmd.name().toLowerCase(), cmd);
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
                    action = e -> e.goalSelector.removeAllGoals(g -> true);
                    break;
                case CLEARTARGETS:
                    action = e -> e.targetSelector.removeAllGoals(g -> true);
                    break;
                case GOALS:
                    if (value.isJsonArray()) {
                        for (JsonElement el : value.getAsJsonArray()) {
                            Consumer<GoalSelector> consumer = parseGoal(el);

                        }
                    } else {
                        ErrorHandler.error("Invalid value for 'goals' action! Expected a JSON array.");
                        return e -> {};
                    }
                    break;
                case TARGETS:
                    if (value.isJsonArray()) {
                    } else {
                        ErrorHandler.error("Invalid value for 'targets' action! Expected a JSON array.");
                        return e -> {};
                    }
                    break;
            }
        }
        return action == null ? e -> {} : action;
    }

    private static Consumer<GoalSelector> parseGoal(JsonElement el) {
        if (!el.isJsonObject()) {
            ErrorHandler.error("Invalid goal in 'goals' action! Expected a JSON object.");
            return e -> {};
        }
        JsonObject goal = el.getAsJsonObject();
        // It has the attributes 'goal' and 'priority'. Then specific attributes for the goal.
        if (!goal.has("goal") || !goal.has("priority")) {
            ErrorHandler.error("Invalid goal in 'goals' action! Expected 'goal' and 'priority' attributes.");
            return e -> {};
        }
        String goalName = goal.get("goal").getAsString();
        int priority = goal.get("priority").getAsInt();
        return e -> {};
    }

    private static <T> Consumer<T> combineConsumer(@Nullable Consumer<T> first, @Nonnull Consumer<T> second) {
        if (first == null) {
            return second;
        } else {
            return first.andThen(second);
        }
    }
}
