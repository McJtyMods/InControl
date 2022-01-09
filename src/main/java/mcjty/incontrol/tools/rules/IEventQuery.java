package mcjty.incontrol.tools.rules;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public interface IEventQuery<T> {

    LevelAccessor getWorld(T o);

    /// Get the position directly from the event
    BlockPos getPos(T o);
    /// Get the position from the event corrected to correspond to a position more likely containing a valid block
    BlockPos getValidBlockPos(T o);

    int getY(T o);

    Entity getEntity(T o);

    DamageSource getSource(T o);

    Entity getAttacker(T o);

    Player getPlayer(T o);

    /// Get the item that is being placed
    ItemStack getItem(T o);
}
