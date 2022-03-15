package mcjty.incontrol.spawner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class SpawnerRule {

    private final List<EntityType<?>> mobs = new ArrayList<>();
    private final List<Float> weights = new ArrayList<>();
    private final MobCategory mobsFromBiome;

    private final float maxWeight;
    private final float persecond;
    private final int attempts;
    private final int minSpawn;
    private final int maxSpawn;
    private final Set<String> phases;
    private final SpawnerConditions conditions;

    enum Cmd {
        MOB,
        WEIGHTS,
        MOBSFROMBIOME,
        PHASE,
        PERSECOND,
        ATTEMPTS,
        AMOUNT,
        CONDITIONS
    }

    private static final Map<String, Cmd> COMMANDS = new HashMap<>();
    static {
        for (Cmd cmd : Cmd.values()) {
            COMMANDS.put(cmd.name().toLowerCase(), cmd);
        }
    }

    private SpawnerRule(Builder builder) {
        mobs.addAll(builder.mobs);
        weights.addAll(builder.weights);
        mobsFromBiome = builder.mobsFromBiome;

        phases = builder.phases;
        persecond = builder.persecond;
        attempts = builder.attempts;
        conditions = builder.conditions;
        minSpawn = builder.minSpawn;
        maxSpawn = builder.maxSpawn;
        float w = 0;
        for (Float weight : weights) {
            w += weight;
        }
        if (w <= 0) {
            w = mobs.size();
        }
        maxWeight = w;

    }

    public List<EntityType<?>> getMobs() {
        return mobs;
    }

    public List<Float> getWeights() {
        return weights;
    }

    public MobCategory getMobsFromBiome() {
        return mobsFromBiome;
    }

    public float getMaxWeight() {
        return maxWeight;
    }

    public Set<String> getPhases() {
        return phases;
    }

    public float getPersecond() {
        return persecond;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getMinSpawn() {
        return minSpawn;
    }

    public int getMaxSpawn() {
        return maxSpawn;
    }

    public SpawnerConditions getConditions() {
        return conditions;
    }

    public static Builder create() {
        return new Builder();
    }

    public static void parse(JsonObject object, Builder builder) {
        for (String attr : object.keySet()) {
            Cmd cmd = COMMANDS.get(attr);
            if (cmd == null) {
                ErrorHandler.error("Invalid command '" + attr + "' for spawner rule!");
                return;
            }

            switch (cmd) {
                case MOB -> {
                    JsonElement mob = object.get("mob");
                    if (mob.isJsonArray()) {
                        for (JsonElement element : mob.getAsJsonArray()) {
                            addMob(builder, element);
                        }
                    } else {
                        addMob(builder, mob);
                    }
                }
                case WEIGHTS -> {
                    JsonElement weights = object.get("weights");
                    if (weights.isJsonArray()) {
                        for (JsonElement element : weights.getAsJsonArray()) {
                            builder.weights(element.getAsFloat());
                        }
                    } else {
                        builder.weights(weights.getAsFloat());
                    }
                }
                case MOBSFROMBIOME -> {
                    if (!builder.mobs.isEmpty()) {
                        InControl.setup.getLogger().error("'mobsfrombiome' cannot be combined with manual mobs!");
                        throw new RuntimeException("'mobsfrombiome' cannot be combined with manual mobs!");
                    }
                    String name = object.get("mobsfrombiome").getAsString().toLowerCase();
                    MobCategory classification = MobCategory.byName(name);
                    if (classification == null) {
                        InControl.setup.getLogger().error("Unknown classification " + name + "!");
                        throw new RuntimeException("Unknown classification " + name + "!");
                    }
                    builder.mobsFromBiome(classification);
                }
                case PHASE -> {
                    JsonElement phaseElement = object.get("phase");
                    if (phaseElement.isJsonArray()) {
                        for (JsonElement element : phaseElement.getAsJsonArray()) {
                            builder.phases(element.getAsString());
                        }
                    } else {
                        builder.phases(phaseElement.getAsString());
                    }
                }
                case PERSECOND -> {
                    builder.perSecond(object.getAsJsonPrimitive("persecond").getAsFloat());
                }
                case ATTEMPTS -> {
                    builder.attempts(object.getAsJsonPrimitive("attempts").getAsInt());
                }
                case AMOUNT -> {
                    JsonObject amount = object.getAsJsonObject("amount");
                    if (amount.has("minimum")) {
                        builder.minSpawn(amount.getAsJsonPrimitive("minimum").getAsInt());
                    }
                    if (amount.has("maximum")) {
                        builder.maxSpawn(amount.getAsJsonPrimitive("maximum").getAsInt());
                    }
                }
                case CONDITIONS -> {
                    JsonObject conditions = object.getAsJsonObject("conditions");
                    SpawnerConditions.Builder conditionsBuilder = SpawnerConditions.create();
                    SpawnerConditions.parse(conditions, conditionsBuilder);
                    builder.conditions(conditionsBuilder.build());
                }
            }
        }
    }

    private static void addMob(Builder builder, JsonElement element) {
        EntityType<?> value = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(element.getAsString()));
        if (value == null) {
            InControl.setup.getLogger().error("Error finding entity " + element.getAsString() + "!");
            throw new RuntimeException("Error finding entity " + element.getAsString() + "!");
        }
        builder.mobs(value);
    }

    public static class Builder {
        private final List<EntityType<?>> mobs = new ArrayList<>();
        private final List<Float> weights = new ArrayList<>();
        private MobCategory mobsFromBiome = null;

        private final Set<String> phases = new HashSet<>();
        private float persecond = 1.0f;
        private int attempts = 1;
        private int minSpawn = 1;
        private int maxSpawn = 1;
        private SpawnerConditions conditions = SpawnerConditions.DEFAULT;

        public Builder mobs(EntityType<?>... mobs) {
            Collections.addAll(this.mobs, mobs);
            return this;
        }

        public Builder weights(Float... weights) {
            Collections.addAll(this.weights, weights);
            return this;
        }

        public Builder mobsFromBiome(MobCategory mobsFromBiome) {
            this.mobsFromBiome = mobsFromBiome;
            return this;
        }

        public Builder phases(String... phases) {
            Collections.addAll(this.phases, phases);
            return this;
        }

        public Builder perSecond(float persecond) {
            this.persecond = persecond;
            return this;
        }

        public Builder attempts(int attempts) {
            this.attempts = attempts;
            return this;
        }

        public Builder minSpawn(int minSpawn) {
            this.minSpawn = minSpawn;
            return this;
        }

        public Builder maxSpawn(int maxSpawn) {
            this.maxSpawn = maxSpawn;
            return this;
        }

        public Builder conditions(SpawnerConditions conditions) {
            this.conditions = conditions;
            return this;
        }

        public SpawnerRule build() {
            return new SpawnerRule(this);
        }
    }

}
