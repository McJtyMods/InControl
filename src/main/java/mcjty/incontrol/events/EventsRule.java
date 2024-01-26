package mcjty.incontrol.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;

import java.util.HashMap;
import java.util.Map;

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
                    SpawnEventAction action = SpawnEventAction.parse(object);
                    if (action != null) {
                        builder.action(action);
                    }
                }
                case PHASE -> {
                    PhaseAction action = PhaseAction.parse(object);
                    if (action != null) {
                        builder.action(action);
                    }
                }
                case NUMBER -> {
                    NumberAction action = NumberAction.parse(object);
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
