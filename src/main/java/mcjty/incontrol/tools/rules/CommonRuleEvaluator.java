package mcjty.incontrol.tools.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import mcjty.incontrol.areas.Area;
import mcjty.incontrol.areas.AreaSystem;
import mcjty.incontrol.tools.cache.StructureCache;
import mcjty.incontrol.tools.typed.AttributeMap;
import mcjty.incontrol.tools.varia.LookAtTools;
import mcjty.incontrol.tools.varia.Tools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static mcjty.incontrol.tools.rules.CommonRuleKeys.*;

public class CommonRuleEvaluator {

    protected final List<BiFunction<Object, IEventQuery, Boolean>> checks = new ArrayList<>();
    private final IModRuleCompatibilityLayer compatibility;

    public CommonRuleEvaluator(AttributeMap map, IModRuleCompatibilityLayer compatibility) {
        this.compatibility = compatibility;
        addChecks(map);
    }

    // Rules in this routine are sorted so that the more expensive checks are added later
    protected void addChecks(AttributeMap map) {
        map.consume(RANDOM, this::addRandomCheck);
        map.consumeAsList(DIMENSION, this::addDimensionCheck);
        map.consumeAsList(DIMENSION_MOD, this::addDimensionModCheck);

        map.consume(TIME, this::addTimeCheck);
        map.consume(MINTIME, this::addMinTimeCheck);
        map.consume(MAXTIME, this::addMaxTimeCheck);

        map.consume(HEIGHT, this::addHeightCheck);
        map.consume(MINHEIGHT, this::addMinHeightCheck);
        map.consume(MAXHEIGHT, this::addMaxHeightCheck);

        map.consume(WEATHER, this::addWeatherCheck);
        map.consumeAsList(BIOMETAGS, this::addBiomeTagCheck);
        map.consume(DIFFICULTY, this::addDifficultyCheck);
        map.consume(MINSPAWNDIST, this::addMinSpawnDistCheck);
        map.consume(MAXSPAWNDIST, this::addMaxSpawnDistCheck);

        map.consume(LIGHT, this::addLightCheck);
        map.consume(MINLIGHT, this::addMinLightCheck);
        map.consume(MAXLIGHT, this::addMaxLightCheck);
        map.consume(MINLIGHT_FULL, this::addMinLightCheckCorrect);
        map.consume(MAXLIGHT_FULL, this::addMaxLightCheckCorrect);

        map.consume(MINDIFFICULTY, this::addMinAdditionalDifficultyCheck);
        map.consume(MAXDIFFICULTY, this::addMaxAdditionalDifficultyCheck);
        map.consume(SEESKY, this::addSeeSkyCheck);
        map.consume(SLIME, this::addSlimeChunkCheck);
        map.consume(AREA, this::addAreaCheck);
        map.consumeAsList(BLOCK, b -> addBlocksCheck(map, b));
        map.consumeAsList(BIOME, this::addBiomesCheck);
        map.consumeAsList(BIOMETYPE, this::addBiomeTypesCheck);

        map.consumeAsList(HELMET, this::addHelmetCheck);
        map.consumeAsList(CHESTPLATE, this::addChestplateCheck);
        map.consumeAsList(LEGGINGS, this::addLeggingsCheck);
        map.consumeAsList(BOOTS, this::addBootsCheck);
        map.consumeAsList(PLAYER_HELDITEM, items -> addHeldItemCheck(items, false));
        map.consumeAsList(HELDITEM, items -> addHeldItemCheck(items, false));
        map.consumeAsList(OFFHANDITEM, items -> addOffHandItemCheck(items, false));
        map.consumeAsList(BOTHHANDSITEM, this::addBothHandsItemCheck);

        map.consumeAsList(LACKHELMET, this::addHelmetCheckLacking);
        map.consumeAsList(LACKCHESTPLATE, this::addChestplateCheckLacking);
        map.consumeAsList(LACKLEGGINGS, this::addLeggingsCheckLacking);
        map.consumeAsList(LACKBOOTS, this::addBootsCheckLacking);
        map.consumeAsList(LACKHELDITEM, items -> addHeldItemCheck(items, true));
        map.consumeAsList(LACKOFFHANDITEM, items -> addOffHandItemCheck(items, true));

        map.consume(STRUCTURE, this::addStructureCheck);
        map.consumeAsList(SCOREBOARDTAGS_ALL, this::addAllScoreboardTagsCheck);
        map.consumeAsList(SCOREBOARDTAGS_ANY, this::addAnyScoreboardTagsCheck);

        map.consume(STATE, this::addStateCheck);
        map.consume(PSTATE, this::addPStateCheck);

        map.consume(SUMMER, this::addSummerCheck);
        map.consume(WINTER, this::addWinterCheck);
        map.consume(SPRING, this::addSpringCheck);
        map.consume(AUTUMN, this::addAutumnCheck);

        map.consume(GAMESTAGE, this::addGameStageCheck);

        map.consume(INCITY, this::addInCityCheck);
        map.consume(INSTREET, this::addInStreetCheck);
        map.consume(INSPHERE, this::addInSphereCheck);
        map.consume(INBUILDING, this::addInBuildingCheck);
        map.consumeAsList(BUILDING, this::addBuildingCheck);

        map.consumeAsList(AMULET, v -> addBaubleCheck(v, compatibility::getAmuletSlots));
        map.consumeAsList(RING, v -> addBaubleCheck(v, compatibility::getRingSlots));
        map.consumeAsList(BELT, v -> addBaubleCheck(v, compatibility::getBeltSlots));
        map.consumeAsList(TRINKET, v -> addBaubleCheck(v, compatibility::getTrinketSlots));
        map.consumeAsList(HEAD, v -> addBaubleCheck(v, compatibility::getHeadSlots));
        map.consumeAsList(BODY, v -> addBaubleCheck(v, compatibility::getBodySlots));
        map.consumeAsList(CHARM, v -> addBaubleCheck(v, compatibility::getCharmSlots));
    }

    private void addAreaCheck(String areaName) {
        Area area = AreaSystem.getArea(areaName);
        if (area == null) {
            ErrorHandler.error("Cannot find area '" + areaName + "'!");
        } else {
            checks.add((event, query) -> {
                LevelAccessor world = query.getWorld(event);
                ResourceKey<Level> key = Tools.getDimensionKey(world);
                if (area.dimension() == key) {
                    BlockPos pos = query.getPos(event);
                    return area.isInArea(pos.getX(), pos.getY(), pos.getZ());
                }
                return false;
            });
        }
    }

    private static final Random rnd = new Random();

    private void addRandomCheck(float r) {
        checks.add((event,query) -> rnd.nextFloat() < r);
    }

    private void addSeeSkyCheck(boolean seesky) {
        if (seesky) {
            checks.add((event,query) -> {
                LevelAccessor world = query.getWorld(event);
                BlockPos pos = query.getPos(event);
                LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                    return false;
                }
                return world.canSeeSkyFromBelowWater(pos);
            });
        } else {
            checks.add((event,query) -> {
                LevelAccessor world = query.getWorld(event);
                BlockPos pos = query.getPos(event);
                LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                    return false;
                }
                return !world.canSeeSkyFromBelowWater(pos);
            });
        }
    }

    private void addSlimeChunkCheck(boolean slime) {
        if (slime) {
            checks.add((event,query) -> isSlimeChunk(new ChunkPos(query.getPos(event)), query.getWorld(event)));
        } else {
            checks.add((event,query) -> !isSlimeChunk(new ChunkPos(query.getPos(event)), query.getWorld(event)));
        }
    }

    private boolean isSlimeChunk(ChunkPos cp, LevelAccessor world) {
        long seed = 0;
        if (world instanceof WorldGenLevel level) {
            seed = level.getSeed();
        }
        return WorldgenRandom.seedSlimeChunk(cp.x, cp.z, seed, 987234911L).nextInt(10) == 0;
    }

    private void addDimensionCheck(List<ResourceKey<Level>> dimensions) {
        if (dimensions.size() == 1) {
            ResourceKey<Level> dim = dimensions.get(0);
            checks.add((event,query) -> Tools.getDimensionKey(query.getWorld(event)).equals(dim));
        } else {
            Set<ResourceKey<Level>> dims = new HashSet<>(dimensions);
            checks.add((event,query) -> dims.contains(Tools.getDimensionKey(query.getWorld(event))));
        }
    }

    private void addDimensionModCheck(List<String> dimensions) {
        if (dimensions.size() == 1) {
            String dimmod = dimensions.get(0);
            checks.add((event,query) -> Tools.getDimensionKey(query.getWorld(event)).location().getNamespace().equals(dimmod));
        } else {
            Set<String> dims = new HashSet<>(dimensions);
            checks.add((event,query) -> dims.contains(Tools.getDimensionKey(query.getWorld(event)).location().getNamespace()));
        }
    }

    private void addDifficultyCheck(String difficulty) {
        difficulty = difficulty.toLowerCase();
        Difficulty diff = Difficulty.byName(difficulty);
        if (diff != null) {
            Difficulty finalDiff = diff;
            checks.add((event,query) -> query.getWorld(event).getDifficulty() == finalDiff);
        } else {
            ErrorHandler.error("Unknown difficulty '" + difficulty + "'! Use one of 'easy', 'normal', 'hard',  or 'peaceful'");
        }
    }

    private void addWeatherCheck(String weather) {
        boolean raining = weather.toLowerCase().startsWith("rain");
        boolean thunder = weather.toLowerCase().startsWith("thunder");
        if (raining) {
            checks.add((event,query) -> {
                LevelAccessor world = query.getWorld(event);
                if (world instanceof Level) {
                    return ((Level) world).isRaining();
                } else {
                    return false;
                }
            });
        } else if (thunder) {
            checks.add((event, query) -> {
                LevelAccessor world = query.getWorld(event);
                if (world instanceof Level) {
                    return ((Level) world).isThundering();
                } else {
                    return false;
                }
            });
        } else {
            ErrorHandler.error("Unknown weather '" + weather + "'! Use 'rain' or 'thunder'");
        }
    }

    private void addBiomeTagCheck(List<String> list) {
        Set<TagKey<Biome>> tags = list.stream().map(s -> TagKey.create(Registries.BIOME, new ResourceLocation(s))).collect(Collectors.toSet());
        if (tags.size() == 1) {
            TagKey<Biome> key = tags.iterator().next();
            checks.add((event,query) -> {
                Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
                return biome.is(key);
            });
        } else {
            checks.add((event, query) -> {
                Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
                for (TagKey<Biome> tag : tags) {
                    if (biome.is(tag)) {
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private void addAllScoreboardTagsCheck(List<String> list) {
        Set<String> tags = new HashSet<>(list);
        checks.add((event,query) -> {
            Entity entity = query.getEntity(event);
            if (entity instanceof LivingEntity living) {
                return living.getTags().containsAll(tags);
            }
            return false;
        });
    }

    private void addAnyScoreboardTagsCheck(List<String> list) {
        Set<String> tags = new HashSet<>(list);
        checks.add((event,query) -> {
            Entity entity = query.getEntity(event);
            if (entity instanceof LivingEntity living) {
                // Return true if entity.getTags() contains any key from tags
                return living.getTags().stream().anyMatch(tags::contains);
            }
            return false;
        });
    }

    private void addStructureCheck(String structure) {
        checks.add((event,query) -> StructureCache.CACHE.isInStructure(query.getWorld(event), structure, query.getPos(event)));
    }

    private void addBiomesCheck(List<String> biomes) {
        if (biomes.size() == 1) {
            String biomename = biomes.get(0);
            checks.add((event,query) -> {
                Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
                return Tools.getBiomeId(biome).equals(biomename);
            });
        } else {
            Set<String> biomenames = new HashSet<>(biomes);
            checks.add((event,query) -> {
                Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
                String biomeId = Tools.getBiomeId(biome);
                return biomenames.contains(biomeId);
            });
        }
    }

    private void addBiomeTypesCheck(List<String> biomeTypes) {
        Set<Biome> biomes = new HashSet<>();
        biomeTypes.stream().map(s -> BiomeManager.BiomeType.valueOf(s.toUpperCase())).
                forEach(type -> BiomeManager.getBiomes(type).forEach(t -> biomes.add(ForgeRegistries.BIOMES.getValue(t.getKey().registry()))));

        checks.add((event,query) -> {
            Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
            return biomes.contains(biome.value());
        });
    }

    public static <T extends Comparable<T>> BlockState set(BlockState state, Property<T> property, String value) {
        Optional<T> optionalValue = property.getValue(value);
        return optionalValue.map(t -> state.setValue(property, t)).orElse(state);
    }

    @Nonnull
    private BiFunction<Object, IEventQuery, BlockPos> parseOffset(String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        JsonObject obj = element.getAsJsonObject();

        int offsetX;
        int offsetY;
        int offsetZ;

        if (obj.has("offset")) {
            JsonObject offset = obj.getAsJsonObject("offset");
            offsetX = offset.has("x") ? offset.get("x").getAsInt() : 0;
            offsetY = offset.has("y") ? offset.get("y").getAsInt() : 0;
            offsetZ = offset.has("z") ? offset.get("z").getAsInt() : 0;
        } else {
            offsetX = obj.has("x") ? obj.get("x").getAsInt() : 0;
            offsetY = obj.has("y") ? obj.get("y").getAsInt() : 0;
            offsetZ = obj.has("z") ? obj.get("z").getAsInt() : 0;
        }

        if (obj.has("look")) {
            return (event, query) -> {
                HitResult result = LookAtTools.getMovingObjectPositionFromPlayer(query.getWorld(event), query.getPlayer(event), false);
                if (result instanceof BlockHitResult) {
                    return ((BlockHitResult) result).getBlockPos().offset(offsetX, offsetY, offsetZ);
                } else {
                    return query.getValidBlockPos(event).offset(offsetX, offsetY, offsetZ);
                }
            };

        }
        return (event, query) -> query.getValidBlockPos(event).offset(offsetX, offsetY, offsetZ);
    }

    private static boolean testBlockStateSafe(LevelAccessor world, BlockPos pos, Block block) {
        LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk != null) {
            BlockState state = world.getBlockState(pos);
            return state.getBlock() == block;
        } else {
            return false;
        }
    }

    private static boolean testBlockStateSafe(LevelAccessor world, BlockPos pos, BlockState block) {
        LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk != null) {
            BlockState state = world.getBlockState(pos);
            return state == block;
        } else {
            return false;
        }
    }

    @Nullable
    private static BiPredicate<LevelAccessor, BlockPos> parseBlock(String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        return parseBlockJson(element);
    }

    @Nullable
    public static BiPredicate<LevelAccessor, BlockPos> parseBlockJson(JsonElement element) {
        if (element.isJsonPrimitive()) {
            String blockname = element.getAsString();
            if (blockname.startsWith("tag:")) {
                ResourceLocation tagname = new ResourceLocation(blockname.substring(4));
                TagKey<Block> key = TagKey.create(Registries.BLOCK, tagname);
                return (world, pos) -> {
                    BlockState state = world.getBlockState(pos);
                    return state.is(key);
                };
            } else {
                if (!ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(blockname))) {
                    ErrorHandler.error("Block '" + blockname + "' is not valid!");
                    return null;
                }
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockname));
                return (world, pos) -> testBlockStateSafe(world, pos, block);
            }
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            BiPredicate<LevelAccessor, BlockPos> test;
            if (obj.has("tag")) {
                ResourceLocation tagname = new ResourceLocation(obj.get("tag").getAsString());
                TagKey<Block> key = TagKey.create(Registries.BLOCK, tagname);
                test = (world, pos) -> {
                    BlockState state = world.getBlockState(pos);
                    return state.is(key);
                };
            } else if (obj.has("block")) {
                String blockname = obj.get("block").getAsString();
                if (!ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(blockname))) {
                    ErrorHandler.error("Block '" + blockname + "' is not valid!");
                    return null;
                }
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockname));
                if (obj.has("properties")) {
                    BlockState blockState = block.defaultBlockState();
                    JsonArray propArray = obj.get("properties").getAsJsonArray();
                    for (JsonElement el : propArray) {
                        JsonObject propObj = el.getAsJsonObject();
                        String name = propObj.get("name").getAsString();
                        String value = propObj.get("value").getAsString();
                        for (Property<?> key : blockState.getProperties()) {
                            if (name.equals(key.getName())) {
                                blockState = set(blockState, key, value);
                            }
                        }
                    }
                    BlockState finalBlockState = blockState;
                    test = (world, pos) -> testBlockStateSafe(world, pos, finalBlockState);
                } else {
                    test = (world, pos) -> testBlockStateSafe(world, pos, block);
                }
            } else {
                test = (world, pos) -> true;
            }

            if (obj.has("mod")) {
                String mod = obj.get("mod").getAsString();
                BiPredicate<LevelAccessor, BlockPos> finalTest = test;
                test = (world, pos) -> {
                    LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                    if (chunk != null) {
                        return finalTest.test(world, pos) && mod.equals(ForgeRegistries.BLOCKS.getKey(world.getBlockState(pos).getBlock()).getNamespace());
                    } else {
                        return false;
                    }
                };
            }
            if (obj.has("energy")) {
                Predicate<Integer> energy = getExpression(obj.get("energy"));
                if (energy != null) {
                    Direction side;
                    if (obj.has("side")) {
                        side = Direction.byName(obj.get("side").getAsString().toLowerCase());
                    } else {
                        side = null;
                    }
                    BiPredicate<LevelAccessor, BlockPos> finalTest = test;
                    test = (world, pos) -> finalTest.test(world, pos) && energy.test(getEnergy(world, pos, side));
                }
            }
            if (obj.has("contains")) {
                Direction side;
                if (obj.has("side")) {
                    side = Direction.byName(obj.get("energyside").getAsString().toLowerCase());
                } else {
                    side = null;
                }
                List<Predicate<ItemStack>> items = getItems(obj.get("contains"));
                BiPredicate<LevelAccessor, BlockPos> finalTest = test;
                test = (world, pos) -> finalTest.test(world, pos) && contains(world, pos, side, items);
            }

            return test;
        } else {
            ErrorHandler.error("Block description is not valid!");
        }
        return null;
    }

    protected static List<Predicate<ItemStack>> getItems(JsonElement itemObj) {
        List<Predicate<ItemStack>> items = new ArrayList<>();
        if (itemObj.isJsonObject()) {
            Predicate<ItemStack> matcher = getMatcher(itemObj.getAsJsonObject());
            if (matcher != null) {
                items.add(matcher);
            }
        } else if (itemObj.isJsonArray()) {
            for (JsonElement element : itemObj.getAsJsonArray()) {
                JsonObject obj = element.getAsJsonObject();
                Predicate<ItemStack> matcher = getMatcher(obj);
                if (matcher != null) {
                    items.add(matcher);
                }
            }
        } else {
            ErrorHandler.error("Item description is not valid!");
        }
        return items;
    }

    private void addBlocksCheck(AttributeMap map, List<String> blocks) {
        BiFunction<Object, IEventQuery, BlockPos> posFunction;
        String bo = map.consumeAndFetch(BLOCKOFFSET);
        if (bo != null) {
            posFunction = parseOffset(bo);
        } else {
            posFunction = (event, query) -> query.getValidBlockPos(event);
        }

        if (blocks.size() == 1) {
            String json = blocks.get(0);
            BiPredicate<LevelAccessor, BlockPos> blockMatcher = parseBlock(json);
            if (blockMatcher != null) {
                checks.add((event, query) -> {
                    BlockPos pos = posFunction.apply(event, query);
                    return pos != null && blockMatcher.test(query.getWorld(event), pos);
                });
            }
        } else {
            List<BiPredicate<LevelAccessor, BlockPos>> blockMatchers = new ArrayList<>();
            for (String block : blocks) {
                BiPredicate<LevelAccessor, BlockPos> blockMatcher = parseBlock(block);
                if (blockMatcher == null) {
                    return;
                }
                blockMatchers.add(blockMatcher);
            }

            checks.add((event,query) -> {
                BlockPos pos = posFunction.apply(event, query);
                if (pos != null) {
                    LevelAccessor world = query.getWorld(event);
                    for (BiPredicate<LevelAccessor, BlockPos> matcher : blockMatchers) {
                        if (matcher.test(world, pos)) {
                            return true;
                        }
                    }
                }
                return false;
            });
        }
    }

    private void addTimeCheck(String time) {
        Predicate<Integer> expression = Tools.parseExpression(time);
        if (expression != null) {
            checks.add((event,query) -> {
                LevelAccessor world = query.getWorld(event);
                if (world instanceof Level) {
                    long t = ((Level)world).getDayTime();
                    return expression.test((int) (t % 24000));
                } else {
                    return false;
                }
            });
        }
    }

    private void addMinTimeCheck(int mintime) {
        checks.add((event,query) -> {
            LevelAccessor world = query.getWorld(event);
            if (world instanceof Level) {
                long time = ((Level)world).getDayTime();
                return (time % 24000) >= mintime;
            } else {
                return false;
            }
        });
    }

    private void addMaxTimeCheck(int maxtime) {
        checks.add((event,query) -> {
            LevelAccessor world = query.getWorld(event);
            if (world instanceof Level) {
                long time = ((Level)world).getDayTime();
                return (time % 24000) <= maxtime;
            } else {
                return false;
            }
        });
    }

    private void addMinSpawnDistCheck(float v) {
        final float d = v * v;
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            ServerLevel sw = Tools.getServerWorld(query.getWorld(event));
            double sqdist = pos.distSqr(sw.getSharedSpawnPos());
            return sqdist >= d;
        });
    }

    private void addMaxSpawnDistCheck(float v) {
        final float d = v * v;
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            ServerLevel sw = Tools.getServerWorld(query.getWorld(event));
            double sqdist = pos.distSqr(sw.getSharedSpawnPos());
            return sqdist <= d;
        });
    }

    private void addLightCheck(String expression) {
        Predicate<Integer> exp = Tools.parseExpression(expression);
        if (exp != null) {
            checks.add((event, query) -> {
                BlockPos pos = query.getPos(event);
                LevelAccessor world = query.getWorld(event);
                LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                    return false;
                }
                return exp.test(world.getBrightness(LightLayer.BLOCK, pos));
            });
        }
    }

    private void addMinLightCheck(int minlight) {
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            LevelAccessor world = query.getWorld(event);
            LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                return false;
            }
            return world.getBrightness(LightLayer.BLOCK, pos) >= minlight;
        });
    }

    private void addMinLightCheckCorrect(int minlight) {
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            LevelAccessor world = query.getWorld(event);
            LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                return false;
            }
            return world.getMaxLocalRawBrightness(pos) >= minlight;
        });
    }

    private void addMaxLightCheck(int maxlight) {
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            LevelAccessor world = query.getWorld(event);
            LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                return false;
            }
            return world.getBrightness(LightLayer.BLOCK, pos) <= maxlight;
        });
    }

    private void addMaxLightCheckCorrect(int maxlight) {
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            LevelAccessor world = query.getWorld(event);
            LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                return false;
            }
            return world.getMaxLocalRawBrightness(pos) <= maxlight;
        });
    }

    private void addMinAdditionalDifficultyCheck(Float mindifficulty) {
        checks.add((event,query) -> query.getWorld(event).getCurrentDifficultyAt(query.getPos(event)).getEffectiveDifficulty() >= mindifficulty);
    }

    private void addMaxAdditionalDifficultyCheck(Float maxdifficulty) {
        checks.add((event,query) -> query.getWorld(event).getCurrentDifficultyAt(query.getPos(event)).getEffectiveDifficulty() <= maxdifficulty);
    }

    private void addHeightCheck(String input) {
        Predicate<Integer> expression = Tools.parseExpression(input);
        if (expression != null) {
            checks.add((event, query) -> expression.test(query.getY(event)));
        }
    }

    private void addMaxHeightCheck(int maxheight) {
        checks.add((event,query) -> query.getY(event) <= maxheight);
    }

    private void addMinHeightCheck(int minheight) {
        checks.add((event,query) -> query.getY(event) >= minheight);
    }


    public boolean match(Object event, IEventQuery query) {
        for (BiFunction<Object, IEventQuery, Boolean> rule : checks) {
            if (!rule.apply(event, query)) {
                return false;
            }
        }
        return true;
    }

    private static Predicate<Integer> getExpressionInteger(String expression, boolean onlyInt) {
        try {
            if (expression.startsWith(">=")) {
                int amount = Integer.parseInt(expression.substring(2));
                return i -> i >= amount;
            }
            if (expression.startsWith(">")) {
                int amount = Integer.parseInt(expression.substring(1));
                return i -> i > amount;
            }
            if (expression.startsWith("<=")) {
                int amount = Integer.parseInt(expression.substring(2));
                return i -> i <= amount;
            }
            if (expression.startsWith("<")) {
                int amount = Integer.parseInt(expression.substring(1));
                return i -> i < amount;
            }
            if (expression.startsWith("=")) {
                int amount = Integer.parseInt(expression.substring(1));
                return i -> i == amount;
            }
            if (expression.startsWith("!=") || expression.startsWith("<>")) {
                int amount = Integer.parseInt(expression.substring(2));
                return i -> i != amount;
            }

            if (expression.contains("-")) {
                String[] split = StringUtils.split(expression, "-");
                int amount1 = Integer.parseInt(split[0]);
                int amount2 = Integer.parseInt(split[1]);
                return i -> i >= amount1 && i <= amount2;
            }

            int amount = Integer.parseInt(expression);
            return i -> i == amount;
        } catch (NumberFormatException e) {
            if (onlyInt) {
                ErrorHandler.error("Bad expression '" + expression + "'!");
            }
            return null;
        }
    }

    private static Predicate<Integer> getExpression(JsonElement element) {
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isNumber()) {
                int amount = element.getAsInt();
                return i -> i == amount;
            } else {
                return getExpressionInteger(element.getAsString(), true);
            }
        } else {
            ErrorHandler.error("Bad expression!");
            return null;
        }
    }

    private static Predicate<CompoundTag> getExpressionOrString(JsonElement element, String tag) {
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isNumber()) {
                int amount = element.getAsInt();
                return tagCompound -> tagCompound.getInt(tag) == amount;
            } else if (element.getAsJsonPrimitive().isBoolean()) {
                boolean v = element.getAsBoolean();
                return tagCompound -> tagCompound.getBoolean(tag) == v;
            } else {
                String str = element.getAsString();
                Predicate<Integer> predicate = getExpressionInteger(str, false);
                if (predicate == null) {
                    return tagCompound -> str.equals(tagCompound.getString(tag));
                }
                return tagCompound -> predicate.test(tagCompound.getInt(tag));
            }
        } else {
            ErrorHandler.error("Bad expression!");
            return null;
        }
    }


    private static Predicate<ItemStack> getMatcher(String name) {
        ItemStack stack = Tools.parseStack(name);
        if (!stack.isEmpty()) {
            // Stack matching
            if (name.contains("/") && name.contains("@")) {
                return s -> ItemStack.isSameItem(s, stack) && ItemStack.isSameItemSameTags(s, stack);
            } else if (name.contains("/")) {
                return s -> ItemStack.isSameItemSameTags(s, stack) && ItemStack.isSameItemSameTags(s, stack);
            } else if (name.contains("@")) {
                return s -> ItemStack.isSameItem(s, stack);
            } else {
                return s -> s.getItem() == stack.getItem();
            }
        }
        return null;
    }

    private static Predicate<ItemStack> getMatcher(JsonObject obj) {
        if (obj.has("empty")) {
            boolean empty = obj.get("empty").getAsBoolean();
            return s -> s.isEmpty() == empty;
        }

        String name = obj.get("item").getAsString();
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
        if (item == null) {
            ErrorHandler.error("Unknown item '" + name + "'!");
            return null;
        }

        Predicate<ItemStack> test;
        if (obj.has("damage")) {
            Predicate<Integer> damage = getExpression(obj.get("damage"));
            if (damage == null) {
                return null;
            }
            test = s -> s.getItem() == item && damage.test(s.getDamageValue());
        } else {
            test = s -> s.getItem() == item;
        }

        if (obj.has("count")) {
            Predicate<Integer> count = getExpression(obj.get("count"));
            if (count != null) {
                Predicate<ItemStack> finalTest = test;
                test = s -> finalTest.test(s) && count.test(s.getCount());
            }
        }
        if (obj.has("tag")) {
            ResourceLocation tagname = new ResourceLocation(obj.get("tag").getAsString());
            TagKey<Item> key = TagKey.create(Registries.ITEM, tagname);
            Predicate<ItemStack> finalTest = test;
            test = s -> finalTest.test(s) && s.is(key);
        }
        if (obj.has("mod")) {
            String mod = obj.get("mod").getAsString();
            Predicate<ItemStack> finalTest = test;
            test = s -> finalTest.test(s) && "mod".equals(ForgeRegistries.ITEMS.getKey(s.getItem()).getNamespace());
        }
        if (obj.has("nbt")) {
            List<Predicate<CompoundTag>> nbtMatchers = getNbtMatchers(obj);
            if (nbtMatchers != null) {
                Predicate<ItemStack> finalTest = test;
                test = s -> finalTest.test(s) && nbtMatchers.stream().allMatch(p -> p.test(s.getTag()));
            }
        }
        if (obj.has("energy")) {
            Predicate<Integer> energy = getExpression(obj.get("energy"));
            if (energy != null) {
                Predicate<ItemStack> finalTest = test;
                test = s -> finalTest.test(s) && energy.test(getEnergy(s));
            }
        }

        return test;
    }

    private static int getEnergy(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    private static boolean contains(LevelAccessor world, BlockPos pos, @Nullable Direction side, @Nonnull List<Predicate<ItemStack>> matchers) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity != null) {
            return tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side).map(h -> {
                for (int i = 0 ; i < h.getSlots() ; i++) {
                    ItemStack stack = h.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        for (Predicate<ItemStack> matcher : matchers) {
                            if (matcher.test(stack)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }).orElse(false);
        }
        return false;
    }

    private static int getEnergy(LevelAccessor world, BlockPos pos, @Nullable Direction side) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity != null) {
            return tileEntity.getCapability(ForgeCapabilities.ENERGY, side).map(IEnergyStorage::getEnergyStored).orElse(0);
        }
        return 0;
    }

    private static List<Predicate<CompoundTag>> getNbtMatchers(JsonObject obj) {
        JsonArray nbtArray = obj.getAsJsonArray("nbt");
        return getNbtMatchers(nbtArray);
    }

    private static List<Predicate<CompoundTag>> getNbtMatchers(JsonArray nbtArray) {
        List<Predicate<CompoundTag>> nbtMatchers = new ArrayList<>();
        for (JsonElement element : nbtArray) {
            JsonObject o = element.getAsJsonObject();
            String tag = o.get("tag").getAsString();
            if (o.has("contains")) {
                List<Predicate<CompoundTag>> subMatchers = getNbtMatchers(o.getAsJsonArray("contains"));
                nbtMatchers.add(tagCompound -> {
                    if (tagCompound != null) {
                        ListTag list = tagCompound.getList(tag, Tag.TAG_COMPOUND);
                        for (Tag base : list) {
                            for (Predicate<CompoundTag> matcher : subMatchers) {
                                if (matcher.test((CompoundTag) base)) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                });
            } else {
                Predicate<CompoundTag> nbt = getExpressionOrString(o.get("value"), tag);
                if (nbt != null) {
                    nbtMatchers.add(nbt);
                }
            }

        }
        return nbtMatchers;
    }


    public static List<Predicate<ItemStack>> getItems(List<String> itemNames) {
        List<Predicate<ItemStack>> items = new ArrayList<>();
        for (String json : itemNames) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(json);
            if (element.isJsonPrimitive()) {
                String name = element.getAsString();
                Predicate<ItemStack> matcher = getMatcher(name);
                if (matcher != null) {
                    items.add(matcher);
                }
            } else if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                Predicate<ItemStack> matcher = getMatcher(obj);
                if (matcher != null) {
                    items.add(matcher);
                }
            } else {
                ErrorHandler.error("Item description '" + json + "' is not valid!");
            }
        }
        return items;
    }

    public void addHelmetCheck(List<String> itemList) {
        List<Predicate<ItemStack>> items = getItems(itemList);
        addArmorCheck(items, EquipmentSlot.HEAD, false);
    }

    public void addChestplateCheck(List<String> itemList) {
        List<Predicate<ItemStack>> items = getItems(itemList);
        addArmorCheck(items, EquipmentSlot.CHEST, false);
    }

    public void addLeggingsCheck(List<String> itemList) {
        List<Predicate<ItemStack>> items = getItems(itemList);
        addArmorCheck(items, EquipmentSlot.LEGS, false);
    }

    public void addBootsCheck(List<String> itemList) {
        List<Predicate<ItemStack>> items = getItems(itemList);
        addArmorCheck(items, EquipmentSlot.FEET, false);
    }

    public void addHelmetCheckLacking(List<String> itemList) {
        List<Predicate<ItemStack>> items = getItems(itemList);
        addArmorCheck(items, EquipmentSlot.HEAD, true);
    }

    public void addChestplateCheckLacking(List<String> itemList) {
        List<Predicate<ItemStack>> items = getItems(itemList);
        addArmorCheck(items, EquipmentSlot.CHEST, true);
    }

    public void addLeggingsCheckLacking(List<String> itemList) {
        List<Predicate<ItemStack>> items = getItems(itemList);
        addArmorCheck(items, EquipmentSlot.LEGS, true);
    }

    public void addBootsCheckLacking(List<String> itemList) {
        List<Predicate<ItemStack>> items = getItems(itemList);
        addArmorCheck(items, EquipmentSlot.FEET, true);
    }

    private void addArmorCheck(List<Predicate<ItemStack>> items, EquipmentSlot slot, boolean lacking) {
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                ItemStack armorItem = player.getItemBySlot(slot);
                if (!armorItem.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(armorItem)) {
                            return !lacking;
                        }
                    }
                }
            }
            return lacking;
        });
    }

    public void addHeldItemCheck(List<String> itemList, boolean lacking) {
        List<Predicate<ItemStack>> items = getItems(itemList);
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                ItemStack mainhand = player.getMainHandItem();
                if (!mainhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(mainhand)) {
                            return !lacking;
                        }
                    }
                }
            }
            return lacking;
        });
    }

    public void addOffHandItemCheck(List<String> itemList, boolean lacking) {
        List<Predicate<ItemStack>> items = getItems(itemList);
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                ItemStack offhand = player.getOffhandItem();
                if (!offhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(offhand)) {
                            return !lacking;
                        }
                    }
                }
            }
            return lacking;
        });
    }

    public void addBothHandsItemCheck(List<String> itemList) {
        List<Predicate<ItemStack>> items = getItems(itemList);
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                ItemStack offhand = player.getOffhandItem();
                if (!offhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(offhand)) {
                            return true;
                        }
                    }
                }
                ItemStack mainhand = player.getMainHandItem();
                if (!mainhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(mainhand)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    private void addStateCheck(String s) {
        if (!compatibility.hasEnigmaScript()) {
            InControl.setup.getLogger().warn("EnigmaScript is missing: this test cannot work!");
            return;
        }
        String[] split = StringUtils.split(s, '=');
        String state;
        String value;
        try {
            state = split[0];
            value = split[1];
        } catch (Exception e) {
            ErrorHandler.error("Bad state=value specifier '" + s + "'!");
            return;
        }

        checks.add((event, query) -> value.equals(compatibility.getState(query.getWorld(event), state)));
    }

    private void addPStateCheck(String s) {
        if (!compatibility.hasEnigmaScript()) {
            InControl.setup.getLogger().warn("EnigmaScript is missing: this test cannot work!");
            return;
        }
        String[] split = StringUtils.split(s, '=');
        String state;
        String value;
        try {
            state = split[0];
            value = split[1];
        } catch (Exception e) {
            ErrorHandler.error("Bad state=value specifier '" + s + "'!");
            return;
        }

        checks.add((event, query) -> value.equals(compatibility.getPlayerState(query.getPlayer(event), state)));
    }

    private void addSummerCheck(Boolean s) {
        if (!compatibility.hasSereneSeasons()) {
            InControl.setup.getLogger().warn("Serene Seasons is missing: this test cannot work!");
            return;
        }
        checks.add((event, query) -> s == compatibility.isSummer(Tools.getServerWorld(query.getWorld(event))));
    }

    private void addWinterCheck(Boolean s) {
        if (!compatibility.hasSereneSeasons()) {
            InControl.setup.getLogger().warn("Serene Seasons is missing: this test cannot work!");
            return;
        }
        checks.add((event, query) -> s == compatibility.isWinter(Tools.getServerWorld(query.getWorld(event))));
    }

    private void addSpringCheck(Boolean s) {
        if (!compatibility.hasSereneSeasons()) {
            InControl.setup.getLogger().warn("Serene Seasons is missing: this test cannot work!");
            return;
        }
        checks.add((event, query) -> s == compatibility.isSpring(Tools.getServerWorld(query.getWorld(event))));
    }

    private void addAutumnCheck(Boolean s) {
        if (!compatibility.hasSereneSeasons()) {
            InControl.setup.getLogger().warn("Serene Seasons is missing: this test cannot work!");
            return;
        }
        checks.add((event, query) -> s == compatibility.isAutumn(Tools.getServerWorld(query.getWorld(event))));
    }

    private void addGameStageCheck(String stage) {
        if (!compatibility.hasGameStages()) {
            InControl.setup.getLogger().warn("Game Stages is missing: the 'gamestage' test cannot work!");
            return;
        }
        checks.add((event, query) -> compatibility.hasGameStage(query.getPlayer(event), stage));
    }

    private void addInCityCheck(boolean incity) {
        if (!compatibility.hasLostCities()) {
            InControl.setup.getLogger().warn("The Lost Cities is missing: the 'incity' test cannot work!");
            return;
        }
        if (incity) {
            checks.add((event,query) -> compatibility.isCity(query, event));
        } else {
            checks.add((event,query) -> !compatibility.isCity(query, event));
        }
    }

    private void addInStreetCheck(boolean instreet) {
        if (!compatibility.hasLostCities()) {
            InControl.setup.getLogger().warn("The Lost Cities is missing: the 'instreet' test cannot work!");
            return;
        }
        if (instreet) {
            checks.add((event,query) -> compatibility.isStreet(query, event));
        } else {
            checks.add((event,query) -> !compatibility.isStreet(query, event));
        }
    }

    private void addInSphereCheck(boolean insphere) {
        if (!compatibility.hasLostCities()) {
            InControl.setup.getLogger().warn("The Lost Cities is missing: the 'insphere' test cannot work!");
            return;
        }
        if (insphere) {
            checks.add((event,query) -> compatibility.inSphere(query, event));
        } else {
            checks.add((event,query) -> !compatibility.inSphere(query, event));
        }
    }

    private void addInBuildingCheck(boolean inbuilding) {
        if (!compatibility.hasLostCities()) {
            InControl.setup.getLogger().warn("The Lost Cities is missing: the 'inbuilding' test cannot work!");
            return;
        }
        if (inbuilding) {
            checks.add((event,query) -> compatibility.isBuilding(query, event));
        } else {
            checks.add((event,query) -> !compatibility.isBuilding(query, event));
        }
    }

    private void addBuildingCheck(List<String> buildings) {
        if (!compatibility.hasLostCities()) {
            InControl.setup.getLogger().warn("The Lost Cities is missing: the 'building' test cannot work!");
            return;
        }
        Set<String> buildingSet = new HashSet<>(buildings);
        checks.add((event,query) -> {
            String building = compatibility.getBuilding(query, event);
            return building != null && buildingSet.contains(building);
        });
    }

    public void addBaubleCheck(List<String> itemList, Supplier<int[]> slotSupplier) {
        if (!compatibility.hasBaubles()) {
            InControl.setup.getLogger().warn("Baubles is missing: this test cannot work!");
            return;
        }

        List<Predicate<ItemStack>> items = getItems(itemList);
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                for (int slot : slotSupplier.get()) {
                    ItemStack stack = compatibility.getBaubleStack(player, slot);
                    if (!stack.isEmpty()) {
                        for (Predicate<ItemStack> item : items) {
                            if (item.test(stack)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        });
    }
}
