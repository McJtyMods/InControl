package mcjty.incontrol.areas;

import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record Area(ResourceKey<Level> dimension, String name, Type type, BlockPos center, int dimx, int dimy, int dimz) {

    public static Builder create() {
        return new Builder();
    }

    public static boolean parse(JsonObject object, Builder builder) {
        if (object.has("dimension")) {
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(object.get("dimension").getAsString()));
            builder.dimension(key);
        } else {
            ErrorHandler.error("Area has no dimension!");
            return false;
        }
        if (object.has("name")) {
            builder.name(object.get("name").getAsString());
        } else {
            ErrorHandler.error("Area has no name!");
            return false;
        }
        if (object.has("type")) {
            builder.type(Type.valueOf(object.get("type").getAsString().toUpperCase()));
        } else {
            ErrorHandler.error("Area '" + builder.name + "' has no type!");
            return false;
        }
        int x;
        if (object.has("x")) {
            x = object.get("x").getAsInt();
        } else {
            ErrorHandler.error("Area '" + builder.name + "' has no x!");
            return false;
        }
        int y;
        if (object.has("y")) {
            y = object.get("y").getAsInt();
        } else {
            ErrorHandler.error("Area '" + builder.name + "' has no y!");
            return false;
        }
        int z;
        if (object.has("z")) {
            z = object.get("z").getAsInt();
        } else {
            ErrorHandler.error("Area '" + builder.name + "' has no z!");
            return false;
        }
        builder.center(new BlockPos(x, y, z));
        int dimx;
        if (object.has("dimx")) {
            dimx = object.get("dimx").getAsInt();
        } else {
            ErrorHandler.error("Area '" + builder.name + "' has no dimx!");
            return false;
        }
        int dimy;
        if (object.has("dimy")) {
            dimy = object.get("dimy").getAsInt();
        } else {
            ErrorHandler.error("Area '" + builder.name + "' has no dimy!");
            return false;
        }
        int dimz;
        if (object.has("dimz")) {
            dimz = object.get("dimz").getAsInt();
        } else {
            ErrorHandler.error("Area '" + builder.name + "' has no dimz!");
            return false;
        }
        builder.dim(dimx, dimy, dimz);
        return true;
    }

    public boolean isInArea(int x, int y, int z) {
        if (type == Type.BOX) {
            return isInBox(x, y, z);
        } else {
            return isInSphere(x, y, z);
        }
    }

    private boolean isInBox(int x, int y, int z) {
        int dx = Math.abs(x - center.getX());
        int dy = Math.abs(y - center.getY());
        int dz = Math.abs(z - center.getZ());
        return dx <= dimx && dy <= dimy && dz <= dimz;
    }

    private boolean isInSphere(int x, int y, int z) {
        int dx = Math.abs(x - center.getX());
        int dy = Math.abs(y - center.getY());
        int dz = Math.abs(z - center.getZ());
        return dx*dx + dy*dy + dz*dz <= dimx*dimx + dimy*dimy + dimz*dimz;
    }

    enum Type {
        BOX,
        SPHERE
    }

    public static class Builder {
        private String name;
        private ResourceKey<Level> dimension;
        private Type type;
        private BlockPos center;
        private int dimx;
        private int dimy;
        private int dimz;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder dimension(ResourceKey<Level> dimension) {
            this.dimension = dimension;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder center(BlockPos center) {
            this.center = center;
            return this;
        }

        public Builder dim(int dimx, int dimy, int dimz) {
            this.dimx = dimx;
            this.dimy = dimy;
            this.dimz = dimz;
            return this;
        }

        public Area build() {
            return new Area(dimension, name, type, center, dimx, dimy, dimz);
        }
    }
}
