package mcjty.incontrol.compat;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class BaublesSupport {

    public static int[] getAmuletSlots() {
        return BaubleType.AMULET.getValidSlots();
    }

    public static int[] getBeltSlots() {
        return BaubleType.BELT.getValidSlots();
    }

    public static int[] getBodySlots() {
        return BaubleType.BODY.getValidSlots();
    }

    public static int[] getCharmSlots() {
        return BaubleType.CHARM.getValidSlots();
    }

    public static int[] getHeadSlots() {
        return BaubleType.HEAD.getValidSlots();
    }

    public static int[] getRingSlots() {
        return BaubleType.RING.getValidSlots();
    }

    public static int[] getTrinketSlots() {
        return BaubleType.TRINKET.getValidSlots();
    }

    public static ItemStack getStack(EntityPlayer player, int slot) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        return handler.getStackInSlot(slot);
    }
}
