package mcjty.incontrol.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.InControl;
import mcjty.incontrol.typed.Attribute;
import mcjty.incontrol.typed.AttributeMap;
import mcjty.incontrol.typed.GenericAttributeMapFactory;
import mcjty.lib.tools.EntityTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.function.Function;

import static mcjty.incontrol.rules.RuleKeys.*;

public class PotentialSpawnRule {

    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    private static final GenericAttributeMapFactory MOB_FACTORY = new GenericAttributeMapFactory();

    static {
        FACTORY
                .attribute(Attribute.create(MINTIME))
                .attribute(Attribute.create(MAXTIME))
                .attribute(Attribute.create(MINLIGHT))
                .attribute(Attribute.create(MAXLIGHT))
                .attribute(Attribute.create(MINHEIGHT))
                .attribute(Attribute.create(MAXHEIGHT))
                .attribute(Attribute.create(MINDIFFICULTY))
                .attribute(Attribute.create(MAXDIFFICULTY))
                .attribute(Attribute.create(MINSPAWNDIST))
                .attribute(Attribute.create(MAXSPAWNDIST))
                .attribute(Attribute.create(RANDOM))
                .attribute(Attribute.create(SEESKY))
                .attribute(Attribute.create(WEATHER))
                .attribute(Attribute.create(TEMPCATEGORY))
                .attribute(Attribute.create(DIFFICULTY))
                .attribute(Attribute.createMulti(BLOCK))
                .attribute(Attribute.createMulti(BIOME))
                .attribute(Attribute.createMulti(DIMENSION))
        ;

        MOB_FACTORY
                .attribute(Attribute.create(MOB))
                .attribute(Attribute.create(WEIGHT))
                .attribute(Attribute.create(GROUPCOUNTMIN))
                .attribute(Attribute.create(GROUPCOUNTMAX))
        ;
    }

    private final List<Function<WorldEvent.PotentialSpawns, Boolean>> checks = new ArrayList<>();
    private List<Biome.SpawnListEntry> spawnEntries = new ArrayList<>();

    private PotentialSpawnRule(AttributeMap map) {
        if (!map.has(MOBS)) {
            InControl.logger.log(Level.ERROR, "No mobs specified!");
            return;
        }
        if (!makeSpawnEntries(map)) {
            return;
        }

        if (map.has(MINTIME)) {
            addMinTimeCheck(map);
        }
        if (map.has(MAXTIME)) {
            addMaxTimeCheck(map);
        }

        if (map.has(MINHEIGHT)) {
            addMinHeightCheck(map);
        }
        if (map.has(MAXHEIGHT)) {
            addMaxHeightCheck(map);
        }

        if (map.has(MINLIGHT)) {
            addMinLightCheck(map);
        }
        if (map.has(MAXLIGHT)) {
            addMaxLightCheck(map);
        }

        if (map.has(MINSPAWNDIST)) {
            addMinSpawnDistCheck(map);
        }
        if (map.has(MAXSPAWNDIST)) {
            addMaxSpawnDistCheck(map);
        }

        if (map.has(MINDIFFICULTY)) {
            addMinAdditionalDifficultyCheck(map);
        }
        if (map.has(MAXDIFFICULTY)) {
            addMaxAdditionalDifficultyCheck(map);
        }

        if (map.has(SEESKY)) {
            addSeeSkyCheck(map);
        }
        if (map.has(WEATHER)) {
            addWeatherCheck(map);
        }
        if (map.has(TEMPCATEGORY)) {
            addTempCategoryCheck(map);
        }
        if (map.has(DIFFICULTY)) {
            addDifficultyCheck(map);
        }
        if (map.has(BLOCK)) {
            addBlocksCheck(map);
        }
        if (map.has(BIOME)) {
            addBiomesCheck(map);
        }
        if (map.has(DIMENSION)) {
            addDimensionCheck(map);
        }
        if (map.has(RANDOM)) {
            addRandomCheck(map);
        }

    }

    private boolean makeSpawnEntries(AttributeMap map) {
        for (AttributeMap mobMap : map.getList(MOBS)) {
            String id = EntityTools.fixEntityId(mobMap.get(MOB));
            Class<? extends Entity> clazz = EntityTools.findClassById(id);
            if (clazz == null) {
                InControl.logger.log(Level.ERROR, "Cannot find mob '" + mobMap.get(MOB) + "'!");
                return false;
            }

            Integer weight = mobMap.get(WEIGHT);
            if (weight == null) {
                weight = 1;
            }
            Integer groupCountMin = mobMap.get(GROUPCOUNTMIN);
            if (groupCountMin == null) {
                groupCountMin = 1;
            }
            Integer groupCountMax = mobMap.get(GROUPCOUNTMAX);
            if (groupCountMax == null) {
                groupCountMax = Math.max(groupCountMin, 1);
            }
            Biome.SpawnListEntry entry = new Biome.SpawnListEntry((Class<? extends EntityLiving>) clazz,
                    weight, groupCountMin, groupCountMax);
            spawnEntries.add(entry);
        }
        return true;
    }

    private static Random rnd = new Random();

    private void addRandomCheck(AttributeMap map) {
        final float r = map.get(RANDOM);
        checks.add(event -> {
            return rnd.nextFloat() < r;
        });
    }

    private void addSeeSkyCheck(AttributeMap map) {
        if (map.get(SEESKY)) {
            checks.add(event -> {
                BlockPos pos = event.getPos();
                return event.getWorld().canBlockSeeSky(pos);
            });
        } else {
            checks.add(event -> {
                BlockPos pos = event.getPos();
                return !event.getWorld().canBlockSeeSky(pos);
            });
        }
    }


    private void addDimensionCheck(AttributeMap map) {
        List<Integer> dimensions = map.getList(DIMENSION);
        if (dimensions.size() == 1) {
            Integer dim = dimensions.get(0);
            checks.add(event -> {
                return event.getWorld().provider.getDimension() == dim;
            });
        } else {
            Set<Integer> dims = new HashSet<>(dimensions);
            checks.add(event -> {
                return dims.contains(event.getWorld().provider.getDimension());
            });
        }
    }

    private void addDifficultyCheck(AttributeMap map) {
        String difficulty = map.get(DIFFICULTY).toLowerCase();
        EnumDifficulty diff = null;
        for (EnumDifficulty d : EnumDifficulty.values()) {
            if (d.getDifficultyResourceKey().endsWith("." + difficulty)) {
                diff = d;
                break;
            }
        }
        if (diff != null) {
            EnumDifficulty finalDiff = diff;
            checks.add(event -> {
                return event.getWorld().getDifficulty() == finalDiff;
            });
        } else {
            InControl.logger.log(Level.ERROR, "Unknown difficulty '" + difficulty + "'! Use one of 'easy', 'normal', 'hard',  or 'peaceful'");
        }
    }

    private void addWeatherCheck(AttributeMap map) {
        String weather = map.get(WEATHER).toLowerCase();
        boolean raining = weather.startsWith("rain");
        boolean thunder = weather.startsWith("thunder");
        if (raining) {
            checks.add(event -> {
                return event.getWorld().isRaining();
            });
        } else if (thunder) {
            checks.add(event -> {
                return event.getWorld().isThundering();
            });
        } else {
            InControl.logger.log(Level.ERROR, "Unknown weather '" + weather + "'! Use 'rain' or 'thunder'");
        }
    }

    private void addTempCategoryCheck(AttributeMap map) {
        String tempcategory = map.get(TEMPCATEGORY).toLowerCase();
        Biome.TempCategory cat = null;
        if ("cold".equals(tempcategory)) {
            cat = Biome.TempCategory.COLD;
        } else if ("medium".equals(tempcategory)) {
            cat = Biome.TempCategory.MEDIUM;
        } else if ("warm".equals(tempcategory)) {
            cat = Biome.TempCategory.WARM;
        } else if ("ocean".equals(tempcategory)) {
            cat = Biome.TempCategory.OCEAN;
        } else {
            InControl.logger.log(Level.ERROR, "Unknown tempcategory '" + tempcategory + "'! Use one of 'cold', 'medium', 'warm',  or 'ocean'");
            return;
        }

        Biome.TempCategory finalCat = cat;
        checks.add(event -> {
            Biome biome = event.getWorld().getBiome(event.getPos());
            return biome.getTempCategory() == finalCat;
        });
    }

    private void addBiomesCheck(AttributeMap map) {
        List<String> biomes = map.getList(BIOME);
        if (biomes.size() == 1) {
            String biomename = biomes.get(0);
            checks.add(event -> {
                Biome biome = event.getWorld().getBiome(event.getPos());
                return biomename.equals(biome.getBiomeName());
            });
        } else {
            Set<String> biomenames = new HashSet<>(biomes);
            checks.add(event -> {
                Biome biome = event.getWorld().getBiome(event.getPos());
                return biomenames.contains(biome.getBiomeName());
            });
        }
    }

    private void addBlocksCheck(AttributeMap map) {
        List<String> blocks = map.getList(BLOCK);
        if (blocks.size() == 1) {
            String blockname = blocks.get(0);
            checks.add(event -> {
                BlockPos pos = event.getPos();
                ResourceLocation registryName = event.getWorld().getBlockState(pos.down()).getBlock().getRegistryName();
                if (registryName == null) {
                    return false;
                }
                String name = registryName.toString();
                return blockname.equals(name);
            });
        } else {
            Set<String> blocknames = new HashSet<>(blocks);
            checks.add(event -> {
                BlockPos pos = event.getPos();
                ResourceLocation registryName = event.getWorld().getBlockState(pos.down()).getBlock().getRegistryName();
                if (registryName == null) {
                    return false;
                }
                String name = registryName.toString();
                return blocknames.contains(name);
            });
        }
    }


    private void addMinTimeCheck(AttributeMap map) {
        final int mintime = map.get(MINTIME);
        checks.add(event -> {
            int time = (int) event.getWorld().getWorldTime();
            return time >= mintime;
        });
    }

    private void addMaxTimeCheck(AttributeMap map) {
        final int maxtime = map.get(MAXTIME);
        checks.add(event -> {
            int time = (int) event.getWorld().getWorldTime();
            return time <= maxtime;
        });
    }

    private void addMinSpawnDistCheck(AttributeMap map) {
        Float d = map.get(MINSPAWNDIST) * map.get(MINSPAWNDIST);
        checks.add(event -> {
            BlockPos pos = event.getPos();
            double sqdist = pos.distanceSqToCenter(0, 0, 0);
            return sqdist >= d;
        });
    }

    private void addMaxSpawnDistCheck(AttributeMap map) {
        Float d = map.get(MAXSPAWNDIST) * map.get(MAXSPAWNDIST);
        checks.add(event -> {
            BlockPos pos = event.getPos();
            double sqdist = pos.distanceSqToCenter(0, 0, 0);
            return sqdist <= d;
        });
    }


    private void addMinLightCheck(AttributeMap map) {
        final int minlight = map.get(MINLIGHT);
        checks.add(event -> {
            BlockPos pos = event.getPos();
            return event.getWorld().getLight(pos, true) >= minlight;
        });
    }

    private void addMaxLightCheck(AttributeMap map) {
        final int maxlight = map.get(MAXLIGHT);
        checks.add(event -> {
            BlockPos pos = event.getPos();
            return event.getWorld().getLight(pos, true) <= maxlight;
        });
    }

    private void addMinAdditionalDifficultyCheck(AttributeMap map) {
        final float mindifficulty = map.get(MINDIFFICULTY);
        checks.add(event -> {
            return event.getWorld().getDifficultyForLocation(event.getPos()).getAdditionalDifficulty() >= mindifficulty;
        });
    }

    private void addMaxAdditionalDifficultyCheck(AttributeMap map) {
        final float maxdifficulty = map.get(MAXDIFFICULTY);
        checks.add(event -> {
            return event.getWorld().getDifficultyForLocation(event.getPos()).getAdditionalDifficulty() <= maxdifficulty;
        });
    }

    private void addMaxHeightCheck(AttributeMap map) {
        final int maxheight = map.get(MAXHEIGHT);
        checks.add(event -> {
            return event.getPos().getY() <= maxheight;
        });
    }

    private void addMinHeightCheck(AttributeMap map) {
        final int minheight = map.get(MINHEIGHT);
        checks.add(event -> {
            return event.getPos().getY() >= minheight;
        });
    }

    public boolean match(WorldEvent.PotentialSpawns event) {
        for (Function<WorldEvent.PotentialSpawns, Boolean> rule : checks) {
            if (!rule.apply(event)) {
                return false;
            }
        }
        return true;
    }

    public List<Biome.SpawnListEntry> getSpawnEntries() {
        return spawnEntries;
    }

    public static PotentialSpawnRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            JsonObject jsonObject = element.getAsJsonObject();
            if (!jsonObject.has("mobs")) {
                return null;
            }

            AttributeMap map = FACTORY.parse(element);

            JsonArray mobs = jsonObject.get("mobs").getAsJsonArray();
            for (JsonElement mob : mobs) {
                AttributeMap mobMap = MOB_FACTORY.parse(mob);
                map.addList(MOBS, mobMap);
            }
            return new PotentialSpawnRule(map);
        }
    }

    public static class SpawnEntryBuilder {
        private String mob;
        private Integer weight = 5;
        private Integer groupCountMin = 2;
        private Integer groupCountMax = 3;

        public SpawnEntryBuilder mob(String mob) {
            this.mob = mob;
            return this;
        }

        public SpawnEntryBuilder weight(Integer weight) {
            this.weight = weight;
            return this;
        }

        public SpawnEntryBuilder groupCountMin(Integer groupCountMin) {
            this.groupCountMin = groupCountMin;
            return this;
        }

        public SpawnEntryBuilder groupCountMax(Integer groupCountMax) {
            this.groupCountMax = groupCountMax;
            return this;
        }


    }
}

