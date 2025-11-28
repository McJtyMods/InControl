package mcjty.incontrol.compat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class CustomNPCSupport {

    public static Entity getNpcEntity(ServerLevel level, int cloneTab, String cloneName) {
        // @todo 1.21
//        try {
//            Entity entity = NpcAPI.Instance().getClones().get(cloneTab, cloneName, NpcAPI.Instance().getIWorld(level)).getMCEntity();
//            entity.getPersistentData().putInt("InControlNatSpawnTab", cloneTab);
//            entity.getPersistentData().putString("InControlNatSpawnName", cloneName);
//            return entity;
//        } catch (Exception e) {
//            ErrorHandler.error("Error spawning NPC (" + e.getMessage() + ")");
//            return null;
//        }
        return null;
    }

    public static EntityType<?> getNPCEntityType() {
//        return CustomEntities.entityCustomNpc;
        return null;
    }

    public static boolean isNPC(Entity entity) {
//        return entity.getType() == CustomEntities.entityCustomNpc;
        return false;
    }

    public static boolean hasNPCInterface(Entity entity) {
//        return entity instanceof EntityNPCInterface;
        return false;
    }
}
