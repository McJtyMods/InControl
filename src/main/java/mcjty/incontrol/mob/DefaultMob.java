package mcjty.incontrol.mob;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

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
        return ForgeRegistries.ENTITY_TYPES.getValue(mobid);
    }

    public Entity getEntity(ServerLevel level){
        EntityType<?> entityType = getType();
        if (entityType != null) {
            return entityType.create(level);
        }
        return null;
    }
}