package baubles.api.cap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public interface IBaublesItemHandler extends IItemHandlerModifiable {	
	
	public boolean isItemValidForSlot(int slot, ItemStack stack, LivingEntity player);

	/**
	 * Used internally to prevent equip/unequip events from triggering when they shouldn't
	 * @return
	 */
	public boolean isEventBlocked();
	public void setEventBlock(boolean blockEvents);

	/**
	 * Used internally for syncing. Indicates if the inventory has changed since last sync
	 * @return
	 */
	boolean isChanged(int slot);
	void setChanged(int slot, boolean changed);
}
