package baubles.api;

import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import baubles.api.inv.BaublesInventoryWrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;

/**
 * @author Azanor
 */
public class BaublesApi 
{	
	/**
	 * Retrieves the baubles inventory capability handler for the supplied player
	 */
	public static IBaublesItemHandler getBaublesHandler(PlayerEntity player)
	{
		return player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
	}
		
	/**
	 * Retrieves the baubles capability handler wrapped as a IInventory for the supplied player
	 */
	@Deprecated
	public static IInventory getBaubles(PlayerEntity player)
	{
		return new BaublesInventoryWrapper(player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null));
	}
}
