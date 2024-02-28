package mcjty.incontrol.tools.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import mcjty.incontrol.tools.varia.Tools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.neoforged.neoforge.common.capabilities.ForgeCapabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class TestingTools {

    public static <T extends Comparable<T>> BlockState set(BlockState state, Property<T> property, String value) {
        Optional<T> optionalValue = property.getValue(value);
        return optionalValue.map(t -> state.setValue(property, t)).orElse(state);
    }

    public static List<Predicate<ItemStack>> getItemsJson(JsonElement itemObj) {
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

    public static Predicate<Integer> getExpression(JsonElement element) {
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

    public static boolean contains(LevelAccessor world, BlockPos pos, @Nullable Direction side, @Nonnull List<Predicate<ItemStack>> matchers) {
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

    public static int getEnergy(LevelAccessor world, BlockPos pos, @Nullable Direction side) {
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

    public static boolean isSlimeChunk(ChunkPos cp, LevelAccessor world) {
        long seed = 0;
        if (world instanceof WorldGenLevel level) {
            seed = level.getSeed();
        }
        return WorldgenRandom.seedSlimeChunk(cp.x, cp.z, seed, 987234911L).nextInt(10) == 0;
    }

    public static boolean isFakePlayer(Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }

        if (entity instanceof FakePlayer) {
            return true;
        }

        // If this returns false it is still possible we have a fake player. Try to find the player in the list of online players
        PlayerList playerList = entity.getCommandSenderWorld().getServer().getPlayerList();
        ServerPlayer playerByUUID = playerList.getPlayer(((Player) entity).getGameProfile().getId());
        if (playerByUUID == null) {
            // The player isn't online. Then it can't be real
            return true;
        }

        // The player is in the list. But is it this player?
        return entity != playerByUUID;
    }

    public static boolean isRealPlayer(Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }
        return !isFakePlayer(entity);
    }

    public static void warn(String message) {
        InControl.setup.getLogger().warn(message);
    }

    public static boolean isChunkInvalid(LevelAccessor world, BlockPos pos) {
        LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
            return true;
        }
        return false;
    }

    @Nullable
    public static NumberResult parseNumberCheck(JsonElement element) {
        if (!element.isJsonObject()) {
            ErrorHandler.error("Number check needs to be an object!");
            return null;
        }
        JsonObject object = element.getAsJsonObject();
        if (!object.has("name")) {
            ErrorHandler.error("Number check needs to have a 'name' field!");
            return null;
        }
        if (!object.has("expression")) {
            ErrorHandler.error("Number check needs to have a 'expression' field!");
            return null;
        }
        String number = object.get("name").getAsString();
        String expression = object.get("expression").getAsString();
        Predicate<Integer> test = Tools.parseExpression(expression);
        return new NumberResult(number, test);
    }

    public record NumberResult(String number, Predicate<Integer> test) {
    }
}
