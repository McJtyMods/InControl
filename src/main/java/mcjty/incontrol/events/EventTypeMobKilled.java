package mcjty.incontrol.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class EventTypeMobKilled implements EventType {

    private List<ResourceLocation> mobs;
    private boolean playerKill = false;

    public EventTypeMobKilled() {
    }

    @Override
    public Type type() {
        return Type.MOB_KILLED;
    }

    public List<ResourceLocation> getMobs() {
        return mobs;
    }

    public boolean isPlayerKill() {
        return playerKill;
    }

    @Override
    public boolean parse(JsonObject object) {
        mobs = new ArrayList<>();
        JsonElement mob = object.get("mob");
        if (mob == null) {
            ErrorHandler.error("No mob specified!");
            return false;
        }
        if (mob.isJsonArray()) {
            for (JsonElement element : mob.getAsJsonArray()) {
                ResourceLocation rl = new ResourceLocation(element.getAsString());
                if (!ForgeRegistries.ENTITY_TYPES.containsKey(rl)) {
                    ErrorHandler.error("Unknown mob '" + rl + "'!");
                    return true;
                }
                mobs.add(rl);
            }
        } else {
            ResourceLocation rl = new ResourceLocation(mob.getAsString());
            if (!ForgeRegistries.ENTITY_TYPES.containsKey(rl)) {
                ErrorHandler.error("Unknown mob '" + rl + "'!");
                return true;
            }
            mobs.add(rl);
        }
        if (mobs.isEmpty()) {
            ErrorHandler.error("No mobs specified!");
            return false;
        }
        if (object.has("player")) {
            playerKill = object.get("player").getAsBoolean();
        }
        return true;
    }
}
