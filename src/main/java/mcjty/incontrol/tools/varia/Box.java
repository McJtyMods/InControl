package mcjty.incontrol.tools.varia;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.Random;

public record Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    public boolean isValid() {
        if (minX >= maxX) {
            return false;
        }
        if (minY >= maxY) {
            return false;
        }
        if (minZ >= maxZ) {
            return false;
        }
        return true;
    }

    public boolean in(BlockPos pos) {
        if (pos.getX() < minX || pos.getX() > maxX) {
            return false;
        }
        if (pos.getY() < minY || pos.getY() > maxY) {
            return false;
        }
        if (pos.getZ() < minZ || pos.getZ() > maxZ) {
            return false;
        }
        return true;
    }

    public boolean in(BlockPos pos, int maxOffset) {
        if (pos.getX() < minX-maxOffset || pos.getX() > maxX+maxOffset) {
            return false;
        }
        if (pos.getY() < minY-maxOffset || pos.getY() > maxY+maxOffset) {
            return false;
        }
        if (pos.getZ() < minZ-maxOffset || pos.getZ() > maxZ+maxOffset) {
            return false;
        }
        return true;
    }

    public BlockPos randomPos(Random random, @Nullable BlockPos groupCenterPos, int groupDistance) {
        if (groupCenterPos != null) {
            if (groupDistance == 0) {
                return groupCenterPos;  // All spawn on the same position
            }
            // Try to find a position near the center that's also in the box (or near to it)
            while (true) {
                BlockPos attempt = new BlockPos(
                        groupCenterPos.getX() - groupDistance + random.nextInt(groupDistance * 2),
                        groupCenterPos.getY() - groupDistance + random.nextInt(groupDistance * 2),
                        groupCenterPos.getZ() - groupDistance + random.nextInt(groupDistance * 2));
                if (in(attempt, 2)) {
                    return attempt;
                }
            }
        }
        return new BlockPos(
                minX + random.nextInt(maxX - minX+1),
                minY + random.nextInt(maxY - minY+1),
                minZ + random.nextInt(maxZ - minZ+1));
    }
}
