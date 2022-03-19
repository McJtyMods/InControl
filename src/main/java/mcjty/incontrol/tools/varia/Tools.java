package mcjty.incontrol.tools.varia;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.incontrol.ErrorHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class Tools {

    public static RegistryKey<World> getDimensionKey(IWorld world) {
        if (world instanceof World) {
            return ((World) world).dimension();
        } else if (world instanceof IServerWorld) {
            return ((IServerWorld) world).getLevel().dimension();
        } else {
            throw new IllegalStateException("Not possible to get a dimension key here!");
        }
    }

    /// Returns empty string on invalid biomes
    @Nonnull
    public static String getBiomeId(Biome biome) {
        if (biome.getRegistryName() == null) {
            Optional<MutableRegistry<Biome>> biomeRegistry = DynamicRegistries.builtin().registry(Registry.BIOME_REGISTRY);
            return biomeRegistry.map(r -> r.getResourceKey(biome).map(key -> key.location().toString()).orElse("")).orElse("");
        } else {
            return biome.getRegistryName().toString();
        }
    }

    public static Pair<Float, ItemStack> parseStackWithFactor(String name, Logger logger) {
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
            return Pair.of(v, parseStack(name.substring(i+1), logger));
        }

        return Pair.of(1.0f, parseStack(name, logger));
    }

    public static Pair<Float, ItemStack> parseStackWithFactor(JsonObject obj, Logger logger) {
        float factor = 1.0f;
        if (obj.has("factor")) {
            factor = obj.get("factor").getAsFloat();
        }
        ItemStack stack = parseStack(obj, logger);
        if (stack == null) {
            return null;
        }
        return Pair.of(factor, stack);
    }

    @Nonnull
    public static ItemStack parseStack(String name, Logger logger) {
        if (name.contains("/")) {
            String[] split = StringUtils.split(name, "/");
            ItemStack stack = parseStackNoNBT(split[0], logger);
            if (stack.isEmpty()) {
                return stack;
            }
            CompoundNBT nbt;
            try {
                nbt = JsonToNBT.parseTag(split[1]);
            } catch (CommandSyntaxException e) {
                ErrorHandler.error("Error parsing NBT in '" + name + "'!");
                return ItemStack.EMPTY;
            }
            stack.setTag(nbt);
            return stack;
        } else {
            return parseStackNoNBT(name, logger);
        }
    }

    @Nullable
    public static ItemStack parseStack(JsonObject obj, Logger logger) {
        if (obj.has("empty")) {
            return ItemStack.EMPTY;
        }
        String name = obj.get("item").getAsString();
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
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
            CompoundNBT tag = null;
            try {
                tag = JsonToNBT.parseTag(nbt);
            } catch (CommandSyntaxException e) {
                ErrorHandler.error("Error parsing json '" + nbt + "'!");
                return ItemStack.EMPTY;
            }
            stack.setTag(tag);
        }
        return stack;
    }

    private static ItemStack parseStackNoNBT(String name, Logger logger) {
        if (name.contains("@")) {
            String[] split = StringUtils.split(name, "@");
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0]));
            if (item == null) {
                return ItemStack.EMPTY;
            }
            int meta = 0;
            try {
                meta = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                ErrorHandler.error("Unknown item '" + name + "'!");
                return ItemStack.EMPTY;
            }
            // @todo 1.15 Meta? Support properties?
            return new ItemStack(item, 1);
        } else {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
            if (item == null) {
                return ItemStack.EMPTY;
            }
            return new ItemStack(item);
        }
    }


    public static ServerWorld getServerWorld(IWorld world) {
        ServerWorld sw;
        if (world instanceof ServerWorld) {
            sw = (ServerWorld) world;
        } else if (world instanceof WorldGenRegion) {
            sw = ((WorldGenRegion) world).getLevel();
        } else {
            throw new IllegalStateException("No world found!");
        }
        return sw;
    }
}
