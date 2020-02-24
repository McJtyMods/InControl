package mcjty.incontrol.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class BaublesSupport {

    private static final int[] EMPTY = new int[0];

    // @todo 1.15
    public static int[] getAmuletSlots() {
//        return BaubleType.AMULET.getValidSlots();
        return EMPTY;
    }
//
    public static int[] getBeltSlots() {
//        return BaubleType.BELT.getValidSlots();
        return EMPTY;
    }
//
    public static int[] getBodySlots() {
//        return BaubleType.BODY.getValidSlots();
        return EMPTY;
    }
//
    public static int[] getCharmSlots() {
//        return BaubleType.CHARM.getValidSlots();
        return EMPTY;
    }
//
    public static int[] getHeadSlots() {
//        return BaubleType.HEAD.getValidSlots();
        return EMPTY;
    }
//
    public static int[] getRingSlots() {
//        return BaubleType.RING.getValidSlots();
        return EMPTY;
    }
//
    public static int[] getTrinketSlots() {
//        return BaubleType.TRINKET.getValidSlots();
        return EMPTY;
    }
//
    public static ItemStack getStack(PlayerEntity player, int slot) {
//        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
//        if (handler == null) {
//            return ItemStack.EMPTY;
//        }
//        return handler.getStackInSlot(slot);
        return ItemStack.EMPTY;
    }
}
