package mcjty.tools.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.tools.cache.StructureCache;
import mcjty.tools.typed.AttributeMap;
import mcjty.tools.typed.Key;
import mcjty.tools.varia.LookAtTools;
import mcjty.tools.varia.Tools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static mcjty.tools.rules.CommonRuleKeys.*;

public class CommonRuleEvaluator {

    protected final List<BiFunction<Event, IEventQuery, Boolean>> checks = new ArrayList<>();
    private final Logger logger;
    private final IModRuleCompatibilityLayer compatibility;

    public CommonRuleEvaluator(AttributeMap map, Logger logger, IModRuleCompatibilityLayer compatibility) {
        this.logger = logger;
        this.compatibility = compatibility;
        addChecks(map);
    }

    // Rules in this routine are sorted so that the more expensive checks are added later
    protected void addChecks(AttributeMap map) {
        if (map.has(RANDOM)) {
            addRandomCheck(map);
        }
        if (map.has(DIMENSION)) {
            addDimensionCheck(map);
        }
        if (map.has(MINTIME)) {
            addMinTimeCheck(map);
        }
        if (map.has(MAXTIME)) {
            addMaxTimeCheck(map);
        }

        if (map.has(MINHEIGHT)) {
            addMinHeightCheck(map);
        }
        if (map.has(MAXHEIGHT)) {
            addMaxHeightCheck(map);
        }
        if (map.has(WEATHER)) {
            addWeatherCheck(map);
        }
        if (map.has(TEMPCATEGORY)) {
            addTempCategoryCheck(map);
        }
        if (map.has(DIFFICULTY)) {
            addDifficultyCheck(map);
        }

        if (map.has(MINSPAWNDIST)) {
            addMinSpawnDistCheck(map);
        }
        if (map.has(MAXSPAWNDIST)) {
            addMaxSpawnDistCheck(map);
        }

        if (map.has(MINLIGHT)) {
            addMinLightCheck(map);
        }
        if (map.has(MAXLIGHT)) {
            addMaxLightCheck(map);
        }

        if (map.has(MINDIFFICULTY)) {
            addMinAdditionalDifficultyCheck(map);
        }
        if (map.has(MAXDIFFICULTY)) {
            addMaxAdditionalDifficultyCheck(map);
        }

        if (map.has(SEESKY)) {
            addSeeSkyCheck(map);
        }
        if (map.has(BLOCK)) {
            addBlocksCheck(map);
        }
        if (map.has(BIOME)) {
            addBiomesCheck(map);
        }
        if (map.has(BIOMETYPE)) {
            addBiomeTypesCheck(map);
        }
        if (map.has(HELMET)) {
            addHelmetCheck(map);
        }
        if (map.has(CHESTPLATE)) {
            addChestplateCheck(map);
        }
        if (map.has(LEGGINGS)) {
            addLeggingsCheck(map);
        }
        if (map.has(BOOTS)) {
            addBootsCheck(map);
        }
        if (map.has(PLAYER_HELDITEM)) {
            addHeldItemCheck(map, PLAYER_HELDITEM);
        }
        if (map.has(HELDITEM)) {
            addHeldItemCheck(map, HELDITEM);
        }
        if (map.has(OFFHANDITEM)) {
            addOffHandItemCheck(map);
        }
        if (map.has(BOTHHANDSITEM)) {
            addBothHandsItemCheck(map);
        }

        if (map.has(STRUCTURE)) {
            addStructureCheck(map);
        }

        if (map.has(STATE)) {
            if (compatibility.hasEnigmaScript()) {
                addStateCheck(map);
            } else {
                logger.warn("EnigmaScript is missing: this test cannot work!");
            }
        }
        if (map.has(PSTATE)) {
            if (compatibility.hasEnigmaScript()) {
                addPStateCheck(map);
            } else {
                logger.warn("EnigmaScript is missing: this test cannot work!");
            }
        }

        if (map.has(SUMMER)) {
            if (compatibility.hasSereneSeasons()) {
                addSummerCheck(map);
            } else {
                logger.warn("Serene Seaons is missing: this test cannot work!");
            }
        }
        if (map.has(WINTER)) {
            if (compatibility.hasSereneSeasons()) {
                addWinterCheck(map);
            } else {
                logger.warn("Serene Seaons is missing: this test cannot work!");
            }
        }
        if (map.has(SPRING)) {
            if (compatibility.hasSereneSeasons()) {
                addSpringCheck(map);
            } else {
                logger.warn("Serene Seaons is missing: this test cannot work!");
            }
        }
        if (map.has(AUTUMN)) {
            if (compatibility.hasSereneSeasons()) {
                addAutumnCheck(map);
            } else {
                logger.warn("Serene Seaons is missing: this test cannot work!");
            }
        }
        if (map.has(GAMESTAGE)) {
            if (compatibility.hasGameStages()) {
                addGameStageCheck(map);
            } else {
                logger.warn("Game Stages is missing: the 'gamestage' test cannot work!");
            }
        }
        if (map.has(INCITY)) {
            if (compatibility.hasLostCities()) {
                addInCityCheck(map);
            } else {
                logger.warn("The Lost Cities is missing: the 'incity' test cannot work!");
            }
        }
        if (map.has(INSTREET)) {
            if (compatibility.hasLostCities()) {
                addInStreetCheck(map);
            } else {
                logger.warn("The Lost Cities is missing: the 'instreet' test cannot work!");
            }
        }
        if (map.has(INSPHERE)) {
            if (compatibility.hasLostCities()) {
                addInSphereCheck(map);
            } else {
                logger.warn("The Lost Cities is missing: the 'insphere' test cannot work!");
            }
        }
        if (map.has(INBUILDING)) {
            if (compatibility.hasLostCities()) {
                addInBuildingCheck(map);
            } else {
                logger.warn("The Lost Cities is missing: the 'inbuilding' test cannot work!");
            }
        }

        if (map.has(AMULET)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, AMULET, compatibility::getAmuletSlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
        if (map.has(RING)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, RING, compatibility::getRingSlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
        if (map.has(BELT)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, BELT, compatibility::getBeltSlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
        if (map.has(TRINKET)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, TRINKET, compatibility::getTrinketSlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
        if (map.has(HEAD)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, HEAD, compatibility::getHeadSlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
        if (map.has(BODY)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, BODY, compatibility::getBodySlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
        if (map.has(CHARM)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, CHARM, compatibility::getCharmSlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
    }

    private static Random rnd = new Random();

    private void addRandomCheck(AttributeMap map) {
        final float r = map.get(RANDOM);
        checks.add((event,query) -> rnd.nextFloat() < r);
    }

    private void addSeeSkyCheck(AttributeMap map) {
        if (map.get(SEESKY)) {
            checks.add((event,query) -> query.getWorld(event).canBlockSeeSky(query.getPos(event)));
        } else {
            checks.add((event,query) -> !query.getWorld(event).canBlockSeeSky(query.getPos(event)));
        }
    }

    private void addDimensionCheck(AttributeMap map) {
        List<DimensionType> dimensions = map.getList(DIMENSION);
        if (dimensions.size() == 1) {
            DimensionType dim = dimensions.get(0);
            checks.add((event,query) -> query.getWorld(event).getDimension().getType() == dim);
        } else {
            Set<DimensionType> dims = new HashSet<>(dimensions);
            checks.add((event,query) -> dims.contains(query.getWorld(event).getDimension().getType()));
        }
    }

    private void addDifficultyCheck(AttributeMap map) {
        String difficulty = map.get(DIFFICULTY).toLowerCase();
        Difficulty diff = null;
        for (Difficulty d : Difficulty.values()) {
            if (d.getTranslationKey().endsWith("." + difficulty)) { // @todo 1.15 maybe a better way?
                diff = d;
                break;
            }
        }
        if (diff != null) {
            Difficulty finalDiff = diff;
            checks.add((event,query) -> query.getWorld(event).getDifficulty() == finalDiff);
        } else {
            logger.log(Level.ERROR, "Unknown difficulty '" + difficulty + "'! Use one of 'easy', 'normal', 'hard',  or 'peaceful'");
        }
    }

    private void addWeatherCheck(AttributeMap map) {
        String weather = map.get(WEATHER);
        boolean raining = weather.toLowerCase().startsWith("rain");
        boolean thunder = weather.toLowerCase().startsWith("thunder");
        if (raining) {
            checks.add((event,query) -> query.getWorld(event).isRaining());
        } else if (thunder) {
            checks.add((event,query) -> query.getWorld(event).isThundering());
        } else {
            logger.log(Level.ERROR, "Unknown weather '" + weather + "'! Use 'rain' or 'thunder'");
        }
    }

    private void addTempCategoryCheck(AttributeMap map) {
        String tempcategory = map.get(TEMPCATEGORY).toLowerCase();
        Biome.TempCategory cat = null;
        if ("cold".equals(tempcategory)) {
            cat = Biome.TempCategory.COLD;
        } else if ("medium".equals(tempcategory)) {
            cat = Biome.TempCategory.MEDIUM;
        } else if ("warm".equals(tempcategory)) {
            cat = Biome.TempCategory.WARM;
        } else if ("ocean".equals(tempcategory)) {
            cat = Biome.TempCategory.OCEAN;
        } else {
            logger.log(Level.ERROR, "Unknown tempcategory '" + tempcategory + "'! Use one of 'cold', 'medium', 'warm',  or 'ocean'");
            return;
        }

        Biome.TempCategory finalCat = cat;
        checks.add((event,query) -> {
            Biome biome = query.getWorld(event).getBiome(query.getPos(event));
            return biome.getTempCategory() == finalCat;
        });
    }

    private void addStructureCheck(AttributeMap map) {
        String structure = map.get(STRUCTURE);
        checks.add((event,query) -> StructureCache.CACHE.isInStructure(query.getWorld(event), structure, query.getPos(event)));
    }

    private void addBiomesCheck(AttributeMap map) {
        List<String> biomes = map.getList(BIOME);
        if (biomes.size() == 1) {
            String biomename = biomes.get(0);
            checks.add((event,query) -> {
                Biome biome = query.getWorld(event).getBiome(query.getPos(event));
                return biomename.equals(compatibility.getBiomeName(biome));
            });
        } else {
            Set<String> biomenames = new HashSet<>(biomes);
            checks.add((event,query) -> {
                Biome biome = query.getWorld(event).getBiome(query.getPos(event));
                return biomenames.contains(compatibility.getBiomeName(biome));
            });
        }
    }

    private void addBiomeTypesCheck(AttributeMap map) {
        List<String> biomeTypes = map.getList(BIOMETYPE);
        if (biomeTypes.size() == 1) {
            String biometype = biomeTypes.get(0);
            BiomeDictionary.Type type = BiomeDictionary.Type.getType(biometype);
            checks.add((event,query) -> {
                Biome biome = query.getWorld(event).getBiome(query.getPos(event));
                return BiomeDictionary.getTypes(biome).contains(type);
            });
        } else {
            Set<BiomeDictionary.Type> types = new HashSet<>();
            for (String s : biomeTypes) {
                types.add(BiomeDictionary.Type.getType(s));
            }

            checks.add((event,query) -> {
                Biome biome = query.getWorld(event).getBiome(query.getPos(event));
                return BiomeDictionary.getTypes(biome).stream().anyMatch(s -> types.contains(s));
            });
        }
    }

    private static final int[] EMPTYINTS = new int[0];

    public static <T extends Comparable<T>> BlockState set(BlockState state, IProperty<T> property, String value) {
        Optional<T> optionalValue = property.parseValue(value);
        if (optionalValue.isPresent()) {
            return state.with(property, optionalValue.get());
        } else {
            return state;
        }
    }

    @Nonnull
    private BiFunction<Event, IEventQuery, BlockPos> parseOffset(String json) {
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
            offsetX = 0;
            offsetY = 0;
            offsetZ = 0;
        }

        if (obj.has("look")) {
            return (event, query) -> {
                RayTraceResult result = LookAtTools.getMovingObjectPositionFromPlayer(query.getWorld(event), query.getPlayer(event), false);
                if (result instanceof BlockRayTraceResult) {
                    return ((BlockRayTraceResult) result).getPos().add(offsetX, offsetY, offsetZ);
                } else {
                    return query.getValidBlockPos(event).add(offsetX, offsetY, offsetZ);
                }
            };

        }
        return (event, query) -> query.getValidBlockPos(event).add(offsetX, offsetY, offsetZ);
    }

    @Nullable
    private BiPredicate<World, BlockPos> parseBlock(String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        if (element.isJsonPrimitive()) {
            String blockname = element.getAsString();
            if (blockname.startsWith("ore:")) {
                // @todo 1.15 ore dictionary?
//                int oreId = OreDictionary.getOreID(blockname.substring(4));
//                return (world, pos) -> isMatchingOreDict(oreId, world.getBlockState(pos).getBlock());
                return (world, pos) -> false;
            } else {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockname));
                if (block == null) {
                    logger.log(Level.ERROR, "Block '" + blockname + "' is not valid!");
                    return null;
                }
                return (world, pos) -> world.getBlockState(pos).getBlock() == block;
            }
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            BiPredicate<World, BlockPos> test;
            if (obj.has("ore")) {
                // @todo 1.15 ore dictionary?
//                int oreId = OreDictionary.getOreID(obj.get("ore").getAsString());
//                test = (world, pos) -> isMatchingOreDict(oreId, world.getBlockState(pos).getBlock());
                test = (world, pos) -> false;
            } else if (obj.has("block")) {
                String blockname = obj.get("block").getAsString();
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockname));
                if (block == null) {
                    logger.log(Level.ERROR, "Block '" + blockname + "' is not valid!");
                    return null;
                }
                if (obj.has("properties")) {
                    BlockState blockState = block.getDefaultState();
                    JsonArray propArray = obj.get("properties").getAsJsonArray();
                    for (JsonElement el : propArray) {
                        JsonObject propObj = el.getAsJsonObject();
                        String name = propObj.get("name").getAsString();
                        String value = propObj.get("value").getAsString();
                        for (IProperty<?> key : blockState.getProperties()) {
                            if (name.equals(key.getName())) {
                                blockState = set(blockState, key, value);
                            }
                        }
                    }
                    BlockState finalBlockState = blockState;
                    test = (world, pos) -> world.getBlockState(pos) == finalBlockState;
                } else {
                    test = (world, pos) -> world.getBlockState(pos).getBlock() == block;
                }
            } else {
                test = (world, pos) -> true;
            }

            if (obj.has("mod")) {
                String mod = obj.get("mod").getAsString();
                BiPredicate<World, BlockPos> finalTest = test;
                test = (world, pos) -> finalTest.test(world, pos) && mod.equals(world.getBlockState(pos).getBlock().getRegistryName().getNamespace());
            }
            if (obj.has("energy")) {
                Predicate<Integer> energy = getExpression(obj.get("energy"), logger);
                if (energy != null) {
                    Direction side;
                    if (obj.has("side")) {
                        side = Direction.byName(obj.get("side").getAsString().toLowerCase());
                    } else {
                        side = null;
                    }
                    BiPredicate<World, BlockPos> finalTest = test;
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
                BiPredicate<World, BlockPos> finalTest = test;
                test = (world, pos) -> finalTest.test(world, pos) && contains(world, pos, side, items);
            }

            return test;
        } else {
            logger.log(Level.ERROR, "Block description '" + json + "' is not valid!");
        }
        return null;
    }

    protected List<Predicate<ItemStack>> getItems(JsonElement itemObj) {
        List<Predicate<ItemStack>> items = new ArrayList<>();
        if (itemObj.isJsonObject()) {
            Predicate<ItemStack> matcher = getMatcher(itemObj.getAsJsonObject(), logger);
            if (matcher != null) {
                items.add(matcher);
            }
        } else if (itemObj.isJsonArray()) {
            for (JsonElement element : itemObj.getAsJsonArray()) {
                JsonObject obj = element.getAsJsonObject();
                Predicate<ItemStack> matcher = getMatcher(obj, logger);
                if (matcher != null) {
                    items.add(matcher);
                }
            }
        } else {
            logger.log(Level.ERROR, "Item description is not valid!");
        }
        return items;
    }

    private boolean isMatchingOreDict(int oreId, Block block) {
//        ItemStack stack = new ItemStack(block);
//        int[] oreIDs = stack.isEmpty() ? EMPTYINTS : OreDictionary.getOreIDs(stack);
//        return isMatchingOreId(oreIDs, oreId);
        // @todo 1.15 oredict
        return false;
    }

    private void addBlocksCheck(AttributeMap map) {

        BiFunction<Event, IEventQuery, BlockPos> posFunction;
        if (map.has(BLOCKOFFSET)) {
            posFunction = parseOffset(map.get(BLOCKOFFSET));
        } else {
            posFunction = (event, query) -> query.getValidBlockPos(event);
        }

        List<String> blocks = map.getList(BLOCK);
        if (blocks.size() == 1) {
            String json = blocks.get(0);
            BiPredicate<World, BlockPos> blockMatcher = parseBlock(json);
            if (blockMatcher != null) {
                checks.add((event, query) -> {
                    BlockPos pos = posFunction.apply(event, query);
                    return pos != null && blockMatcher.test(query.getWorld(event), pos);
                });
            }
        } else {
            List<BiPredicate<World, BlockPos>> blockMatchers = new ArrayList<>();
            for (String block : blocks) {
                BiPredicate<World, BlockPos> blockMatcher = parseBlock(block);
                if (blockMatcher == null) {
                    return;
                }
                blockMatchers.add(blockMatcher);
            }

            checks.add((event,query) -> {
                BlockPos pos = posFunction.apply(event, query);
                if (pos != null) {
                    World world = query.getWorld(event);
                    for (BiPredicate<World, BlockPos> matcher : blockMatchers) {
                        if (matcher.test(world, pos)) {
                            return true;
                        }
                    }
                }
                return false;
            });
        }
    }

    private static boolean isMatchingOreId(int[] oreIDs, int oreId) {
        if (oreIDs.length > 0) {
            for (int id : oreIDs) {
                if (id == oreId) {
                    return true;
                }
            }
        }
        return false;
    }


    private void addMinTimeCheck(AttributeMap map) {
        final int mintime = map.get(MINTIME);
        checks.add((event,query) -> {
            int time = (int) query.getWorld(event).getGameTime();
            return (time % 24000) >= mintime;
        });
    }

    private void addMaxTimeCheck(AttributeMap map) {
        final int maxtime = map.get(MAXTIME);
        checks.add((event,query) -> {
            int time = (int) query.getWorld(event).getGameTime();
            return (time % 24000) <= maxtime;
        });
    }

    private void addMinSpawnDistCheck(AttributeMap map) {
        final Float d = map.get(MINSPAWNDIST) * map.get(MINSPAWNDIST);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            double sqdist = pos.distanceSq(query.getWorld(event).getSpawnPoint());
            return sqdist >= d;
        });
    }

    private void addMaxSpawnDistCheck(AttributeMap map) {
        final Float d = map.get(MAXSPAWNDIST) * map.get(MAXSPAWNDIST);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            double sqdist = pos.distanceSq(query.getWorld(event).getSpawnPoint());
            return sqdist <= d;
        });
    }


    private void addMinLightCheck(AttributeMap map) {
        final int minlight = map.get(MINLIGHT);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            return query.getWorld(event).getLight(pos) >= minlight;
        });
    }

    private void addMaxLightCheck(AttributeMap map) {
        final int maxlight = map.get(MAXLIGHT);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            return query.getWorld(event).getLight(pos) <= maxlight;
        });
    }

    private void addMinAdditionalDifficultyCheck(AttributeMap map) {
        final Float mindifficulty = map.get(MINDIFFICULTY);
        checks.add((event,query) -> query.getWorld(event).getDifficultyForLocation(query.getPos(event)).getAdditionalDifficulty() >= mindifficulty);
    }

    private void addMaxAdditionalDifficultyCheck(AttributeMap map) {
        final Float maxdifficulty = map.get(MAXDIFFICULTY);
        checks.add((event,query) -> query.getWorld(event).getDifficultyForLocation(query.getPos(event)).getAdditionalDifficulty() <= maxdifficulty);
    }

    private void addMaxHeightCheck(AttributeMap map) {
        final int maxheight = map.get(MAXHEIGHT);
        checks.add((event,query) -> query.getY(event) <= maxheight);
    }

    private void addMinHeightCheck(AttributeMap map) {
        final int minheight = map.get(MINHEIGHT);
        checks.add((event,query) -> query.getY(event) >= minheight);
    }


    public boolean match(Event event, IEventQuery query) {
        for (BiFunction<Event, IEventQuery, Boolean> rule : checks) {
            if (!rule.apply(event, query)) {
                return false;
            }
        }
        return true;
    }

    private static Predicate<Integer> getExpression(String expression, Logger logger) {
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
            logger.log(Level.ERROR, "Bad expression '" + expression + "'!");
            return null;
        }
    }

    private static Predicate<Integer> getExpression(JsonElement element, Logger logger) {
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isNumber()) {
                int amount = element.getAsInt();
                return i -> i == amount;
            } else {
                return getExpression(element.getAsString(), logger);
            }
        } else {
            logger.log(Level.ERROR, "Bad expression!");
            return null;
        }
    }

    private static Predicate<ItemStack> getMatcher(String name, Logger logger) {
        ItemStack stack = Tools.parseStack(name, logger);
        if (!stack.isEmpty()) {
            // Stack matching
            if (name.contains("/") && name.contains("@")) {
                return s -> ItemStack.areItemsEqual(s, stack) && ItemStack.areItemStackTagsEqual(s, stack);
            } else if (name.contains("/")) {
                return s -> ItemStack.areItemsEqualIgnoreDurability(s, stack) && ItemStack.areItemStackTagsEqual(s, stack);
            } else if (name.contains("@")) {
                return s -> ItemStack.areItemsEqual(s, stack);
            } else {
                return s -> s.getItem() == stack.getItem();
            }
        }
        return null;
    }

    private static Predicate<ItemStack> getMatcher(JsonObject obj, Logger logger) {
        if (obj.has("empty")) {
            boolean empty = obj.get("empty").getAsBoolean();
            return s -> s.isEmpty() == empty;
        }

        String name = obj.get("item").getAsString();
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
        if (item == null) {
            logger.log(Level.ERROR, "Unknown item '" + name + "'!");
            return null;
        }

        Predicate<ItemStack> test;
        if (obj.has("damage")) {
            Predicate<Integer> damage = getExpression(obj.get("damage"), logger);
            if (damage == null) {
                return null;
            }
            test = s -> s.getItem() == item && damage.test(s.getDamage());
        } else {
            test = s -> s.getItem() == item;
        }

        if (obj.has("count")) {
            Predicate<Integer> count = getExpression(obj.get("count"), logger);
            if (count != null) {
                Predicate<ItemStack> finalTest = test;
                test = s -> finalTest.test(s) && count.test(s.getCount());
            }
        }
        if (obj.has("ore")) {
            // @todo 1.15 ore dictionary
//            int oreId = OreDictionary.getOreID(obj.get("ore").getAsString());
//            Predicate<ItemStack> finalTest = test;
//            test = s -> finalTest.test(s) && isMatchingOreId(s.isEmpty() ? EMPTYINTS : OreDictionary.getOreIDs(s), oreId);
            test = s -> false;
        }
        if (obj.has("mod")) {
            String mod = obj.get("mod").getAsString();
            Predicate<ItemStack> finalTest = test;
            test = s -> finalTest.test(s) && "mod".equals(s.getItem().getRegistryName().getNamespace());
        }
        if (obj.has("nbt")) {
            List<Predicate<CompoundNBT>> nbtMatchers = getNbtMatchers(obj, logger);
            if (nbtMatchers != null) {
                Predicate<ItemStack> finalTest = test;
                test = s -> finalTest.test(s) && nbtMatchers.stream().allMatch(p -> p.test(s.getTag()));
            }
        }
        if (obj.has("energy")) {
            Predicate<Integer> energy = getExpression(obj.get("energy"), logger);
            if (energy != null) {
                Predicate<ItemStack> finalTest = test;
                test = s -> finalTest.test(s) && energy.test(getEnergy(s));
            }
        }

        return test;
    }

    private static int getEnergy(ItemStack stack) {
        return stack.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    private boolean contains(World world, BlockPos pos, @Nullable Direction side, @Nonnull List<Predicate<ItemStack>> matchers) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).map(h -> {
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

    private int getEnergy(World world, BlockPos pos, @Nullable Direction side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            return tileEntity.getCapability(CapabilityEnergy.ENERGY, side).map(h -> h.getEnergyStored()).orElse(0);
        }
        return 0;
    }

    private static List<Predicate<CompoundNBT>> getNbtMatchers(JsonObject obj, Logger logger) {
        JsonArray nbtArray = obj.getAsJsonArray("nbt");
        return getNbtMatchers(nbtArray, logger);
    }

    private static List<Predicate<CompoundNBT>> getNbtMatchers(JsonArray nbtArray, Logger logger) {
        List<Predicate<CompoundNBT>> nbtMatchers = new ArrayList<>();
        for (JsonElement element : nbtArray) {
            JsonObject o = element.getAsJsonObject();
            String tag = o.get("tag").getAsString();
            if (o.has("contains")) {
                List<Predicate<CompoundNBT>> subMatchers = getNbtMatchers(o.getAsJsonArray("contains"), logger);
                nbtMatchers.add(tagCompound -> {
                    if (tagCompound != null) {
                        ListNBT list = tagCompound.getList(tag, Constants.NBT.TAG_COMPOUND);
                        for (INBT base : list) {
                            for (Predicate<CompoundNBT> matcher : subMatchers) {
                                if (matcher.test((CompoundNBT) base)) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                });
            } else {
                Predicate<Integer> nbt = getExpression(o.get("value"), logger);
                if (nbt == null) {
                    return null;
                }
                nbtMatchers.add(tagCompound -> nbt.test(tagCompound.getInt(tag)));
            }

        }
        return nbtMatchers;
    }


    public static List<Predicate<ItemStack>> getItems(List<String> itemNames, Logger logger) {
        List<Predicate<ItemStack>> items = new ArrayList<>();
        for (String json : itemNames) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(json);
            if (element.isJsonPrimitive()) {
                String name = element.getAsString();
                Predicate<ItemStack> matcher = getMatcher(name, logger);
                if (matcher != null) {
                    items.add(matcher);
                }
            } else if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                Predicate<ItemStack> matcher = getMatcher(obj, logger);
                if (matcher != null) {
                    items.add(matcher);
                }
            } else {
                logger.log(Level.ERROR, "Item description '" + json + "' is not valid!");
            }
        }
        return items;
    }

    public void addHelmetCheck(AttributeMap map) {
        List<Predicate<ItemStack>> items = getItems(map.getList(HELMET), logger);
        addArmorCheck(items, EquipmentSlotType.HEAD);
    }

    public void addChestplateCheck(AttributeMap map) {
        List<Predicate<ItemStack>> items = getItems(map.getList(CHESTPLATE), logger);
        addArmorCheck(items, EquipmentSlotType.CHEST);
    }

    public void addLeggingsCheck(AttributeMap map) {
        List<Predicate<ItemStack>> items = getItems(map.getList(LEGGINGS), logger);
        addArmorCheck(items, EquipmentSlotType.LEGS);
    }

    public void addBootsCheck(AttributeMap map) {
        List<Predicate<ItemStack>> items = getItems(map.getList(BOOTS), logger);
        addArmorCheck(items, EquipmentSlotType.FEET);
    }

    private void addArmorCheck(List<Predicate<ItemStack>> items, EquipmentSlotType slot) {
        checks.add((event,query) -> {
            PlayerEntity player = query.getPlayer(event);
            if (player != null) {
                ItemStack armorItem = player.getItemStackFromSlot(slot);
                if (!armorItem.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(armorItem)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    public void addHeldItemCheck(AttributeMap map, Key<String> key) {
        List<Predicate<ItemStack>> items = getItems(map.getList(key), logger);
        checks.add((event,query) -> {
            PlayerEntity player = query.getPlayer(event);
            if (player != null) {
                ItemStack mainhand = player.getHeldItemMainhand();
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

    public void addOffHandItemCheck(AttributeMap map) {
        List<Predicate<ItemStack>> items = getItems(map.getList(OFFHANDITEM), logger);
        checks.add((event,query) -> {
            PlayerEntity player = query.getPlayer(event);
            if (player != null) {
                ItemStack offhand = player.getHeldItemOffhand();
                if (!offhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(offhand)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    public void addBothHandsItemCheck(AttributeMap map) {
        List<Predicate<ItemStack>> items = getItems(map.getList(BOTHHANDSITEM), logger);
        checks.add((event,query) -> {
            PlayerEntity player = query.getPlayer(event);
            if (player != null) {
                ItemStack offhand = player.getHeldItemOffhand();
                if (!offhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(offhand)) {
                            return true;
                        }
                    }
                }
                ItemStack mainhand = player.getHeldItemMainhand();
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

    private void addStateCheck(AttributeMap map) {
        String s = map.get(STATE);
        String[] split = StringUtils.split(s, '=');
        String state;
        String value;
        try {
            state = split[0];
            value = split[1];
        } catch (Exception e) {
            logger.log(Level.ERROR, "Bad state=value specifier '" + s + "'!");
            return;
        }

        checks.add((event, query) -> value.equals(compatibility.getState(query.getWorld(event), state)));
    }

    private void addPStateCheck(AttributeMap map) {
        String s = map.get(PSTATE);
        String[] split = StringUtils.split(s, '=');
        String state;
        String value;
        try {
            state = split[0];
            value = split[1];
        } catch (Exception e) {
            logger.log(Level.ERROR, "Bad state=value specifier '" + s + "'!");
            return;
        }

        checks.add((event, query) -> value.equals(compatibility.getPlayerState(query.getPlayer(event), state)));
    }

    private void addSummerCheck(AttributeMap map) {
        Boolean s = map.get(SUMMER);
        checks.add((event, query) -> s == compatibility.isSummer(query.getWorld(event)));
    }

    private void addWinterCheck(AttributeMap map) {
        Boolean s = map.get(WINTER);
        checks.add((event, query) -> s == compatibility.isWinter(query.getWorld(event)));
    }

    private void addSpringCheck(AttributeMap map) {
        Boolean s = map.get(SPRING);
        checks.add((event, query) -> s == compatibility.isSpring(query.getWorld(event)));
    }

    private void addAutumnCheck(AttributeMap map) {
        Boolean s = map.get(AUTUMN);
        checks.add((event, query) -> s == compatibility.isAutumn(query.getWorld(event)));
    }

    private void addGameStageCheck(AttributeMap map) {
        String stage = map.get(GAMESTAGE);
        checks.add((event, query) -> compatibility.hasGameStage(query.getPlayer(event), stage));
    }

    private void addInCityCheck(AttributeMap map) {
        if (map.get(INCITY)) {
            checks.add((event,query) -> compatibility.isCity(query, event));
        } else {
            checks.add((event,query) -> !compatibility.isCity(query, event));
        }
    }

    private void addInStreetCheck(AttributeMap map) {
        if (map.get(INSTREET)) {
            checks.add((event,query) -> compatibility.isStreet(query, event));
        } else {
            checks.add((event,query) -> !compatibility.isStreet(query, event));
        }
    }

    private void addInSphereCheck(AttributeMap map) {
        if (map.get(INSPHERE)) {
            checks.add((event,query) -> compatibility.inSphere(query, event));
        } else {
            checks.add((event,query) -> !compatibility.inSphere(query, event));
        }
    }

    private void addInBuildingCheck(AttributeMap map) {
        if (map.get(INBUILDING)) {
            checks.add((event,query) -> compatibility.isBuilding(query, event));
        } else {
            checks.add((event,query) -> !compatibility.isBuilding(query, event));
        }
    }

    public void addBaubleCheck(AttributeMap map, Key<String> key, Supplier<int[]> slotSupplier) {
        List<Predicate<ItemStack>> items = getItems(map.getList(key), logger);
        checks.add((event,query) -> {
            PlayerEntity player = query.getPlayer(event);
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
