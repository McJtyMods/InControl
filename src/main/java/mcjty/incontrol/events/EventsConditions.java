package mcjty.incontrol.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.tools.rules.TestingTools;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Predicate;

public class EventsConditions {

    private final Set<ResourceKey<Level>> dimensions;
    private final float random;
    private final Set<String> phases;
    private final Map<String, Predicate<Integer>> numbers;

    public static final EventsConditions DEFAULT = EventsConditions.create().build();

    enum Cmd {
        DIMENSION,
        RANDOM,
        PHASE,
        NUMBER
    }

    private static final Map<String, Cmd> CONDITIONS = new HashMap<>();
    static {
        for (Cmd cmd : Cmd.values()) {
            CONDITIONS.put(cmd.name().toLowerCase(), cmd);
        }
    }


    private EventsConditions(Builder builder) {
        dimensions = new HashSet<>(builder.dimensions);
        random = builder.random;
        phases = builder.phases;
        numbers = builder.numbers;
    }

    public void validate() {
//        if (dimensions.isEmpty()) {
//            throw new IllegalStateException("No dimensions specified!");
//        }
    }

    public Set<ResourceKey<Level>> getDimensions() {
        return dimensions;
    }

    public float getRandom() {
        return random;
    }

    public Set<String> getPhases() {
        return phases;
    }

    public Map<String, Predicate<Integer>> getNumbers() {
        return numbers;
    }

    public static Builder create() {
        return new Builder();
    }

    public static void parse(JsonObject object, Builder builder) {
        for (String attr : object.keySet()) {
            Cmd cmd = CONDITIONS.get(attr);
            if (cmd == null) {
                ErrorHandler.error("Invalid condition '" + attr + "' for spawner rule!");
                return;
            }

            switch (cmd) {
                case DIMENSION -> {
                    JsonElement value = object.get(attr);
                    if (value.isJsonArray()) {
                        for (JsonElement element : value.getAsJsonArray()) {
                            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(element.getAsString()));
                            builder.dimensions(key);
                        }
                    } else {
                        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(value.getAsString()));
                        builder.dimensions(key);
                    }
                }
                case RANDOM -> {
                    builder.random(object.get(attr).getAsFloat());
                }
                case PHASE -> {
                    JsonElement value = object.get(attr);
                    if (value.isJsonArray()) {
                        for (JsonElement element : value.getAsJsonArray()) {
                            builder.phase(element.getAsString());
                        }
                    } else {
                        builder.phase(value.getAsString());
                    }
                }
                case NUMBER -> {
                    JsonElement value = object.get(attr);
                    if (value.isJsonArray()) {
                        for (JsonElement element : value.getAsJsonArray()) {
                            TestingTools.NumberResult result = TestingTools.parseNumberCheck(element);
                            if (result != null) {
                                builder.numbers.put(result.number(), result.test());
                            }
                        }
                    } else {
                        TestingTools.NumberResult result = TestingTools.parseNumberCheck(value);
                        if (result != null) {
                            builder.numbers.put(result.number(), result.test());
                        }
                    }
                }
            }
        }
    }

    public static class Builder {
        private final Set<ResourceKey<Level>> dimensions = new HashSet<>();
        private float random = -1;
        private final Set<String> phases = new HashSet<>();
        private final Map<String, Predicate<Integer>> numbers = new HashMap<>();

        public Builder dimensions(ResourceKey<Level>... dimensions) {
            Collections.addAll(this.dimensions, dimensions);
            return this;
        }

        public Builder random(float random) {
            this.random = random;
            return this;
        }

        public Builder phase(String... phases) {
            Collections.addAll(this.phases, phases);
            return this;
        }

        public Builder number(String name, Predicate<Integer> predicate) {
            numbers.put(name, predicate);
            return this;
        }

        public EventsConditions build() {
            return new EventsConditions(this);
        }
    }
}