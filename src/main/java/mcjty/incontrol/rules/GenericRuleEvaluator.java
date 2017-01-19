package mcjty.incontrol.rules;

import mcjty.incontrol.InControl;
import mcjty.incontrol.cache.StructureCache;
import mcjty.incontrol.typed.AttributeMap;
import mcjty.incontrol.varia.Tools;
import mcjty.lib.tools.EntityTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.function.BiFunction;

import static mcjty.incontrol.rules.RuleKeys.*;


public class GenericRuleEvaluator {

    private final List<BiFunction<Event, IEventQuery, Boolean>> checks = new ArrayList<>();

    public GenericRuleEvaluator(AttributeMap map) {
        addChecks(map);
    }

    private void addChecks(AttributeMap map) {
        if (map.has(RANDOM)) {
            addRandomCheck(map);
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
        if (map.has(HOSTILE)) {
            addHostileCheck(map);
        }
        if (map.has(PASSIVE)) {
            addPassiveCheck(map);
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
        if (map.has(DIMENSION)) {
            addDimensionCheck(map);
        }

        if (map.has(MINSPAWNDIST)) {
            addMinSpawnDistCheck(map);
        }
        if (map.has(MAXSPAWNDIST)) {
            addMaxSpawnDistCheck(map);
        }

        if (map.has(MINLIGHT)) {
            addMinLightCheck(map);
        }
        if (map.has(MAXLIGHT)) {
            addMaxLightCheck(map);
        }

        if (map.has(MOB)) {
            addMobsCheck(map);
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
        if (map.has(MOD)) {
            addModsCheck(map);
        }
        if (map.has(BLOCK)) {
            addBlocksCheck(map);
        }
        if (map.has(BIOME)) {
            addBiomesCheck(map);
        }

        if (map.has(STRUCTURE)) {
            addStructureCheck(map);
        }

        if (map.has(MINCOUNT)) {
            addMinCountCheck(map);
        }
        if (map.has(MAXCOUNT)) {
            addMaxCountCheck(map);
        }
    }

    private static Random rnd = new Random();

    private void addRandomCheck(AttributeMap map) {
        final float r = map.get(RANDOM);
        checks.add((event,query) -> rnd.nextFloat() < r);
    }

    private void addSeeSkyCheck(AttributeMap map) {
        if (map.get(SEESKY)) {
            checks.add((event,query) -> {
                return query.getWorld(event).canBlockSeeSky(query.getPos(event));
            });
        } else {
            checks.add((event,query) -> {
                return !query.getWorld(event).canBlockSeeSky(query.getPos(event));
            });
        }
    }

    private void addHostileCheck(AttributeMap map) {
        if (map.get(HOSTILE)) {
            checks.add((event,query) -> query.getEntity(event) instanceof IMob);
        } else {
            checks.add((event,query) -> !(query.getEntity(event) instanceof IMob));
        }
    }

    private void addPassiveCheck(AttributeMap map) {
        if (map.get(PASSIVE)) {
            checks.add((event,query) -> (query.getEntity(event) instanceof IAnimals && !(query.getEntity(event) instanceof IMob)));
        } else {
            checks.add((event,query) -> !(query.getEntity(event) instanceof IAnimals && !(query.getEntity(event) instanceof IMob)));
        }
    }

    private void addMobsCheck(AttributeMap map) {
        List<String> mobs = map.getList(MOB);
        if (mobs.size() == 1) {
            String name = mobs.get(0);
            String id = EntityTools.fixEntityId(name);
            Class<? extends Entity> clazz = EntityTools.findClassById(id);
            if (clazz != null) {
                checks.add((event,query) -> clazz.equals(query.getEntity(event).getClass()));
            } else {
                InControl.logger.log(Level.ERROR, "Unknown mob '" + name + "'!");
            }
        } else {
            Set<Class> classes = new HashSet<>();
            for (String name : mobs) {
                String id = EntityTools.fixEntityId(name);
                Class<? extends Entity> clazz = EntityTools.findClassById(id);
                if (clazz != null) {
                    classes.add(clazz);
                } else {
                    InControl.logger.log(Level.ERROR, "Unknown mob '" + name + "'!");
                }
            }
            if (!classes.isEmpty()) {
                checks.add((event,query) -> classes.contains(query.getEntity(event).getClass()));
            }
        }
    }

    private void addDimensionCheck(AttributeMap map) {
        List<Integer> dimensions = map.getList(DIMENSION);
        if (dimensions.size() == 1) {
            Integer dim = dimensions.get(0);
            checks.add((event,query) -> query.getWorld(event).provider.getDimension() == dim);
        } else {
            Set<Integer> dims = new HashSet<>(dimensions);
            checks.add((event,query) -> dims.contains(query.getWorld(event).provider.getDimension()));
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
            checks.add((event,query) -> query.getWorld(event).getDifficulty() == finalDiff);
        } else {
            InControl.logger.log(Level.ERROR, "Unknown difficulty '" + difficulty + "'! Use one of 'easy', 'normal', 'hard',  or 'peaceful'");
        }
    }

    private void addWeatherCheck(AttributeMap map) {
        String weather = map.get(WEATHER);
        boolean raining = weather.toLowerCase().startsWith("rain");
        boolean thunder = weather.toLowerCase().startsWith("thunder");
        if (raining) {
            checks.add((event,query) -> query.getWorld(event).isRaining());
        } else if (thunder) {
            checks.add((event,query) -> query.getWorld(event).isThundering());
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
        checks.add((event,query) -> {
            Biome biome = query.getWorld(event).getBiome(query.getPos(event));
            return biome.getTempCategory() == finalCat;
        });
    }

    private void addStructureCheck(AttributeMap map) {
        String structure = map.get(STRUCTURE);
        checks.add((event,query) -> {
            return StructureCache.CACHE.isInStructure(query.getWorld(event), structure, query.getPos(event));
        });
    }

    private void addBiomesCheck(AttributeMap map) {
        List<String> biomes = map.getList(BIOME);
        if (biomes.size() == 1) {
            String biomename = biomes.get(0);
            checks.add((event,query) -> {
                Biome biome = query.getWorld(event).getBiome(query.getPos(event));
                return biomename.equals(biome.getBiomeName());
            });
        } else {
            Set<String> biomenames = new HashSet<>(biomes);
            checks.add((event,query) -> {
                Biome biome = query.getWorld(event).getBiome(query.getPos(event));
                return biomenames.contains(biome.getBiomeName());
            });
        }
    }

    private void addBlocksCheck(AttributeMap map) {
        List<String> blocks = map.getList(BLOCK);
        if (blocks.size() == 1) {
            String blockname = blocks.get(0);
            checks.add((event,query) -> {
                BlockPos pos = query.getPos(event);
                ResourceLocation registryName = query.getWorld(event).getBlockState(pos.down()).getBlock().getRegistryName();
                if (registryName == null) {
                    return false;
                }
                String name = registryName.toString();
                return blockname.equals(name);
            });
        } else {
            Set<String> blocknames = new HashSet<>(blocks);
            checks.add((event,query) -> {
                BlockPos pos = query.getPos(event);
                ResourceLocation registryName = query.getWorld(event).getBlockState(pos.down()).getBlock().getRegistryName();
                if (registryName == null) {
                    return false;
                }
                String name = registryName.toString();
                return blocknames.contains(name);
            });
        }
    }


    private void addModsCheck(AttributeMap map) {
        List<String> mods = map.getList(MOD);
        if (mods.size() == 1) {
            String modid = mods.get(0);
            checks.add((event,query) -> {
                String id = Tools.findModID(query.getEntity(event));
                return modid.equals(id);
            });
        } else {
            Set<String> modids = new HashSet<>();
            for (String modid : mods) {
                modids.add(modid);
            }
            checks.add((event,query) -> {
                String id = Tools.findModID(query.getEntity(event));
                return modids.contains(id);
            });
        }
    }

    private void addMinCountCheck(AttributeMap map) {
        final String mincount = map.get(MINCOUNT);
        String[] splitted = StringUtils.split(mincount, ',');
        Class<?> entityClass = null;
        int amount;
        try {
            amount = Integer.parseInt(splitted[0]);
        } catch (NumberFormatException e) {
            InControl.logger.log(Level.ERROR, "Bad amount for mincount '" + splitted[0] + "'!");
            return;
        }
        if (splitted.length > 1) {
            String id = EntityTools.fixEntityId(splitted[1]);
            entityClass = EntityTools.findClassById(id);
            if (entityClass == null) {
                InControl.logger.log(Level.ERROR, "Unknown mob '" + splitted[1] + "'!");
                return;
            }
        }

        Class<?> finalEntityClass = entityClass;
        checks.add((event,query) -> {
            int count = query.getWorld(event).countEntities(finalEntityClass == null ? query.getEntity(event).getClass() : finalEntityClass);
            return count >= amount;
        });
    }

    private void addMaxCountCheck(AttributeMap map) {
        final String maxcount = map.get(MAXCOUNT);
        String[] splitted = StringUtils.split(maxcount, ',');
        Class<?> entityClass = null;
        int amount;
        try {
            amount = Integer.parseInt(splitted[0]);
        } catch (NumberFormatException e) {
            InControl.logger.log(Level.ERROR, "Bad amount for maxcount '" + splitted[0] + "'!");
            return;
        }
        if (splitted.length > 1) {
            String id = EntityTools.fixEntityId(splitted[1]);
            entityClass = EntityTools.findClassById(id);
            if (entityClass == null) {
                InControl.logger.log(Level.ERROR, "Unknown mob '" + splitted[1] + "'!");
                return;
            }
        }

        Class<?> finalEntityClass = entityClass;
        checks.add((event,query) -> {
            int count = query.getWorld(event).countEntities(finalEntityClass == null ? query.getEntity(event).getClass() : finalEntityClass);
            return count <= amount;
        });
    }

    private void addMinTimeCheck(AttributeMap map) {
        final int mintime = map.get(MINTIME);
        checks.add((event,query) -> {
            int time = (int) query.getWorld(event).getWorldTime();
            return time >= mintime;
        });
    }

    private void addMaxTimeCheck(AttributeMap map) {
        final int maxtime = map.get(MAXTIME);
        checks.add((event,query) -> {
            int time = (int) query.getWorld(event).getWorldTime();
            return time <= maxtime;
        });
    }

    private void addMinSpawnDistCheck(AttributeMap map) {
        final Float d = map.get(MINSPAWNDIST) * map.get(MINSPAWNDIST);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            double sqdist = pos.distanceSq(query.getWorld(event).getSpawnPoint());
            return sqdist >= d;
        });
    }

    private void addMaxSpawnDistCheck(AttributeMap map) {
        final Float d = map.get(MAXSPAWNDIST) * map.get(MAXSPAWNDIST);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            double sqdist = pos.distanceSq(query.getWorld(event).getSpawnPoint());
            return sqdist <= d;
        });
    }


    private void addMinLightCheck(AttributeMap map) {
        final int minlight = map.get(MINLIGHT);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            return query.getWorld(event).getLight(pos, true) >= minlight;
        });
    }

    private void addMaxLightCheck(AttributeMap map) {
        final int maxlight = map.get(MAXLIGHT);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            return query.getWorld(event).getLight(pos, true) <= maxlight;
        });
    }

    private void addMinAdditionalDifficultyCheck(AttributeMap map) {
        final Float mindifficulty = map.get(MINDIFFICULTY);
        checks.add((event,query) -> query.getWorld(event).getDifficultyForLocation(query.getPos(event)).getAdditionalDifficulty() >= mindifficulty);
    }

    private void addMaxAdditionalDifficultyCheck(AttributeMap map) {
        final Float maxdifficulty = map.get(MAXDIFFICULTY);
        checks.add((event,query) -> query.getWorld(event).getDifficultyForLocation(query.getPos(event)).getAdditionalDifficulty() <= maxdifficulty);
    }

    private void addMaxHeightCheck(AttributeMap map) {
        final int maxheight = map.get(MAXHEIGHT);
        checks.add((event,query) -> query.getY(event) <= maxheight);
    }

    private void addMinHeightCheck(AttributeMap map) {
        final int minheight = map.get(MINHEIGHT);
        checks.add((event,query) -> query.getY(event) >= minheight);
    }

    public boolean match(Event event, IEventQuery query) {
        for (BiFunction<Event, IEventQuery, Boolean> rule : checks) {
            if (!rule.apply(event, query)) {
                return false;
            }
        }
        return true;
    }

}
