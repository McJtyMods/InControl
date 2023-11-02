package mcjty.incontrol.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class EventsRule {

    private final EventType eventType;
    private final EventsConditions conditions;
    private final SpawnEventAction action;

    enum Cmd {
        ON,
        PARAMETER,
        SPAWN,
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
    }


    public static Builder create() {
        return new Builder();
    }

    public static void parse(JsonObject object, Builder builder) {
        EventType.Type type = null;
        String parameter = null;

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
                case PARAMETER -> {
                    JsonElement par = object.get("parameter");
                    parameter = par.getAsString();
                }
                case SPAWN -> {
                    List<ResourceLocation> mobs = new ArrayList<>();
                    JsonObject value = object.getAsJsonObject("spawn");
                    JsonElement mob = value.get("mob");
                    if (mob.isJsonArray()) {
                        for (JsonElement element : mob.getAsJsonArray()) {
                            ResourceLocation mobid = new ResourceLocation(element.getAsString());
                            if (!ForgeRegistries.ENTITY_TYPES.containsKey(mobid)) {
                                ErrorHandler.error("Invalid mob '" + mobid + "' for events rule!");
                                return;
                            }
                            mobs.add(mobid);
                        }
                    } else {
                        ResourceLocation mobid = new ResourceLocation(mob.getAsString());
                        if (!ForgeRegistries.ENTITY_TYPES.containsKey(mobid)) {
                            ErrorHandler.error("Invalid mob '" + mobid + "' for events rule!");
                            return;
                        }
                        mobs.add(mobid);
                    }
                    if (mobs.isEmpty()) {
                        ErrorHandler.error("No mobs specified for events rule!");
                        return;
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
                        return;
                    }
                    if (mindistance > maxdistance) {
                        ErrorHandler.error("Mindistance can't be larger than maxdistance for events rule!");
                        return;
                    }

                    SpawnEventAction action = new SpawnEventAction(mobs, attempts, mindistance, maxdistance, mincount, maxcount);
                    builder.action(action);
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
        builder.eventType(new EventType(type, parameter));
    }

    public EventType getEventType() {
        return eventType;
    }

    public EventsConditions getConditions() {
        return conditions;
    }

    public SpawnEventAction getAction() {
        return action;
    }

    public static class Builder {

        private EventsConditions conditions = EventsConditions.DEFAULT;
        private SpawnEventAction action;
        private EventType eventType;

        public Builder conditions(EventsConditions conditions) {
            this.conditions = conditions;
            return this;
        }

        public Builder action(SpawnEventAction action) {
            this.action = action;
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
