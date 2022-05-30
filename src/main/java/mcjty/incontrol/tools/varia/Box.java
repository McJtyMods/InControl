package mcjty.incontrol.tools.varia;

import net.minecraft.core.BlockPos;

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

    public BlockPos randomPos(Random random) {
        return new BlockPos(
                minX + random.nextInt(maxX - minX+1),
                minY + random.nextInt(maxY - minY+1),
                minZ + random.nextInt(maxZ - minZ+1));
    }
}
