package mcjty.tools.varia;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LookAtTools {

    public static RayTraceResult getMovingObjectPositionFromPlayer(World worldIn, PlayerEntity playerIn, boolean useLiquids) {
        float pitch = playerIn.rotationPitch;
        float yaw = playerIn.rotationYaw;
        Vec3d vec3 = getPlayerEyes(playerIn);
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
        Vec3d vec31 = vec3.add(f6 * reach, f5 * reach, f7 * reach);
        RayTraceContext context = new RayTraceContext(vec3, vec31, RayTraceContext.BlockMode.COLLIDER, useLiquids ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE, playerIn);
        return worldIn.rayTraceBlocks(context);
    }

    private static Vec3d getPlayerEyes(PlayerEntity playerIn) {
        double x = playerIn.getPosX();
        double y = playerIn.getPosY() + playerIn.getEyeHeight();
        double z = playerIn.getPosZ();
        return new Vec3d(x, y, z);
    }
}
