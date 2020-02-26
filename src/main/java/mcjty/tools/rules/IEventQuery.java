package mcjty.tools.rules;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IEventQuery<T> {

    World getWorld(T o);

    /// Get the position directly from the event
    BlockPos getPos(T o);
    /// Get the position from the event corrected to correspond to a position more likely containing a valid block
    BlockPos getValidBlockPos(T o);

    int getY(T o);

    Entity getEntity(T o);

    DamageSource getSource(T o);

    Entity getAttacker(T o);

    PlayerEntity getPlayer(T o);

    /// Get the item that is being placed
    ItemStack getItem(T o);
}
