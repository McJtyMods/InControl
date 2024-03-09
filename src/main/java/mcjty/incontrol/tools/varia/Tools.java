package mcjty.incontrol.tools.varia;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.incontrol.ErrorHandler;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public class Tools {

    public static ResourceKey<Level> getDimensionKey(LevelAccessor world) {
        if (world instanceof Level) {
            return ((Level) world).dimension();
        } else if (world instanceof ServerLevelAccessor) {
            return ((ServerLevelAccessor) world).getLevel().dimension();
        } else {
            throw new IllegalStateException("Not possible to get a dimension key here!");
        }
    }

    public static String getBiomeId(Holder<Biome> biomeHolder) {
        return biomeHolder.unwrap().map((key) -> key.location().toString(), (key) -> "[unregistered " + key + "]");
    }

    public static Pair<Float, ItemStack> parseStackWithFactor(String name) {
        int i = 0;
        while (i < name.length() && (Character.isDigit(name.charAt(i)) || name.charAt(i) == '.')) {
            i++;
        }
        if (i < name.length() && name.charAt(i) == '=') {
            String f = name.substring(0, i);
            float v;
            try {
                v = Float.parseFloat(f);
            } catch (NumberFormatException e) {
                v = 1.0f;
            }
            return Pair.of(v, parseStack(name.substring(i + 1)));
        }

        return Pair.of(1.0f, parseStack(name));
    }

    public static Pair<Float, ItemStack> parseStackWithFactor(JsonObject obj) {
        float factor = 1.0f;
        if (obj.has("factor")) {
            factor = obj.get("factor").getAsFloat();
        }
        ItemStack stack = parseStack(obj);
        if (stack == null) {
            return null;
        }
        return Pair.of(factor, stack);
    }

    @Nonnull
    public static ItemStack parseStack(String name) {
        if (name.contains("{")) {
            int idx = name.indexOf('{');
            ItemStack stack = parseStackNoNBT(name.substring(0, idx));
            if (stack.isEmpty()) {
                return stack;
            }
            CompoundTag nbt;
            try {
                nbt = TagParser.parseTag(name.substring(idx));
            } catch (CommandSyntaxException e) {
                ErrorHandler.error("Error parsing NBT in '" + name + "'!");
                return ItemStack.EMPTY;
            }
            stack.setTag(nbt);
            return stack;
        } else if (name.contains("/")) {
            int idx = name.indexOf('/');
            ItemStack stack = parseStackNoNBT(name.substring(0, idx));
            if (stack.isEmpty()) {
                return stack;
            }
            CompoundTag nbt;
            try {
                nbt = TagParser.parseTag(name.substring(idx + 1));
            } catch (CommandSyntaxException e) {
                ErrorHandler.error("Error parsing NBT in '" + name + "'!");
                return ItemStack.EMPTY;
            }
            stack.setTag(nbt);
            return stack;
        } else {
            return parseStackNoNBT(name);
        }
    }

    @Nullable
    public static ItemStack parseStack(JsonObject obj) {
        if (obj.has("empty")) {
            return ItemStack.EMPTY;
        }
        String name = obj.get("item").getAsString();
        Item item = BuiltInRegistries.ITEM.getValue(new ResourceLocation(name));
        if (item == null) {
            ErrorHandler.error("Unknown item '" + name + "'!");
            return null;
        }
        ItemStack stack = new ItemStack(item);
        if (obj.has("damage")) {
            stack.setDamageValue(obj.get("damage").getAsInt());
        }
        if (obj.has("count")) {
            stack.setCount(obj.get("count").getAsInt());
        }
        if (obj.has("nbt")) {
            String nbt = obj.get("nbt").toString();
            CompoundTag tag = null;
            try {
                tag = TagParser.parseTag(nbt);
            } catch (CommandSyntaxException e) {
                ErrorHandler.error("Error parsing json '" + nbt + "'!");
                return ItemStack.EMPTY;
            }
            stack.setTag(tag);
        }
        return stack;
    }

    private static ItemStack parseStackNoNBT(String name) {
        Item item = BuiltInRegistries.ITEM.getValue(new ResourceLocation(name));
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }


    public static ServerLevel getServerWorld(LevelAccessor world) {
        ServerLevel sw;
        if (world instanceof ServerLevel) {
            sw = (ServerLevel) world;
        } else if (world instanceof ServerLevelAccessor) {
            sw = ((ServerLevelAccessor) world).getLevel();
        } else {
            throw new IllegalStateException("No world found!");
        }
        return sw;
    }

    // Parse an expression to a predicate that matches a certain integer. The following
    // operators are supported:
    // greater(x) is true if the number is greater than x (or gt)
    // greaterOrEqual(x) is true if the number is greater or equal to x (or ge)
    // smaller(x) is true if the number is smaller than x (or lt)
    // smallerOrEqual(x) is true if the number is smaller or equal to x (or le)
    // equal(x) is true if the number is equal to x (or eq)
    // notEqual(x) is true if the number is not equal to x (or ne)
    // range(min,max) is true if the number is in the range
    // outsideRange(min,max) is true if the number is outside the range
    // repeat(cycle,min,max) is true for every part of the cycle
    public static Predicate<Integer> parseExpression(String input) {
        // Use startsWith to parse the expression
        String i = input.toLowerCase();
        if (i.startsWith("greater(") || i.startsWith("gt(")) {
            // Get the number
            int number = Integer.parseInt(i.substring(i.indexOf('(') + 1, i.indexOf(')')).trim());
            return value -> value > number;
        } else if (i.startsWith("greaterorequal(") || i.startsWith("ge(")) {
            int number = Integer.parseInt(i.substring(i.indexOf('(') + 1, i.indexOf(')')).trim());
            return value -> value >= number;
        } else if (i.startsWith("smaller(") || i.startsWith("lt(")) {
            int number = Integer.parseInt(i.substring(i.indexOf('(') + 1, i.indexOf(')')).trim());
            return value -> value < number;
        } else if (i.startsWith("smallerorequal(") || i.startsWith("le(")) {
            int number = Integer.parseInt(i.substring(i.indexOf('(') + 1, i.indexOf(')')).trim());
            return value -> value <= number;
        } else if (i.startsWith("equal(") || i.startsWith("eq(")) {
            int number = Integer.parseInt(i.substring(i.indexOf('(') + 1, i.indexOf(')')).trim());
            return value -> value == number;
        } else if (i.startsWith("notequal(") || i.startsWith("ne(")) {
            int number = Integer.parseInt(i.substring(i.indexOf('(') + 1, i.indexOf(')')).trim());
            return value -> value != number;
        } else if (i.startsWith("range(")) {
            int min = Integer.parseInt(i.substring(i.indexOf('(') + 1, i.indexOf(',')));
            int max = Integer.parseInt(i.substring(i.indexOf(',') + 1, i.indexOf(')')));
            return value -> value >= min && value <= max;
        } else if (i.startsWith("outsiderange(")) {
            int min = Integer.parseInt(i.substring(i.indexOf('(') + 1, i.indexOf(',')));
            int max = Integer.parseInt(i.substring(i.indexOf(',') + 1, i.indexOf(')')));
            return value -> value < min || value > max;
        } else if (i.startsWith("repeat(")) {
            int cycle = Integer.parseInt(i.substring(i.indexOf('(') + 1, i.indexOf(',')));
            int min = Integer.parseInt(i.substring(i.indexOf(',') + 1, i.lastIndexOf(',')));
            int max = Integer.parseInt(i.substring(i.lastIndexOf(',') + 1, i.indexOf(')')));
            return value -> {
                int v = value % cycle;
                return v >= min && v <= max;
            };
        } else {
            ErrorHandler.error("Unknown expression '" + input + "'!");
            return null;
        }
    }
}
