package mcjty.incontrol.mob;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class DefaultMob {
    private ResourceLocation mobid;

    public DefaultMob(){
    }

    public DefaultMob(ResourceLocation mobid){
        this.mobid = mobid;
    }

    public ResourceLocation getMobid() {
        return mobid;
    }

    public void setMobid(ResourceLocation mobid) {
        this.mobid = mobid;
    }

    public EntityType<?> getType(){
        return BuiltInRegistries.ENTITY_TYPE.get(mobid);
    }

    public Entity getEntity(ServerLevel level){
        EntityType<?> entityType = getType();
        if (entityType != null) {
            return entityType.create(level);
        }
        return null;
    }
}