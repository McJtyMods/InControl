package mcjty.incontrol.spawner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.InControl;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpawnerRule {

    private final List<EntityType<?>> mobs = new ArrayList<>();
    private final float persecond;
    private final int attempts;
    private final int minSpawn;
    private final int maxSpawn;
    private final SpawnerConditions conditions;

    private SpawnerRule(Builder builder) {
        mobs.addAll(builder.mobs);
        persecond = builder.persecond;
        attempts = builder.attempts;
        conditions = builder.conditions;
        minSpawn = builder.minSpawn;
        maxSpawn = builder.maxSpawn;
    }

    public List<EntityType<?>> getMobs() {
        return mobs;
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
        if (object.has("mob")) {
            JsonElement mob = object.get("mob");
            if (mob.isJsonArray()) {
                for (JsonElement element : mob.getAsJsonArray()) {
                    addMob(builder, element);
                }
            } else {
                addMob(builder, mob);
            }
        }
        if (object.has("persecond")) {
            builder.perSecond(object.getAsJsonPrimitive("persecond").getAsFloat());
        }
        if (object.has("attempts")) {
            builder.attempts(object.getAsJsonPrimitive("attempts").getAsInt());
        }
        if (object.has("amount")) {
            JsonObject amount = object.getAsJsonObject("amount");
            if (amount.has("minimum")) {
                builder.minSpawn(amount.getAsJsonPrimitive("minimum").getAsInt());
            }
            if (amount.has("maximum")) {
                builder.maxSpawn(amount.getAsJsonPrimitive("maximum").getAsInt());
            }
        }
        if (object.has("conditions")) {
            JsonObject conditions = object.getAsJsonObject("conditions");
            SpawnerConditions.Builder conditionsBuilder = SpawnerConditions.create();
            SpawnerConditions.parse(conditions, conditionsBuilder);
            builder.conditions(conditionsBuilder.build());
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
        private float persecond = 1.0f;
        private int attempts = 1;
        private int minSpawn = 1;
        private int maxSpawn = 1;
        private SpawnerConditions conditions = SpawnerConditions.DEFAULT;

        public Builder mobs(EntityType<?>... mobs) {
            Collections.addAll(this.mobs, mobs);
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
