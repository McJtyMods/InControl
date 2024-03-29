package mcjty.incontrol.spawner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.*;

public class SpawnerConditions {

    private final Set<RegistryKey<World>> dimensions;
    private final int mindist;
    private final int maxdist;
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

    public static final SpawnerConditions DEFAULT = SpawnerConditions.create().build();

    enum Cmd {
        DIMENSION,
        MINDIST,
        MAXDIST,
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
        MAXNEUTRAL
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

        validate();
    }

    private void validate() {
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

    public static Builder create() {
        return new Builder();
    }

    public static void parse(JsonObject object, Builder builder) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String attr = entry.getKey();
            Cmd cmd = CONDITIONS.get(attr);
            if (cmd == null) {
                ErrorHandler.error("Invalid condition '" + attr + "' for spawner rule!");
                return;
            }

            switch (cmd) {
                case DIMENSION: {
                    JsonElement value = object.get(attr);
                    if (value.isJsonArray()) {
                        for (JsonElement element : value.getAsJsonArray()) {
                            RegistryKey<World> key = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(element.getAsString()));
                            builder.dimensions(key);
                        }
                    } else {
                        RegistryKey<World> key = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(value.getAsString()));
                        builder.dimensions(key);
                    }
                    break;
                }
                case MINDIST: {
                    builder.distance(object.getAsJsonPrimitive("mindist").getAsInt(), builder.maxdist);
                    break;
                }
                case MAXDIST: {
                    builder.distance(builder.mindist, object.getAsJsonPrimitive("maxdist").getAsInt());
                    break;
                }
                case MINDAYCOUNT: {
                    builder.daycount(object.getAsJsonPrimitive("mindaycount").getAsInt(), builder.maxdaycount);
                    break;
                }
                case MAXDAYCOUNT: {
                    builder.daycount(builder.mindaycount, object.getAsJsonPrimitive("maxdaycount").getAsInt());
                    break;
                }
                case MINHEIGHT: {
                    builder.height(object.getAsJsonPrimitive("minheight").getAsInt(), builder.maxheight);
                    break;
                }
                case MAXHEIGHT: {
                    builder.height(builder.minheight, object.getAsJsonPrimitive("maxheight").getAsInt());
                    break;
                }
                case INWATER: {
                    builder.inWater(object.getAsJsonPrimitive("inwater").getAsBoolean());
                    break;
                }
                case INLAVA: {
                    builder.inLava(object.getAsJsonPrimitive("inlava").getAsBoolean());
                    break;
                }
                case INLIQUID: {
                    builder.inLiquid(object.getAsJsonPrimitive("inliquid").getAsBoolean());
                    break;
                }
                case INAIR: {
                    builder.inAir(object.getAsJsonPrimitive("inair").getAsBoolean());
                    break;
                }
                case NORESTRICTIONS: {
                    builder.noRestrictions(object.getAsJsonPrimitive("norestrictions").getAsBoolean());
                    break;
                }
                case MAXTHIS: {
                    builder.maxThis(object.getAsJsonPrimitive("maxthis").getAsInt());
                    break;
                }
                case MAXLOCAL: {
                    builder.maxLocal(object.getAsJsonPrimitive("maxlocal").getAsInt());
                    break;
                }
                case MAXTOTAL: {
                    builder.maxTotal(object.getAsJsonPrimitive("maxtotal").getAsInt());
                    break;
                }
                case MAXHOSTILE: {
                    builder.maxHostile(object.getAsJsonPrimitive("maxhostile").getAsInt());
                    break;
                }
                case MAXPEACEFUL: {
                    builder.maxPeaceful(object.getAsJsonPrimitive("maxpeaceful").getAsInt());
                    break;
                }
                case MAXNEUTRAL: {
                    builder.maxNeutral(object.getAsJsonPrimitive("maxneutral").getAsInt());
                    break;
                }
            }
        }
    }

    public static class Builder {
        private final Set<RegistryKey<World>> dimensions = new HashSet<>();

        private int mindist = 24;
        private int maxdist = 120;
        private int mindaycount = 0;
        private int maxdaycount = Integer.MAX_VALUE;
        private int minheight = 1;
        private int maxheight = 256;
        private boolean inLiquid = false;
        private boolean inWater = false;
        private boolean inLava = false;
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

        public SpawnerConditions build() {
            return new SpawnerConditions(this);
        }
    }
}
