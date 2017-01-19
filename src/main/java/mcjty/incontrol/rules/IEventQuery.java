package mcjty.incontrol.rules;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IEventQuery {

    World getWorld(Object o);

    BlockPos getPos(Object o);

    int getY(Object o);

    Entity getEntity(Object o);

    DamageSource getSource(Object o);
}
