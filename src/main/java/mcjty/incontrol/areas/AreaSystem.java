package mcjty.incontrol.areas;

import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class AreaSystem {

    private static final Map<String, Area> areas = new HashMap<>();

    public static void addArea(Area area) {
        areas.put(area.name(), area);
    }

    public static void reloadRules() {
        areas.clear();
        AreaParser.readRules("areas.json");
    }

    @Nullable
    public static String isInArea(LevelAccessor level, int x, int y, int z) {
        for (Area area : areas.values()) {
            if (area.isInArea(x, y, z)) {
                return area.name();
            }
        }
        return null;
    }

    public static Area getArea(String name) {
        return areas.get(name);
    }

}
