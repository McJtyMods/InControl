package mcjty.incontrol.compat;

import mcjty.incontrol.ErrorHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import noppes.npcs.CustomEntities;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.entity.EntityNPCInterface;

public class CustomNPCSupport {

    public static Entity getNpcEntity(ServerLevel level, int cloneTab, String cloneName) {
        try {
            Entity entity = NpcAPI.Instance().getClones().get(cloneTab, cloneName, NpcAPI.Instance().getIWorld(level)).getMCEntity();
            entity.getPersistentData().putInt("InControlNatSpawnTab", cloneTab);
            entity.getPersistentData().putString("InControlNatSpawnName", cloneName);
            return entity;
        } catch (Exception e) {
            ErrorHandler.error("Error spawning NPC (" + e.getMessage() + ")");
            return null;
        }
    }

    public static EntityType<?> getNPCEntityType() {
        return CustomEntities.entityCustomNpc;
    }

    public static boolean isNPC(Entity entity) {
        return entity.getType() == CustomEntities.entityCustomNpc;
    }

    public static boolean hasNPCInterface(Entity entity) {
        return entity instanceof EntityNPCInterface;
    }
}
