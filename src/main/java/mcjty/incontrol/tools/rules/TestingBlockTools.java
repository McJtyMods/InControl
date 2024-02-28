package mcjty.incontrol.tools.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.tools.varia.LookAtTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class TestingBlockTools
{
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
    public static BiPredicate<LevelAccessor, BlockPos> parseBlock(String json) {
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
                                blockState = TestingTools.set(blockState, key, value);
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
                Predicate<Integer> energy = TestingTools.getExpression(obj.get("energy"));
                if (energy != null) {
                    Direction side;
                    if (obj.has("side")) {
                        side = Direction.byName(obj.get("side").getAsString().toLowerCase());
                    } else {
                        side = null;
                    }
                    BiPredicate<LevelAccessor, BlockPos> finalTest = test;
                    test = (world, pos) -> finalTest.test(world, pos) && energy.test(TestingTools.getEnergy(world, pos, side));
                }
            }
            if (obj.has("contains")) {
                Direction side;
                if (obj.has("side")) {
                    side = Direction.byName(obj.get("energyside").getAsString().toLowerCase());
                } else {
                    side = null;
                }
                List<Predicate<ItemStack>> items = TestingTools.getItemsJson(obj.get("contains"));
                BiPredicate<LevelAccessor, BlockPos> finalTest = test;
                test = (world, pos) -> finalTest.test(world, pos) && TestingTools.contains(world, pos, side, items);
            }

            return test;
        } else {
            ErrorHandler.error("Block description is not valid!");
        }
        return null;
    }

    @Nonnull
    public static BiFunction<Object, IEventQuery, BlockPos> parseOffset(String json) {
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
}
