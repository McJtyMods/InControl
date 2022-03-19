package mcjty.incontrol.tools.varia;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;

public class LookAtTools {

    public static RayTraceResult getMovingObjectPositionFromPlayer(IWorld worldIn, PlayerEntity playerIn, boolean useLiquids) {
        float pitch = playerIn.xRot;
        float yaw = playerIn.yRot;
        Vector3d vec3 = getPlayerEyes(playerIn);
        float f2 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f3 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f4 = -MathHelper.cos(-pitch * 0.017453292F);
        float f5 = MathHelper.sin(-pitch * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double reach = 5.0D;
        if (playerIn instanceof ServerPlayerEntity) {
            // @todo 1.15? Where is reach?
//            reach = ((ServerPlayerEntity)playerIn).interactionManager.getBlockReachDistance();
        }
        Vector3d vec31 = vec3.add(f6 * reach, f5 * reach, f7 * reach);
        RayTraceContext context = new RayTraceContext(vec3, vec31, RayTraceContext.BlockMode.COLLIDER, useLiquids ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE, playerIn);
        return worldIn.clip(context);
    }

    private static Vector3d getPlayerEyes(PlayerEntity playerIn) {
        double x = playerIn.getX();
        double y = playerIn.getY() + playerIn.getEyeHeight();
        double z = playerIn.getZ();
        return new Vector3d(x, y, z);
    }
}
