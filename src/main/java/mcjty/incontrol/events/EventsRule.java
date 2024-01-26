package mcjty.incontrol.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mcjty.incontrol.rules.support.RuleKeys.PHASE;

public class EventsRule {

    private final EventType eventType;
    private final EventsConditions conditions;
    private final SpawnEventAction action;
    private final PhaseAction phaseAction;
    private final NumberAction numberAction;

    enum Cmd {
        ON,
        PARAMETERS,
        SPAWN,
        PHASE,
        NUMBER,
        CONDITIONS
    }

    private static final Map<String, Cmd> COMMANDS = new HashMap<>();
    static {
        for (Cmd cmd : Cmd.values()) {
            COMMANDS.put(cmd.name().toLowerCase(), cmd);
        }
    }

    private EventsRule(Builder builder) {
        conditions = builder.conditions;
        action = builder.action;
        eventType = builder.eventType;
        phaseAction = builder.phaseAction;
        numberAction = builder.numberAction;
    }


    public static Builder create() {
        return new Builder();
    }

    public static void parse(JsonObject object, Builder builder) {
        EventType.Type type = null;
        JsonObject parameters = null;

        for (String attr : object.keySet()) {
            Cmd cmd = COMMANDS.get(attr);
            if (cmd == null) {
                ErrorHandler.error("Invalid command '" + attr + "' for events rule!");
                return;
            }

            switch (cmd) {
                case ON -> {
                    JsonElement on = object.get("on");
                    type = EventType.Type.getType(on.getAsString());
                    if (type == null) {
                        ErrorHandler.error("Invalid 'on' value '" + on.getAsString() + "' for events rule!");
                        return;
                    }
                }
                case PARAMETERS -> {
                    parameters = object.getAsJsonObject("parameters");
                }
                case SPAWN -> {
                    SpawnEventAction action = parseSpawnEventAction(object);
                    if (action != null) {
                        builder.action(action);
                    }
                }
                case PHASE -> {
                    PhaseAction action = parsePhaseAction(object);
                    if (action != null) {
                        builder.action(action);
                    }
                }
                case NUMBER -> {
                    NumberAction action = parseNumberAction(object);
                    if (action != null) {
                        builder.action(action);
                    }
                }
                case CONDITIONS -> {
                    JsonObject conditions = object.getAsJsonObject("conditions");
                    EventsConditions.Builder conditionsBuilder = EventsConditions.create();
                    EventsConditions.parse(conditions, conditionsBuilder);
                    EventsConditions cnd = conditionsBuilder.build();
                    cnd.validate();
                    builder.conditions(cnd);
                }
            }
        }
        if (type == null) {
            ErrorHandler.error("No 'on' specified for events rule!");
            return;
        }
        EventType et;
        switch (type) {
            case MOB_KILLED -> {
                et = new EventTypeMobKilled();
                if (!et.parse(parameters)) {
                    return;
                }
            }
            case BLOCK_BROKEN -> {
                et = new EventTypeBlockBroken();
                if (!et.parse(parameters)) {
                    return;
                }
            }
            case CUSTOM -> {
                et = new EventTypeCustom();
                if (!et.parse(parameters)) {
                    return;
                }
            }
            default -> {
                ErrorHandler.error("Unknown event type '" + type + "' for events rule!");
                return;
            }
        }

        builder.eventType(et);
    }

    @Nullable
    private static NumberAction parseNumberAction(JsonObject object) {
        JsonObject number = object.getAsJsonObject("number");
        if (number == null) {
            // Valid
            return null;
        }
        if (!number.has("name")) {
            ErrorHandler.error("No name specified for number action!");
            return null;
        }
        if (!number.has("value")) {
            ErrorHandler.error("No set/add/mul specified for number action!");
            return null;
        }
        String name = number.getAsJsonPrimitive("name").getAsString();
        String value = number.getAsJsonPrimitive("value").getAsString();
        // Parse value as a string with the following format:
        // [<operator><number>]+
        // Example: *30+2
        // This means: multiply by 30 and add 2
        // Example: 60-1
        // This means: Take 60 and subtract 1
        List<NumberAction.Action> actions = new ArrayList<>();
        value = value.trim();
        int pos = 0;
        while (pos < value.length()) {
            char c = value.charAt(pos);
            if (c == ' ') {
                pos++;
                continue;
            }
            NumberAction.Operator operator = NumberAction.Operator.getOperator(c);
            if (operator == NumberAction.Operator.NONE) {
                ErrorHandler.error("Invalid number action '" + value + "'!");
                return null;
            }
            pos++;
            int start = pos;
            while (pos < value.length() && Character.isDigit(value.charAt(pos))) {
                pos++;
            }
            if (pos > start) {
                int v = Integer.parseInt(value.substring(start, pos));
                actions.add(new NumberAction.Action(operator, v));
            } else {
                ErrorHandler.error("Invalid number action '" + value + "'!");
                return null;
            }
        }
        return new NumberAction(name, actions);
    }

    @Nullable
    private static PhaseAction parsePhaseAction(JsonObject object) {
        JsonObject value = object.getAsJsonObject(PHASE.name());
        if (value == null) {
            // Valid
            return null;
        }
        List<String> phases = new ArrayList<>();
        JsonElement names = value.get("names");
        if (names == null) {
            ErrorHandler.error("No names specified for phase action!");
            return null;
        }
        if (names.isJsonPrimitive()) {
            if (!names.getAsJsonPrimitive().isString()) {
                ErrorHandler.error("Invalid names specified for phase action!");
                return null;
            }
            phases.add(names.getAsString());
        } else {
            for (JsonElement element : names.getAsJsonArray()) {
                if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                    ErrorHandler.error("Invalid names specified for phase action!");
                    return null;
                }
                phases.add(element.getAsString());
            }
        }

        boolean set;
        if (value.has("set")) {
            set = value.getAsJsonPrimitive("set").getAsBoolean();
        } else {
            set = true;
        }
        return new PhaseAction(phases, set);
    }

    @Nullable
    private static SpawnEventAction parseSpawnEventAction(JsonObject object) {
        List<ResourceLocation> mobs = new ArrayList<>();
        JsonObject value = object.getAsJsonObject("spawn");
        if (value == null) {
            // Valid
            return null;
        }
        JsonElement mob = value.get("mob");
        if (mob.isJsonArray()) {
            for (JsonElement element : mob.getAsJsonArray()) {
                ResourceLocation mobid = new ResourceLocation(element.getAsString());
                if (!ForgeRegistries.ENTITY_TYPES.containsKey(mobid)) {
                    ErrorHandler.error("Invalid mob '" + mobid + "' for events rule!");
                    return null;
                }
                mobs.add(mobid);
            }
        } else {
            ResourceLocation mobid = new ResourceLocation(mob.getAsString());
            if (!ForgeRegistries.ENTITY_TYPES.containsKey(mobid)) {
                ErrorHandler.error("Invalid mob '" + mobid + "' for events rule!");
                return null;
            }
            mobs.add(mobid);
        }
        if (mobs.isEmpty()) {
            ErrorHandler.error("No mobs specified for events rule!");
            return null;
        }

        int attempts = 10;
        int mincount = 1;
        int maxcount = 1;
        float mindistance = 0.0f;
        float maxdistance = 10.0f;
        if (value.has("attempts")) {
            attempts = value.getAsJsonPrimitive("attempts").getAsInt();
        }
        if (value.has("mindistance")) {
            mindistance = value.getAsJsonPrimitive("mindistance").getAsFloat();
        }
        if (value.has("maxdistance")) {
            maxdistance = value.getAsJsonPrimitive("maxdistance").getAsFloat();
        }
        if (value.has("mincount")) {
            mincount = value.getAsJsonPrimitive("mincount").getAsInt();
        }
        if (value.has("maxcount")) {
            maxcount = value.getAsJsonPrimitive("maxcount").getAsInt();
        }
        // Check count and distance bounds
        if (mincount > maxcount) {
            ErrorHandler.error("Mincount can't be larger than maxcount for events rule!");
            return null;
        }
        if (mindistance > maxdistance) {
            ErrorHandler.error("Mindistance can't be larger than maxdistance for events rule!");
            return null;
        }
        boolean norestrictions = false;
        if (value.has("norestrictions")) {
            norestrictions = value.getAsJsonPrimitive("norestrictions").getAsBoolean();
        }

        return new SpawnEventAction(mobs, attempts, mindistance, maxdistance, mincount, maxcount, norestrictions);
    }

    public EventType getEventType() {
        return eventType;
    }

    public EventsConditions getConditions() {
        return conditions;
    }

    public SpawnEventAction getSpawnAction() {
        return action;
    }

    public PhaseAction getPhaseAction() {
        return phaseAction;
    }

    public NumberAction getNumberAction() {
        return numberAction;
    }

    public static class Builder {

        private EventsConditions conditions = EventsConditions.DEFAULT;
        private SpawnEventAction action;
        private PhaseAction phaseAction;
        private NumberAction numberAction;
        private EventType eventType;

        public Builder conditions(EventsConditions conditions) {
            this.conditions = conditions;
            return this;
        }

        public Builder action(SpawnEventAction action) {
            this.action = action;
            return this;
        }

        public Builder action(PhaseAction phaseAction) {
            this.phaseAction = phaseAction;
            return this;
        }

        public Builder action(NumberAction numberAction) {
            this.numberAction = numberAction;
            return this;
        }

        public Builder eventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public EventsRule build() {
            return new EventsRule(this);
        }
    }

}
