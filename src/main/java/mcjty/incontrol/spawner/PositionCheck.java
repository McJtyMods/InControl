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

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class PositionCheck {

    private final TriFunction<Level, BlockPos, Player, Boolean> extraConditions;

    enum Cmd {
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
        BUILDING,
        MULTIBUILDING,
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


    private PositionCheck(Builder builder, boolean defaultIfNone) {
        extraConditions = builder.extraConditions == null ? (level, blockPos, player) -> defaultIfNone : builder.extraConditions;
    }

    @Nonnull
    public TriFunction<Level, BlockPos, Player, Boolean> getExtraConditions() {
        return extraConditions;
    }

    public static Builder create(BiFunction<TriFunction<Level, BlockPos, Player, Boolean>, TriFunction<Level, BlockPos, Player, Boolean>, TriFunction<Level, BlockPos, Player, Boolean>> combiner) {
        return new Builder(combiner);
    }

    public static void parse(JsonObject object, Builder builder) {
        for (String attr : object.keySet()) {
            Cmd cmd = CONDITIONS.get(attr);
            if (cmd == null) {
                ErrorHandler.error("Invalid condition '" + attr + "' for spawner rule!");
                return;
            }

            switch (cmd) {
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
                        Set<TagKey<Biome>> keys = new HashSet<>();
                        for (JsonElement element : value.getAsJsonArray()) {
                            ResourceLocation key = new ResourceLocation(element.getAsString());
                            keys.add(TagKey.create(Registries.BIOME, key));
                        }
                        builder.extraCondition((level, pos, player) -> {
                            for (TagKey<Biome> key : keys) {
                                Holder<Biome> biome = level.getBiome(pos);
                                if (biome.is(key)) {
                                    return true;
                                }
                            }
                            return false;
                        });
                    } else {
                        TagKey<Biome> tag = TagKey.create(Registries.BIOME, new ResourceLocation(value.getAsString()));
                        builder.extraCondition((level, pos, player) -> level.getBiome(pos).is(tag));
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
                case BUILDING -> {
                    if (!ModSetup.lostcities) {
                        ErrorHandler.error("Lost Cities condition specified but Lost Cities mod is not loaded!");
                        return;
                    }
                    JsonElement value = object.get(attr);
                    Set<String> keys = new HashSet<>();
                    if (value.isJsonArray()) {
                        for (JsonElement element : value.getAsJsonArray()) {
                            keys.add(element.getAsString());
                        }
                    } else {
                        keys.add(value.getAsString());
                    }
                    builder.extraCondition((level, pos, player) -> {
                        String building = LostCitySupport.getBuildingName(level, pos);
                        return keys.contains(building);
                    });
                }
                case MULTIBUILDING -> {
                    if (!ModSetup.lostcities) {
                        ErrorHandler.error("Lost Cities condition specified but Lost Cities mod is not loaded!");
                        return;
                    }
                    JsonElement value = object.get(attr);
                    Set<String> keys = new HashSet<>();
                    if (value.isJsonArray()) {
                        for (JsonElement element : value.getAsJsonArray()) {
                            keys.add(element.getAsString());
                        }
                    } else {
                        keys.add(value.getAsString());
                    }
                    builder.extraCondition((level, pos, player) -> {
                        String building = LostCitySupport.getMultiBuildingName(level, pos);
                        return keys.contains(building);
                    });
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
        private final BiFunction<TriFunction<Level, BlockPos, Player, Boolean>, TriFunction<Level, BlockPos, Player, Boolean>, TriFunction<Level, BlockPos, Player, Boolean>> combiner;

        public Builder(BiFunction<TriFunction<Level, BlockPos, Player, Boolean>, TriFunction<Level, BlockPos, Player, Boolean>, TriFunction<Level, BlockPos, Player, Boolean>> combiner) {
            this.combiner = combiner;
        }

        private TriFunction<Level, BlockPos, Player, Boolean> extraConditions = null;

        public Builder extraCondition(TriFunction<Level, BlockPos, Player, Boolean> extraCondition) {
            if (this.extraConditions == null) {
                this.extraConditions = extraCondition;
            } else {
                TriFunction<Level, BlockPos, Player, Boolean> oldCondition = this.extraConditions;
                this.extraConditions = combiner.apply(oldCondition, extraCondition);
            }
            return this;
        }

        public PositionCheck build(boolean defaultIfNone) {
            return new PositionCheck(this, defaultIfNone);
        }
    }
}
