package mcjty.incontrol.tools.varia;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;

public class LookAtTools {

    public static HitResult getMovingObjectPositionFromPlayer(LevelAccessor worldIn, Player playerIn, boolean useLiquids) {
        float pitch = playerIn.getXRot();
        float yaw = playerIn.getYRot();
        Vec3 vec3 = getPlayerEyes(playerIn);
        float f2 = Mth.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f3 = Mth.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f4 = -Mth.cos(-pitch * 0.017453292F);
        float f5 = Mth.sin(-pitch * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double reach = 5.0D;
        if (playerIn instanceof ServerPlayer) {
            // @todo 1.15? Where is reach?
//            reach = ((ServerPlayerEntity)playerIn).interactionManager.getBlockReachDistance();
        }
        Vec3 vec31 = vec3.add(f6 * reach, f5 * reach, f7 * reach);
        ClipContext context = new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, useLiquids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, playerIn);
        return worldIn.clip(context);
    }

    private static Vec3 getPlayerEyes(Player playerIn) {
        double x = playerIn.getX();
        double y = playerIn.getY() + playerIn.getEyeHeight();
        double z = playerIn.getZ();
        return new Vec3(x, y, z);
    }
}
