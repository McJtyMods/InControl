package mcjty.tools.varia;

import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class Box {

    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    private Box(Builder builder) {
        this.minX = builder.minX;
        this.minY = builder.minY;
        this.minZ = builder.minZ;
        this.maxX = builder.maxX;
        this.maxY = builder.maxY;
        this.maxZ = builder.maxZ;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

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

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {

        private int minX;
        private int minY;
        private int minZ;
        private int maxX;
        private int maxY;
        private int maxZ;

        public Builder minimum(BlockPos pos) {
            minX = pos.getX();
            minY = pos.getY();
            minZ = pos.getZ();
            return this;
        }

        public Builder maximum(BlockPos pos) {
            maxX = pos.getX();
            maxY = pos.getY();
            maxZ = pos.getZ();
            return this;
        }

        public Builder center(BlockPos center, int radiusX, int radiusY, int radiusZ) {
            minX = center.getX() - radiusX;
            minY = center.getY() - radiusY;
            minZ = center.getZ() - radiusZ;
            maxX = center.getX() + radiusX;
            maxY = center.getY() + radiusY;
            maxZ = center.getZ() + radiusZ;
            return this;
        }

        public Builder clampY(int minimumY, int maximumY) {
            minY = Math.max(minY, minimumY);
            maxY = Math.min(maxY, maximumY);
            return this;
        }

        public Box build() {
            return new Box(this);
        }
    }
}
