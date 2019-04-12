package baubles.api.cap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IBaublesItemHandler extends IItemHandlerModifiable {

    boolean isItemValidForSlot(int slot, ItemStack stack, EntityLivingBase player);

    /**
     * Used internally to prevent equip/unequip events from triggering when they shouldn't
     *
     * @return
     */
    boolean isEventBlocked();

    void setEventBlock(boolean blockEvents);

    /**
     * Used internally for syncing. Indicates if the inventory has changed since last sync
     *
     * @return
     */
    boolean isChanged(int slot);

    void setChanged(int slot, boolean changed);
}
