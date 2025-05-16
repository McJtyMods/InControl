package mcjty.incontrol.spawner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.LongSet;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.compat.GameStageSupport;
import mcjty.incontrol.compat.LostCitySupport;
import mcjty.incontrol.compat.SereneSeasonsSupport;
import mcjty.incontrol.rules.support.GenericRuleEvaluator;
import mcjty.incontrol.setup.ModSetup;
import mcjty.incontrol.tools.cache.StructureCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.commons.lang3.function.TriFunction;

import java.util.*;
import java.util.function.Predicate;

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
    private final TriFunction<Level, BlockPos, Player, Boolean> extraConditions;

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
        STURDY,

        MINTIME,
        MAXTIME,
        MINLIGHT,
        MAXLIGHT,
        MINLIGHT_FULL,
        MAXLIGHT_FULL,
        BIOME,
        BIOMETAGS,
        SEESKY,
        CAVE,
        STRUCTURE,
        HASSTRUCTURE,
        STRUCTURETAGS,
        INCITY,
        INBUILDING,
        INMULTIBUILDING,
        INSTREET,
        INSPHERE,
        GAMESTAGE,
        SUMMER,
        WINTER,
        SPRING,
        AUTUMN
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
        extraConditions = builder.extraConditions == null ? (level, blockPos, player) -> true : builder.extraConditions;
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

    public TriFunction<Level, BlockPos, Player, Boolean> getExtraConditions() {
        return extraConditions;
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
                    builder.verticalDistance(object.getAsJsonPrimitive("minverticaldist").getAsInt(), builder.verticalMaxdist);
                }
                case MAXVERTICALDIST -> {
                    builder.verticalDistance(builder.verticalMindist, object.getAsJsonPrimitive("maxverticaldist").getAsInt());
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

                case MINTIME -> {
                    final int mintime = object.getAsJsonPrimitive("mintime").getAsInt();
                    builder.extraCondition((level, pos, player) -> (int) (level.getDayTime() % 24000) >= mintime);
                }
                case MAXTIME -> {
                    final int maxtime = object.getAsJsonPrimitive("maxtime").getAsInt();
                    builder.extraCondition((level, pos, player) -> (int) (level.getDayTime() % 24000) <= maxtime);
                }
                case MINLIGHT -> {
                    final int minlight = object.getAsJsonPrimitive("minlight").getAsInt();
                    builder.extraCondition((level, pos, player) -> level.getBrightness(LightLayer.BLOCK, pos) >= minlight);
                }
                case MAXLIGHT -> {
                    final int maxlight = object.getAsJsonPrimitive("maxlight").getAsInt();
                    builder.extraCondition((level, pos, player) -> level.getBrightness(LightLayer.BLOCK, pos) <= maxlight);
                }
                case MINLIGHT_FULL -> {
                    final int minlight = object.getAsJsonPrimitive("minlight_full").getAsInt();
                    builder.extraCondition((level, pos, player) -> level.getMaxLocalRawBrightness(pos) >= minlight);
                }
                case MAXLIGHT_FULL -> {
                    final int maxlight = object.getAsJsonPrimitive("maxlight_full").getAsInt();
                    builder.extraCondition((level, pos, player) -> level.getMaxLocalRawBrightness(pos) <= maxlight);
                }
                case BIOME -> {
                    JsonElement value = object.get(attr);
                    if (value.isJsonArray()) {
                        Set<ResourceKey<Biome>> keys = new HashSet<>();
                        for (JsonElement element : value.getAsJsonArray()) {
                            ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, new ResourceLocation(element.getAsString()));
                            keys.add(key);
                        }
                        Predicate<ResourceKey<Biome>> predicate = key -> keys.contains(key);
                        builder.extraCondition((level, pos, player) -> level.getBiome(pos).is(predicate));
                    } else {
                        ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, new ResourceLocation(value.getAsString()));
                        builder.extraCondition((level, pos, player) -> level.getBiome(pos).is(key));
                    }
                }
                case BIOMETAGS -> {
                    JsonElement value = object.get(attr);
                    if (value.isJsonArray()) {
                        Set<ResourceLocation> keys = new HashSet<>();
                        for (JsonElement element : value.getAsJsonArray()) {
                            ResourceLocation key = new ResourceLocation(element.getAsString());
                            keys.add(key);
                        }
                        Predicate<ResourceKey<Biome>> predicate = key -> keys.contains(key.location());
                        builder.extraCondition((level, pos, player) -> level.getBiome(pos).is(predicate));
                    } else {
                        ResourceLocation key = new ResourceLocation(value.getAsString());
                        builder.extraCondition((level, pos, player) -> level.getBiome(pos).is(key));
                    }
                }
                case SEESKY -> {
                    final boolean seesky = object.getAsJsonPrimitive("seesky").getAsBoolean();
                    builder.extraCondition((level, pos, player) -> level.canSeeSkyFromBelowWater(pos) == seesky);
                }
                case CAVE -> {
                    final boolean cave = object.getAsJsonPrimitive("cave").getAsBoolean();
                    builder.extraCondition((level, pos, player) -> GenericRuleEvaluator.checkCave(level, pos) == cave);
                }
                case STRUCTURE -> {
                    JsonElement value = object.get(attr);
                    if (value.isJsonArray()) {
                        Set<String> keys = new HashSet<>();
                        for (JsonElement element : value.getAsJsonArray()) {
                            keys.add(element.getAsString());
                        }
                        builder.extraCondition((level, pos, player) -> {
                            for (String key : keys) {
                                if (StructureCache.CACHE.isInStructure(level, key, pos)) {
                                    return true;
                                }
                            }
                            return false;
                        });
                    } else {
                        final String structure = value.getAsString();
                        builder.extraCondition((level, pos, player) -> StructureCache.CACHE.isInStructure(level, structure, pos));
                    }
                }
                case HASSTRUCTURE -> {
                    final boolean hasstructure = object.getAsJsonPrimitive("hasstructure").getAsBoolean();
                    builder.extraCondition((level, pos, player) -> StructureCache.CACHE.isInAnyStructure(level, pos) == hasstructure);
                }
                case STRUCTURETAGS -> {
                    JsonElement value = object.get(attr);
                    Set<TagKey<Structure>> tagSet = new HashSet<>();
                    if (value.isJsonArray()) {
                        for (JsonElement element : value.getAsJsonArray()) {
                            ResourceLocation key = new ResourceLocation(element.getAsString());
                            TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, key);
                            tagSet.add(tag);
                        }
                    } else {
                        ResourceLocation key = new ResourceLocation(value.getAsString());
                        TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, key);
                        tagSet.add(tag);
                    }
                    builder.extraCondition((level, pos, player) -> {
                        ChunkAccess chunk = level.getChunk(pos);
                        Map<Structure, LongSet> references = chunk.getAllReferences();
                        for (Map.Entry<Structure, LongSet> e : references.entrySet()) {
                            LongSet longs = e.getValue();
                            if (!longs.isEmpty()) {
                                Structure struct = e.getKey();
                                Optional<ResourceKey<Structure>> resourceKey = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getResourceKey(struct);
                                if (resourceKey.isPresent()) {
                                    Holder.Reference<Structure> holder = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolder(resourceKey.get()).get();
                                    for (TagKey<Structure> tagKey : tagSet) {
                                        if (holder.is(tagKey)) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                        return false;
                    });
                }
                case INCITY -> {
                    if (!ModSetup.lostcities) {
                        ErrorHandler.error("Lost Cities condition specified but Lost Cities mod is not loaded!");
                        return;
                    }
                    final boolean incity = object.getAsJsonPrimitive("incity").getAsBoolean();
                    builder.extraCondition((level, pos, player) -> LostCitySupport.isCity(level, pos) == incity);
                }
                case INBUILDING -> {
                    if (!ModSetup.lostcities) {
                        ErrorHandler.error("Lost Cities condition specified but Lost Cities mod is not loaded!");
                        return;
                    }
                    final boolean inbuilding = object.getAsJsonPrimitive("inbuilding").getAsBoolean();
                    builder.extraCondition((level, pos, player) -> LostCitySupport.isBuilding(level, pos) == inbuilding);
                }
                case INMULTIBUILDING -> {
                    if (!ModSetup.lostcities) {
                        ErrorHandler.error("Lost Cities condition specified but Lost Cities mod is not loaded!");
                        return;
                    }
                    final boolean inmultibuilding = object.getAsJsonPrimitive("inmultibuilding").getAsBoolean();
                    builder.extraCondition((level, pos, player) -> LostCitySupport.isMultiBuilding(level, pos) == inmultibuilding);
                }
                case INSTREET -> {
                    if (!ModSetup.lostcities) {
                        ErrorHandler.error("Lost Cities condition specified but Lost Cities mod is not loaded!");
                        return;
                    }
                    final boolean instreet = object.getAsJsonPrimitive("instreet").getAsBoolean();
                    builder.extraCondition((level, pos, player) -> LostCitySupport.isStreet(level, pos) == instreet);
                }
                case INSPHERE -> {
                    if (!ModSetup.lostcities) {
                        ErrorHandler.error("Lost Cities condition specified but Lost Cities mod is not loaded!");
                        return;
                    }
                    final boolean inspere = object.getAsJsonPrimitive("inspere").getAsBoolean();
                    builder.extraCondition((level, pos, player) -> LostCitySupport.inSphere(level, pos) == inspere);
                }
                case GAMESTAGE -> {
                    if (!ModSetup.gamestages) {
                        ErrorHandler.error("Game Stages condition specified but Game Stages mod is not loaded!");
                        return;
                    }
                    JsonElement value = object.get(attr);
                    if (value.isJsonArray()) {
                        Set<String> keys = new HashSet<>();
                        for (JsonElement element : value.getAsJsonArray()) {
                            keys.add(element.getAsString());
                        }
                        builder.extraCondition((level, pos, player) -> {
                            for (String key : keys) {
                                if (GameStageSupport.hasGameStage(player, key)) {
                                    return true;
                                }
                            }
                            return false;
                        });
                    } else {
                        final String gamestage = value.getAsString();
                        builder.extraCondition((level, pos, player) -> GameStageSupport.hasGameStage(player, gamestage));
                    }
                }
                case SUMMER -> {
                    if (!ModSetup.sereneSeasons) {
                        ErrorHandler.error("Serene Seasons condition specified but Serene Seasons mod is not loaded!");
                        return;
                    }
                    final boolean summer = object.getAsJsonPrimitive("summer").getAsBoolean();
                    builder.extraCondition((level, pos, player) -> SereneSeasonsSupport.isSummer(level) == summer);
                }
                case WINTER -> {
                    if (!ModSetup.sereneSeasons) {
                        ErrorHandler.error("Serene Seasons condition specified but Serene Seasons mod is not loaded!");
                        return;
                    }
                    final boolean winter = object.getAsJsonPrimitive("winter").getAsBoolean();
                    builder.extraCondition((level, pos, player) -> SereneSeasonsSupport.isWinter(level) == winter);
                }
                case SPRING -> {
                    if (!ModSetup.sereneSeasons) {
                        ErrorHandler.error("Serene Seasons condition specified but Serene Seasons mod is not loaded!");
                        return;
                    }
                    final boolean spring = object.getAsJsonPrimitive("spring").getAsBoolean();
                    builder.extraCondition((level, pos, player) -> SereneSeasonsSupport.isSpring(level) == spring);
                }
                case AUTUMN -> {
                    if (!ModSetup.sereneSeasons) {
                        ErrorHandler.error("Serene Seasons condition specified but Serene Seasons mod is not loaded!");
                        return;
                    }
                    final boolean autumn = object.getAsJsonPrimitive("autumn").getAsBoolean();
                    builder.extraCondition((level, pos, player) -> SereneSeasonsSupport.isAutumn(level) == autumn);
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
        private TriFunction<Level, BlockPos, Player, Boolean> extraConditions = null;

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

        public Builder extraCondition(TriFunction<Level, BlockPos, Player, Boolean> extraCondition) {
            if (this.extraConditions == null) {
                this.extraConditions = extraCondition;
            } else {
                TriFunction<Level, BlockPos, Player, Boolean> oldCondition = this.extraConditions;
                this.extraConditions = (level, blockPos, player) -> oldCondition.apply(level, blockPos, player) && extraCondition.apply(level, blockPos, player);
            }
            return this;
        }

        public SpawnerConditions build() {
            return new SpawnerConditions(this);
        }
    }
}
