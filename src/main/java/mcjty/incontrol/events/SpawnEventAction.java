package mcjty.incontrol.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.mob.CNPCMob;
import mcjty.incontrol.mob.DefaultMob;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record SpawnEventAction(List<DefaultMob> mobid, int attempts,
                               float mindistance, float maxdistance, int minamount, int maxamount,
                               boolean norestrictions) {

    @Nullable
    public static DefaultMob parseMob(JsonElement mob) {
        ResourceLocation mobid;
        if (mob.isJsonObject()){
            JsonObject object = mob.getAsJsonObject();
            String mobName = object.get("mob").getAsString();
            if (mobName.equals("customnpcs:customnpc")){
                return new CNPCMob(object.get("cloneTab").getAsInt(), object.get("cloneName").getAsString());
            }
            mobid = new ResourceLocation(mobName);
        } else {
            mobid = new ResourceLocation(mob.getAsString());
        }
        if (!ForgeRegistries.ENTITY_TYPES.containsKey(mobid)) {
            ErrorHandler.error("Invalid mob '" + mobid + "' for events rule!");
            return null;
        }
        return new DefaultMob(mobid);
    }
    @Nullable
    static SpawnEventAction parse(JsonObject object) {
        List<DefaultMob> mobs = new ArrayList<>();
        JsonObject value = object.getAsJsonObject("spawn");
        if (value == null) {
            // Valid
            return null;
        }
        JsonElement mob = value.get("mob");
        if (mob.isJsonArray()) {
            for (JsonElement element : mob.getAsJsonArray()) {
                mobs.add(parseMob(element));
            }
        } else {
            mobs.add(parseMob(mob));
        }
        if (mobs.isEmpty()) {
            ErrorHandler.error("No mobs specified for events rule!");
            return null;
        }

        int attempts = 10;
        int mincount = 1;
        int maxcount = 1;
        float mindistance = 0.0f;
        float maxdistance = 10.0f;
        if (value.has("attempts")) {
            attempts = value.getAsJsonPrimitive("attempts").getAsInt();
        }
        if (value.has("mindistance")) {
            mindistance = value.getAsJsonPrimitive("mindistance").getAsFloat();
        }
        if (value.has("maxdistance")) {
            maxdistance = value.getAsJsonPrimitive("maxdistance").getAsFloat();
        }
        if (value.has("mincount")) {
            mincount = value.getAsJsonPrimitive("mincount").getAsInt();
        }
        if (value.has("maxcount")) {
            maxcount = value.getAsJsonPrimitive("maxcount").getAsInt();
        }
        // Check count and distance bounds
        if (mincount > maxcount) {
            ErrorHandler.error("Mincount can't be larger than maxcount for events rule!");
            return null;
        }
        if (mindistance > maxdistance) {
            ErrorHandler.error("Mindistance can't be larger than maxdistance for events rule!");
            return null;
        }
        boolean norestrictions = false;
        if (value.has("norestrictions")) {
            norestrictions = value.getAsJsonPrimitive("norestrictions").getAsBoolean();
        }

        return new SpawnEventAction(mobs, attempts, mindistance, maxdistance, mincount, maxcount, norestrictions);
    }
}
