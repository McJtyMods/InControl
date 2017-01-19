package mcjty.incontrol.rules.support;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IEventQuery<T> {

    World getWorld(T o);

    BlockPos getPos(T o);

    int getY(T o);

    Entity getEntity(T o);

    DamageSource getSource(T o);
}
