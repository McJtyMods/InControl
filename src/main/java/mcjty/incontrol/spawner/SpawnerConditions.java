package mcjty.incontrol.spawner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SpawnerConditions {

    private final Set<RegistryKey<World>> dimensions;
    private final int mindist;
    private final int maxdist;
    private final int minheight;
    private final int maxheight;
    private final boolean inWater;
    private final boolean inAir;
    private final boolean noRestrictions;
    private final int maxthis;
    private final int maxlocal;
    private final int maxtotal;
    private final int maxhostile;
    private final int maxpeaceful;
    private final int maxneutral;

    public static final SpawnerConditions DEFAULT = SpawnerConditions.create().build();

    private SpawnerConditions(Builder builder) {
        dimensions = new HashSet<>(builder.dimensions);
        mindist = builder.mindist;
        maxdist = builder.maxdist;
        minheight = builder.minheight;
        maxheight = builder.maxheight;
        inWater = builder.inWater;
        inAir = builder.inAir;
        maxthis = builder.maxthis;
        maxlocal = builder.maxlocal;
        maxtotal = builder.maxtotal;
        maxhostile = builder.maxhostile;
        maxpeaceful = builder.maxpeaceful;
        maxneutral = builder.maxneutral;
        noRestrictions = builder.noRestrictions;

        validate();
    }

    private void validate() {
        if (mindist < 0) {
            throw new IllegalStateException("Invalid negative minimum distance!");
        }
        if (maxdist < 0) {
            throw new IllegalStateException("Invalid negative maximum distance!");
        }
        if (minheight < 0) {
            throw new IllegalStateException("Invalid negative minimum height!");
        }
        if (maxheight < 0) {
            throw new IllegalStateException("Invalid negative maximum height!");
        }
        if (mindist >= maxdist) {
            throw new IllegalStateException("Minimum distance must be smaller then maximum!");
        }
        if (minheight >= maxheight) {
            throw new IllegalStateException("Minimum height must be smaller then maximum!");
        }
    }

    public Set<RegistryKey<World>> getDimensions() {
        return dimensions;
    }

    public int getMindist() {
        return mindist;
    }

    public int getMaxdist() {
        return maxdist;
    }

    public int getMinheight() {
        return minheight;
    }

    public int getMaxheight() {
        return maxheight;
    }

    public boolean isInWater() {
        return inWater;
    }

    public boolean isInAir() {
        return inAir;
    }

    public boolean isNoRestrictions() {
        return noRestrictions;
    }

    public int getMaxthis() {
        return maxthis;
    }

    public int getMaxlocal() {
        return maxlocal;
    }

    public int getMaxtotal() {
        return maxtotal;
    }

    public int getMaxhostile() {
        return maxhostile;
    }

    public int getMaxpeaceful() {
        return maxpeaceful;
    }

    public int getMaxneutral() {
        return maxneutral;
    }

    public static Builder create() {
        return new Builder();
    }

    public static void parse(JsonObject object, Builder builder) {
        if (object.has("dimension")) {
            JsonElement dimension = object.get("dimension");
            if (dimension.isJsonArray()) {
                for (JsonElement element : dimension.getAsJsonArray()) {
                    RegistryKey<World> key = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(element.getAsString()));
                    builder.dimensions(key);
                }
            } else {
                RegistryKey<World> key = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(dimension.getAsString()));
                builder.dimensions(key);
            }
        }
        if (object.has("mindist")) {
            builder.distance(object.getAsJsonPrimitive("mindist").getAsInt(), builder.maxdist);
        }
        if (object.has("maxdist")) {
            builder.distance(builder.mindist, object.getAsJsonPrimitive("maxdist").getAsInt());
        }
        if (object.has("minheight")) {
            builder.height(object.getAsJsonPrimitive("minheight").getAsInt(), builder.maxheight);
        }
        if (object.has("maxheight")) {
            builder.height(builder.minheight, object.getAsJsonPrimitive("maxheight").getAsInt());
        }
        if (object.has("inwater")) {
            builder.inWater(object.getAsJsonPrimitive("inwater").getAsBoolean());
        }
        if (object.has("inair")) {
            builder.inAir(object.getAsJsonPrimitive("inair").getAsBoolean());
        }
        if (object.has("norestrictions")) {
            builder.noRestrictions(object.getAsJsonPrimitive("norestrictions").getAsBoolean());
        }
        if (object.has("maxthis")) {
            builder.maxThis(object.getAsJsonPrimitive("maxthis").getAsInt());
        }
        if (object.has("maxlocal")) {
            builder.maxLocal(object.getAsJsonPrimitive("maxlocal").getAsInt());
        }
        if (object.has("maxtotal")) {
            builder.maxTotal(object.getAsJsonPrimitive("maxtotal").getAsInt());
        }
        if (object.has("maxhostile")) {
            builder.maxHostile(object.getAsJsonPrimitive("maxhostile").getAsInt());
        }
        if (object.has("maxpeaceful")) {
            builder.maxPeaceful(object.getAsJsonPrimitive("maxpeaceful").getAsInt());
        }
        if (object.has("maxneutral")) {
            builder.maxNeutral(object.getAsJsonPrimitive("maxneutral").getAsInt());
        }
    }

    public static class Builder {
        private final Set<RegistryKey<World>> dimensions = new HashSet<>();

        private int mindist = 24;
        private int maxdist = 120;
        private int minheight = 1;
        private int maxheight = 256;
        private boolean inWater = false;
        private boolean inAir = false;
        private boolean noRestrictions = false;

        private int maxthis = -1;
        private int maxlocal = -1;
        private int maxtotal = -1;
        private int maxhostile = -1;
        private int maxpeaceful = -1;
        private int maxneutral = -1;

        public Builder dimensions(RegistryKey<World>... dimensions) {
            Collections.addAll(this.dimensions, dimensions);
            return this;
        }

        public Builder noRestrictions(boolean noRestrictions) {
            this.noRestrictions = noRestrictions;
            return this;
        }

        public Builder distance(int min, int max) {
            this.mindist = min;
            this.maxdist = max;
            return this;
        }

        public Builder height(int min, int max) {
            this.minheight = min;
            this.maxheight = max;
            return this;
        }


        public Builder inWater(boolean inWater) {
            this.inWater = inWater;
            return this;
        }

        public Builder inAir(boolean inAir) {
            this.inAir = inAir;
            return this;
        }

        public Builder maxThis(int maxThis) {
            this.maxthis = maxThis;
            return this;
        }

        public Builder maxLocal(int maxLocal) {
            this.maxlocal = maxLocal;
            return this;
        }

        public Builder maxTotal(int maxTotal) {
            this.maxtotal = maxTotal;
            return this;
        }

        public Builder maxHostile(int maxHostile) {
            this.maxhostile = maxHostile;
            return this;
        }

        public Builder maxPeaceful(int maxPeaceful) {
            this.maxpeaceful = maxPeaceful;
            return this;
        }

        public Builder maxNeutral(int maxNeutral) {
            this.maxneutral = maxNeutral;
            return this;
        }

        public SpawnerConditions build() {
            return new SpawnerConditions(this);
        }
    }
}
