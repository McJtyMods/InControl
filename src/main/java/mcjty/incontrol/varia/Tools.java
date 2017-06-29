package mcjty.incontrol.varia;

import mcjty.incontrol.InControl;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Tools {

    public static Map<String, String> modSourceID = null;

    public static String findModID(Object obj) {
        if (modSourceID == null) {
            modSourceID = new HashMap<>();
            for (ModContainer mod : Loader.instance().getModList()) {
                modSourceID.put(mod.getSource().getName(), mod.getModId());
            }

            modSourceID.put("1.8.0.jar", "minecraft");
            modSourceID.put("1.8.8.jar", "minecraft");
            modSourceID.put("1.8.9.jar", "minecraft");
            modSourceID.put("Forge", "minecraft");
        }


        String path;
        try {
            if (obj instanceof Class) {
                path = ((Class) obj).getProtectionDomain().getCodeSource().getLocation().toString();
            } else {
                path = obj.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
            }
        } catch (Exception e) {
            return "<Unknown>";
        }
        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "<Unknown>";
        }
        String modName = "<Unknown>";
        for (String s : modSourceID.keySet()) {
            if (path.contains(s)) {
                modName = modSourceID.get(s);
                break;
            }
        }
        if (modName.equals("Minecraft Coder Pack")) {
            modName = "minecraft";
        } else if (modName.equals("Forge")) {
            modName = "minecraft";
        }

        return modName;
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
            return Pair.of(v, parseStack(name.substring(i+1)));
        }

        return Pair.of(1.0f, parseStack(name));
    }

    public static ItemStack parseStack(String name) {
        if (name.contains("/")) {
            String[] split = StringUtils.split(name, "/");
            ItemStack stack = parseStackNoNBT(split[0]);
            if (ItemStackTools.isEmpty(stack)) {
                return stack;
            }
            NBTTagCompound nbt;
            try {
                nbt = JsonToNBT.getTagFromJson(split[1]);
            } catch (NBTException e) {
                InControl.logger.log(Level.ERROR, "Error parsing NBT in '" + name + "'!");
                return ItemStackTools.getEmptyStack();
            }
            stack.setTagCompound(nbt);
            return stack;
        } else {
            return parseStackNoNBT(name);
        }
    }

    private static ItemStack parseStackNoNBT(String name) {
        if (name.contains("@")) {
            String[] split = StringUtils.split(name, "@");
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0]));
            if (item == null) {
                return ItemStackTools.getEmptyStack();
            }
            int meta = 0;
            try {
                meta = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                InControl.logger.log(Level.ERROR, "Unknown item '" + name + "'!");
                return ItemStackTools.getEmptyStack();
            }
            return new ItemStack(item, 1, meta);
        } else {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
            if (item == null) {
                return ItemStackTools.getEmptyStack();
            }
            return new ItemStack(item);
        }
    }
}
