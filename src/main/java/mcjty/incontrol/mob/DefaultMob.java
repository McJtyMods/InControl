package mcjty.incontrol.mob;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class DefaultMob {
    private Identifier mobid;

    public DefaultMob(){
    }

    public DefaultMob(Identifier mobid){
        this.mobid = mobid;
    }

    public Identifier getMobid() {
        return mobid;
    }

    public void setMobid(Identifier mobid) {
        this.mobid = mobid;
    }

    public EntityType<?> getType(){
        return BuiltInRegistries.ENTITY_TYPE.getValue(mobid);
    }

    public Entity getEntity(ServerLevel level){
        EntityType<?> entityType = getType();
        if (entityType != null) {
            return entityType.create(level, net.minecraft.world.entity.EntitySpawnReason.NATURAL);
        }
        return null;
    }
}