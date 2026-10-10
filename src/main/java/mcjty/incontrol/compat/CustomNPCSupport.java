package mcjty.incontrol.compat;

import mcjty.incontrol.ErrorHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class CustomNPCSupport {

    public static Entity getNpcEntity(ServerLevel level, int cloneTab, String cloneName) {
        try {
            Class<?> apiClass = Class.forName("noppes.npcs.api.NpcAPI");
            Object api = apiClass.getMethod("Instance").invoke(null);
            Object clones = apiClass.getMethod("getClones").invoke(api);
            Object world = apiClass.getMethod("getIWorld", ServerLevel.class).invoke(api, level);
            Object npc = Class.forName("noppes.npcs.api.handler.ICloneHandler")
                    .getMethod("get", int.class, String.class, Class.forName("noppes.npcs.api.IWorld"))
                    .invoke(clones, cloneTab, cloneName, world);
            Entity entity = (Entity) Class.forName("noppes.npcs.api.entity.IEntity").getMethod("getMCEntity").invoke(npc);
            entity.getPersistentData().putInt("InControlNatSpawnTab", cloneTab);
            entity.getPersistentData().putString("InControlNatSpawnName", cloneName);
            return entity;
        } catch (ReflectiveOperationException | RuntimeException e) {
            ErrorHandler.error("Error spawning NPC (" + e.getMessage() + ")");
            return null;
        }
    }

    public static EntityType<?> getNPCEntityType() {
        try {
            return (EntityType<?>) Class.forName("noppes.npcs.CustomEntities").getField("entityCustomNpc").get(null);
        } catch (ReflectiveOperationException e) {
            ErrorHandler.error("Unsupported CustomNPCs entity API: " + e.getMessage());
            return null;
        }
    }

    public static boolean isNPC(Entity entity) {
        return entity.getType() == getNPCEntityType();
    }

    public static boolean hasNPCInterface(Entity entity) {
        try {
            return Class.forName("noppes.npcs.entity.EntityNPCInterface").isInstance(entity);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
