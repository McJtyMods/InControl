package mcjty.incontrol.compat;

import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.platform.EntityData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class CustomNPCSupport {
    private CustomNPCSupport() {}
    public static Entity getNpcEntity(ServerLevel level, int cloneTab, String cloneName) {
        Object api = OptionalApi.invoke(OptionalApi.type("noppes.npcs.api.NpcAPI"), "Instance");
        Object clones = OptionalApi.invoke(api, "getClones");
        Object world = OptionalApi.invoke(api, "getIWorld", level);
        Object clone = OptionalApi.invoke(clones, "get", cloneTab, cloneName, world);
        Object result = OptionalApi.invoke(clone, "getMCEntity");
        if (result instanceof Entity entity) {
            EntityData.get(entity).putInt("InControlNatSpawnTab", cloneTab);
            EntityData.get(entity).putString("InControlNatSpawnName", cloneName);
            return entity;
        }
        ErrorHandler.error("Cannot spawn CustomNPC clone: a compatible Fabric CustomNPCs API is required");
        return null;
    }
    public static EntityType<?> getNPCEntityType() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.fromNamespaceAndPath("customnpcs", "customnpc")).orElse(null);
    }
    public static boolean isNPC(Entity entity) { return entity.getType() == getNPCEntityType(); }
    public static boolean hasNPCInterface(Entity entity) {
        Class<?> type = OptionalApi.type("noppes.npcs.entity.EntityNPCInterface");
        return type != null && type.isInstance(entity);
    }
}
