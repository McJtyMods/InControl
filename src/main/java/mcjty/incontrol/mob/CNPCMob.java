package mcjty.incontrol.mob;

import mcjty.incontrol.compat.CustomNPCSupport;
import mcjty.incontrol.setup.ModSetup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Objects;

public class CNPCMob extends DefaultMob {
    private int cloneTab;
    private String cloneName;

    public CNPCMob(int cloneTab, String cloneName) {
        this.cloneName = cloneName;
        this.cloneTab = cloneTab;
    }

    public int getCloneTab() {
        return cloneTab;
    }

    public void setCloneTab(int cloneTab) {
        this.cloneTab = cloneTab;
    }

    public String getCloneName() {
        return cloneName;
    }

    public void setCloneName(String cloneName) {
        this.cloneName = cloneName;
    }

    public EntityType<?> getType() {
        if (!ModSetup.customnpcs) {
            return null;
        }
        return CustomNPCSupport.getNPCEntityType();
    }

    public boolean matches(Entity entity) {
        if (entity == null) return false;
        if (!entity.getPersistentData().contains("InControlNatSpawnTab") || !entity.getPersistentData().contains("InControlNatSpawnName"))
            return false;
        return entity.getPersistentData().getInt("InControlNatSpawnTab") == cloneTab && entity.getPersistentData().getString("InControlNatSpawnName").equals(cloneName);
    }

    @Override
    public Entity getEntity(ServerLevel level) {
        if (ModSetup.customnpcs) {
            return CustomNPCSupport.getNpcEntity(level, cloneTab, cloneName);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CNPCMob cnpcMob)) return false;
        return cloneTab == cnpcMob.cloneTab && Objects.equals(cloneName, cnpcMob.cloneName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cloneTab, cloneName);
    }
}