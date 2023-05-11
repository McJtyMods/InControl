package mcjty.incontrol.spawner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;

public class SpawnerConditions {

    private final Set<ResourceKey<Level>> dimensions;
    private final int mindist;
    private final int maxdist;
    private final int verticalMindist;
    private final int verticalMaxdist;
    private final int minheight;
    private final int maxheight;
    private final int mindaycount;
    private final int maxdaycount;
    private final boolean inLiquid;
    private final boolean inWater;
    private final boolean inLava;
    private final boolean inAir;
    private final boolean noRestrictions;
    private final int maxthis;
    private final int maxlocal;
    private final int maxtotal;
    private final int maxhostile;
    private final int maxpeaceful;
    private final int maxneutral;
    private final boolean validSpawn;
    private final boolean sturdy;

    public static final SpawnerConditions DEFAULT = SpawnerConditions.create().build();

    enum Cmd {
        DIMENSION,
        MINDIST,
        MAXDIST,
        MINVERTICALDIST,
        MAXVERTICALDIST,
        MINDAYCOUNT,
        MAXDAYCOUNT,
        MINHEIGHT,
        MAXHEIGHT,
        INWATER,
        INLAVA,
        INLIQUID,
        INAIR,
        NORESTRICTIONS,
        MAXTHIS,
        MAXLOCAL,
        MAXTOTAL,
        MAXHOSTILE,
        MAXPEACEFUL,
        MAXNEUTRAL,
        VALIDSPAWN,
        STURDY
    }

    private static final Map<String, Cmd> CONDITIONS = new HashMap<>();
    static {
        for (Cmd cmd : Cmd.values()) {
            CONDITIONS.put(cmd.name().toLowerCase(), cmd);
        }
    }


    private SpawnerConditions(Builder builder) {
        dimensions = new HashSet<>(builder.dimensions);
        mindist = builder.mindist;
        maxdist = builder.maxdist;
        verticalMindist = builder.verticalMindist;
        verticalMaxdist = builder.verticalMaxdist;
        minheight = builder.minheight;
        maxheight = builder.maxheight;
        mindaycount = builder.mindaycount;
        maxdaycount = builder.maxdaycount;
        inLiquid = builder.inLiquid;
        inWater = builder.inWater;
        inLava = builder.inLava;
        inAir = builder.inAir;
        maxthis = builder.maxthis;
        maxlocal = builder.maxlocal;
        maxtotal = builder.maxtotal;
        maxhostile = builder.maxhostile;
        maxpeaceful = builder.maxpeaceful;
        maxneutral = builder.maxneutral;
        noRestrictions = builder.noRestrictions;
        validSpawn = builder.validSpawn;
        sturdy = builder.sturdy;
    }

    public void validate() {
        if (dimensions.isEmpty()) {
            throw new IllegalStateException("No dimensions specified!");
        }
        if (mindaycount < 0) {
            throw new IllegalStateException("Invalid negative minimum daycount!");
        }
        if (maxdaycount < 0) {
            throw new IllegalStateException("Invalid negative maximum daycount!");
        }
        if (mindist < 0) {
            throw new IllegalStateException("Invalid negative minimum distance!");
        }
        if (maxdist < 0) {
            throw new IllegalStateException("Invalid negative maximum distance!");
        }
        if (mindist >= maxdist) {
            throw new IllegalStateException("Minimum distance must be smaller then maximum!");
        }
        if (minheight >= maxheight) {
            throw new IllegalStateException("Minimum height must be smaller then maximum!");
        }
        if (verticalMindist >= verticalMaxdist) {
            throw new IllegalStateException("Minimum vertical distance must be smaller then maximum!");
        }
    }

    public Set<ResourceKey<Level>> getDimensions() {
        return dimensions;
    }

    public int getMindaycount() {
        return mindaycount;
    }

    public int getMaxdaycount() {
        return maxdaycount;
    }

    public int getMindist() {
        return mindist;
    }

    public int getMaxdist() {
        return maxdist;
    }

    public int getVerticalMindist() {
        return verticalMindist;
    }

    public int getVerticalMaxdist() {
        return verticalMaxdist;
    }

    public int getMinheight() {
        return minheight;
    }

    public int getMaxheight() {
        return maxheight;
    }

    public boolean isInLiquid() {
        return inLiquid;
    }

    public boolean isInWater() {
        return inWater;
    }

    public boolean isInLava() {
        return inLava;
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

    public boolean isValidSpawn() {
        return validSpawn;
    }

    public boolean isSturdy() {
        return sturdy;
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
                case MINDIST -> {
                    builder.distance(object.getAsJsonPrimitive("mindist").getAsInt(), builder.maxdist);
                }
                case MAXDIST -> {
                    builder.distance(builder.mindist, object.getAsJsonPrimitive("maxdist").getAsInt());
                }
                case MINVERTICALDIST -> {
                    builder.verticalDistance(object.getAsJsonPrimitive("minverticaldist").getAsInt(), builder.maxdist);
                }
                case MAXVERTICALDIST -> {
                    builder.verticalDistance(builder.mindist, object.getAsJsonPrimitive("maxverticaldist").getAsInt());
                }
                case MINDAYCOUNT -> {
                    builder.daycount(object.getAsJsonPrimitive("mindaycount").getAsInt(), builder.maxdaycount);
                }
                case MAXDAYCOUNT -> {
                    builder.daycount(builder.mindaycount, object.getAsJsonPrimitive("maxdaycount").getAsInt());
                }
                case MINHEIGHT -> {
                    builder.height(object.getAsJsonPrimitive("minheight").getAsInt(), builder.maxheight);
                }
                case MAXHEIGHT -> {
                    builder.height(builder.minheight, object.getAsJsonPrimitive("maxheight").getAsInt());
                }
                case INWATER -> {
                    builder.inWater(object.getAsJsonPrimitive("inwater").getAsBoolean());
                }
                case INLAVA -> {
                    builder.inLava(object.getAsJsonPrimitive("inlava").getAsBoolean());
                }
                case INLIQUID -> {
                    builder.inLiquid(object.getAsJsonPrimitive("inliquid").getAsBoolean());
                }
                case INAIR -> {
                    builder.inAir(object.getAsJsonPrimitive("inair").getAsBoolean());
                }
                case NORESTRICTIONS -> {
                    builder.noRestrictions(object.getAsJsonPrimitive("norestrictions").getAsBoolean());
                }
                case MAXTHIS -> {
                    builder.maxThis(object.getAsJsonPrimitive("maxthis").getAsInt());
                }
                case MAXLOCAL -> {
                    builder.maxLocal(object.getAsJsonPrimitive("maxlocal").getAsInt());
                }
                case MAXTOTAL -> {
                    builder.maxTotal(object.getAsJsonPrimitive("maxtotal").getAsInt());
                }
                case MAXHOSTILE -> {
                    builder.maxHostile(object.getAsJsonPrimitive("maxhostile").getAsInt());
                }
                case MAXPEACEFUL -> {
                    builder.maxPeaceful(object.getAsJsonPrimitive("maxpeaceful").getAsInt());
                }
                case MAXNEUTRAL -> {
                    builder.maxNeutral(object.getAsJsonPrimitive("maxneutral").getAsInt());
                }
                case VALIDSPAWN -> {
                    builder.validSpawn(object.getAsJsonPrimitive("validspawn").getAsBoolean());
                }
                case STURDY -> {
                    builder.sturdy(object.getAsJsonPrimitive("sturdy").getAsBoolean());
                }
            }
        }
    }

    public static class Builder {
        private final Set<ResourceKey<Level>> dimensions = new HashSet<>();

        private int mindist = 24;
        private int maxdist = 120;
        private int verticalMindist = -1;
        private int verticalMaxdist = -1;
        private int mindaycount = 0;
        private int maxdaycount = Integer.MAX_VALUE;
        private int minheight = 1;
        private int maxheight = 256;
        private boolean inLiquid = false;
        private boolean inWater = false;
        private boolean inLava = false;
        private boolean inAir = false;
        private boolean noRestrictions = false;
        private boolean validSpawn = false;
        private boolean sturdy = false;

        private int maxthis = -1;
        private int maxlocal = -1;
        private int maxtotal = -1;
        private int maxhostile = -1;
        private int maxpeaceful = -1;
        private int maxneutral = -1;

        public Builder dimensions(ResourceKey<Level>... dimensions) {
            Collections.addAll(this.dimensions, dimensions);
            return this;
        }

        public Builder noRestrictions(boolean noRestrictions) {
            this.noRestrictions = noRestrictions;
            return this;
        }

        public Builder daycount(int min, int max) {
            this.mindaycount = min;
            this.maxdaycount = max;
            return this;
        }

        public Builder distance(int min, int max) {
            this.mindist = min;
            this.maxdist = max;
            return this;
        }

        public Builder verticalDistance(int min, int max) {
            this.verticalMindist = min;
            this.verticalMaxdist = max;
            return this;
        }

        public Builder height(int min, int max) {
            this.minheight = min;
            this.maxheight = max;
            return this;
        }

        public Builder inLiquid(boolean inLiquid) {
            this.inLiquid = inLiquid;
            return this;
        }

        public Builder inWater(boolean inWater) {
            this.inWater = inWater;
            return this;
        }

        public Builder inLava(boolean inLava) {
            this.inLava = inLava;
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

        public Builder validSpawn(boolean validSpawn) {
            this.validSpawn = validSpawn;
            return this;
        }

        public Builder sturdy(boolean sturdy) {
            this.sturdy = sturdy;
            return this;
        }

        public SpawnerConditions build() {
            return new SpawnerConditions(this);
        }
    }
}
