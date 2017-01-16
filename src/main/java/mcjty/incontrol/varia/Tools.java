package mcjty.incontrol.varia;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

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
}
