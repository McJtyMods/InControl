package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.InControl;
import mcjty.incontrol.varia.JSonTools;
import mcjty.incontrol.varia.Tools;
import mcjty.lib.tools.EntityTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;


public class SpawnRule {
    private final Event.Result result;
    private final List<Function<LivingSpawnEvent.CheckSpawn, Boolean>> checks = new ArrayList<>();
    private final List<Consumer<LivingSpawnEvent.CheckSpawn>> actions = new ArrayList<>();

    private SpawnRule(Builder builder) {
        if (builder.mintime != null) {
            addMinTimeCheck(builder);
        }
        if (builder.maxtime != null) {
            addMaxTimeCheck(builder);
        }

        if (builder.minheight != null) {
            addMinHeightCheck(builder);
        }
        if (builder.maxheight != null) {
            addMaxHeightCheck(builder);
        }

        if (builder.minlight != null) {
            addMinLightCheck(builder);
        }
        if (builder.maxlight != null) {
            addMaxLightCheck(builder);
        }

        if (builder.minAdditionalDifficulty != null) {
            addMinAdditionalDifficultyCheck(builder);
        }
        if (builder.maxAdditionalDifficulty != null) {
            addMaxAdditionalDifficultyCheck(builder);
        }

        if (builder.hostile != null) {
            addHostileCheck(builder);
        }
        if (builder.passive != null) {
            addPassiveCheck(builder);
        }
        if (builder.seesky != null) {
            addSeeSkyCheck(builder);
        }
        if (builder.weather != null) {
            addWeatherCheck(builder);
        }
        if (builder.tempcategory != null) {
            addTempCategoryCheck(builder);
        }
        if (builder.difficulty != null) {
            addDifficultyCheck(builder);
        }
        if (!builder.mobs.isEmpty()) {
            addMobsCheck(builder);
        }
        if (!builder.mods.isEmpty()) {
            addModsCheck(builder);
        }
        if (!builder.blocks.isEmpty()) {
            addBlocksCheck(builder);
        }
        if (!builder.biomes.isEmpty()) {
            addBiomesCheck(builder);
        }
        if (!builder.dimensions.isEmpty()) {
            addDimensionCheck(builder);
        }

        String br = builder.result.toLowerCase();
        if ("default".equals(br) || br.startsWith("def")) {
            this.result = Event.Result.DEFAULT;
        } else if ("allow".equals(br) || "true".equals(br)) {
            this.result = Event.Result.ALLOW;
        } else {
            this.result = Event.Result.DENY;
        }

        if (builder.healthmultiply != null || builder.healthadd != null) {
            addHealthAction(builder);
        }
    }

    private void addHealthAction(Builder builder) {
        float m = builder.healthmultiply != null ? builder.healthmultiply : 1;
        float a = builder.healthadd != null ? builder.healthadd : 0;
        actions.add(event -> {
            EntityLivingBase entityLiving = event.getEntityLiving();
            if (entityLiving != null) {
                IAttributeInstance entityAttribute = entityLiving.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
                double newMax = entityAttribute.getBaseValue() * m + a;
                entityAttribute.setBaseValue(newMax);
                entityLiving.setHealth((float) newMax);
            }
        });
    }

    private void addSeeSkyCheck(Builder builder) {
        if (builder.seesky) {
            checks.add(event -> {
                BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
                return event.getWorld().canBlockSeeSky(pos);
            });
        } else {
            checks.add(event -> {
                BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
                return !event.getWorld().canBlockSeeSky(pos);
            });
        }
    }

    private void addHostileCheck(Builder builder) {
        if (builder.hostile) {
            checks.add(event -> {
                return event.getEntity() instanceof IMob;
            });
        } else {
            checks.add(event -> {
                return !(event.getEntity() instanceof IMob);
            });
        }
    }

    private void addPassiveCheck(Builder builder) {
        if (builder.passive) {
            checks.add(event -> {
                return (event.getEntity() instanceof IAnimals && !(event.getEntity() instanceof IMob));
            });
        } else {
            checks.add(event -> {
                return !(event.getEntity() instanceof IAnimals && !(event.getEntity() instanceof IMob));
            });
        }
    }

    private void addMobsCheck(Builder builder) {
        if (builder.mobs.size() == 1) {
            String name = builder.mobs.get(0);
            String id = EntityTools.fixEntityId(name);
            Class<? extends Entity> clazz = EntityTools.findClassById(id);
            if (clazz != null) {
                checks.add(event -> {
                    return clazz.equals(event.getEntity().getClass());
                });
            } else {
                InControl.logger.log(Level.ERROR, "Unknown mob '" + name + "'!");
            }
        } else {
            Set<Class> classes = new HashSet<>();
            for (String name : builder.mobs) {
                String id = EntityTools.fixEntityId(name);
                Class<? extends Entity> clazz = EntityTools.findClassById(id);
                if (clazz != null) {
                    classes.add(clazz);
                } else {
                    InControl.logger.log(Level.ERROR, "Unknown mob '" + name + "'!");
                }
            }
            if (!classes.isEmpty()) {
                checks.add(event -> {
                    return classes.contains(event.getEntity().getClass());
                });
            }
        }
    }

    private void addDimensionCheck(Builder builder) {
        if (builder.dimensions.size() == 1) {
            Integer dim = builder.dimensions.get(0);
            checks.add(event -> {
                return event.getWorld().provider.getDimension() == dim;
            });
        } else {
            Set<Integer> dimensions = new HashSet<>(builder.dimensions);
            checks.add(event -> {
                return dimensions.contains(event.getWorld().provider.getDimension());
            });
        }
    }

    private void addDifficultyCheck(Builder builder) {
        String difficulty = builder.difficulty.toLowerCase();
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

    private void addWeatherCheck(Builder builder) {
        String weather = builder.weather;
        boolean raining = weather.toLowerCase().startsWith("rain");
        boolean thunder = weather.toLowerCase().startsWith("thunder");
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

    private void addTempCategoryCheck(Builder builder) {
        String tempcategory = builder.tempcategory.toLowerCase();
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
            Biome biome = event.getWorld().getBiome(new BlockPos(event.getX(), event.getY(), event.getZ()));
            return biome.getTempCategory() == finalCat;
        });
    }

    private void addBiomesCheck(Builder builder) {
        if (builder.biomes.size() == 1) {
            String biomename = builder.biomes.get(0);
            checks.add(event -> {
                Biome biome = event.getWorld().getBiome(new BlockPos(event.getX(), event.getY(), event.getZ()));
                return biomename.equals(biome.getBiomeName());
            });
        } else {
            Set<String> biomenames = new HashSet<>(builder.biomes);
            checks.add(event -> {
                Biome biome = event.getWorld().getBiome(new BlockPos(event.getX(), event.getY(), event.getZ()));
                return biomenames.contains(biome.getBiomeName());
            });
        }
    }

    private void addBlocksCheck(Builder builder) {
        if (builder.blocks.size() == 1) {
            String blockname = builder.blocks.get(0);
            checks.add(event -> {
                BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
                ResourceLocation registryName = event.getWorld().getBlockState(pos.down()).getBlock().getRegistryName();
                if (registryName == null) {
                    return false;
                }
                String name = registryName.toString();
                return blockname.equals(name);
            });
        } else {
            Set<String> blocknames = new HashSet<>(builder.blocks);
            checks.add(event -> {
                BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
                ResourceLocation registryName = event.getWorld().getBlockState(pos.down()).getBlock().getRegistryName();
                if (registryName == null) {
                    return false;
                }
                String name = registryName.toString();
                return blocknames.contains(name);
            });
        }
    }


    private void addModsCheck(Builder builder) {
        if (builder.mods.size() == 1) {
            String modid = builder.mods.get(0);
            checks.add(event -> {
                String id = Tools.findModID(event.getEntity());
                return modid.equals(id);
            });
        } else {
            Set<String> modids = new HashSet<>();
            for (String modid : builder.mods) {
                modids.add(modid);
            }
            checks.add(event -> {
                String id = Tools.findModID(event.getEntity());
                return modids.contains(id);
            });
        }
    }

    private void addMinTimeCheck(Builder builder) {
        checks.add(event -> {
            int time = (int) event.getWorld().getWorldTime();
            return time >= builder.mintime;
        });
    }

    private void addMaxTimeCheck(Builder builder) {
        checks.add(event -> {
            int time = (int) event.getWorld().getWorldTime();
            return time <= builder.maxtime;
        });
    }

    private void addMinLightCheck(Builder builder) {
        checks.add(event -> {
            BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
            return event.getWorld().getLight(pos, true) >= builder.minlight;
        });
    }

    private void addMaxLightCheck(Builder builder) {
        checks.add(event -> {
            BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
            return event.getWorld().getLight(pos, true) <= builder.maxlight;
        });
    }

    private void addMinAdditionalDifficultyCheck(Builder builder) {
        checks.add(event -> {
            return event.getWorld().getDifficultyForLocation(new BlockPos(event.getX(), event.getY(), event.getZ())).getAdditionalDifficulty() >= builder.minAdditionalDifficulty;
        });
    }

    private void addMaxAdditionalDifficultyCheck(Builder builder) {
        checks.add(event -> {
            return event.getWorld().getDifficultyForLocation(new BlockPos(event.getX(), event.getY(), event.getZ())).getAdditionalDifficulty() <= builder.maxAdditionalDifficulty;
        });
    }

    private void addMaxHeightCheck(Builder builder) {
        checks.add(event -> {
            return event.getY() <= builder.maxheight;
        });
    }

    private void addMinHeightCheck(Builder builder) {
        checks.add(event -> {
            return event.getY() >= builder.minheight;
        });
    }

    public boolean match(LivingSpawnEvent.CheckSpawn event) {
        for (Function<LivingSpawnEvent.CheckSpawn, Boolean> rule : checks) {
            if (!rule.apply(event)) {
                return false;
            }
        }
        return true;
    }

    public void action(LivingSpawnEvent.CheckSpawn event) {
        for (Consumer<LivingSpawnEvent.CheckSpawn> action : actions) {
            action.accept(event);
        }
    }

    public Event.Result getResult() {
        return result;
    }

    public static SpawnRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            Builder builder = new Builder();
            JsonObject jsonObject = element.getAsJsonObject();

            // Outputs
            builder.healthmultiply(JSonTools.parseFloat(jsonObject, "healthmultiply"));
            builder.healthadd(JSonTools.parseFloat(jsonObject, "healthadd"));
            if (jsonObject.has("result")) {
                builder.result(jsonObject.get("result").getAsString());
            }

            // Inputs
            builder.mintime(JSonTools.parseInt(jsonObject, "mintime"));
            builder.maxtime(JSonTools.parseInt(jsonObject, "maxtime"));
            builder.minheight(JSonTools.parseInt(jsonObject, "minheight"));
            builder.maxheight(JSonTools.parseInt(jsonObject, "maxheight"));
            builder.minlight(JSonTools.parseInt(jsonObject, "minlight"));
            builder.maxlight(JSonTools.parseInt(jsonObject, "maxlight"));
            builder.minAdditionalDifficulty(JSonTools.parseFloat(jsonObject, "mindifficulty"));
            builder.maxAdditionalDifficulty(JSonTools.parseFloat(jsonObject, "maxdifficulty"));
            builder.passive(JSonTools.parseBool(jsonObject, "passive"));
            builder.hostile(JSonTools.parseBool(jsonObject, "hostile"));
            builder.seesky(JSonTools.parseBool(jsonObject, "seesky"));
            if (jsonObject.has("weather")) {
                builder.weather(jsonObject.get("weather").getAsString());
            }
            if (jsonObject.has("tempcategory")) {
                builder.tempcategory(jsonObject.get("tempcategory").getAsString());
            }
            if (jsonObject.has("difficulty")) {
                builder.difficulty(jsonObject.get("difficulty").getAsString());
            }
            JSonTools.getElement(jsonObject, "mob")
                    .ifPresent(e -> JSonTools.asArrayOrSingle(e)
                            .map(JsonElement::getAsString)
                            .forEach(builder::mob));
            JSonTools.getElement(jsonObject, "mod")
                    .ifPresent(e -> JSonTools.asArrayOrSingle(e)
                            .map(JsonElement::getAsString)
                            .forEach(builder::mod));
            JSonTools.getElement(jsonObject, "block")
                    .ifPresent(e -> JSonTools.asArrayOrSingle(e)
                            .map(JsonElement::getAsString)
                            .forEach(builder::block));
            JSonTools.getElement(jsonObject, "biome")
                    .ifPresent(e -> JSonTools.asArrayOrSingle(e)
                            .map(JsonElement::getAsString)
                            .forEach(builder::biome));
            JSonTools.getElement(jsonObject, "dimension")
                    .ifPresent(e -> JSonTools.asArrayOrSingle(e)
                            .map(JsonElement::getAsInt)
                            .forEach(builder::dimension));

            return builder.build();
        }
    }

    public static class Builder {
        private Integer mintime = null;
        private Integer maxtime = null;
        private Integer minlight = null;
        private Integer maxlight = null;
        private Integer minheight = null;
        private Integer maxheight = null;
        private Boolean passive = null;
        private Boolean hostile = null;
        private Boolean seesky = null;
        private String weather = null;
        private String difficulty = null;
        private String tempcategory = null;
        private Float minAdditionalDifficulty = null;
        private Float maxAdditionalDifficulty = null;
        private List<String> mobs = new ArrayList<>();
        private List<String> mods = new ArrayList<>();
        private List<String> blocks = new ArrayList<>();
        private List<String> biomes = new ArrayList<>();
        private List<Integer> dimensions = new ArrayList<>();

        private String result = "default";
        private Float healthmultiply = null;
        private Float healthadd = null;

        public Builder healthmultiply(Float healthmultiply) {
            this.healthmultiply = healthmultiply;
            return this;
        }

        public Builder healthadd(Float healthadd) {
            this.healthadd = healthadd;
            return this;
        }

        public Builder mintime(Integer mintime) {
            this.mintime = mintime;
            return this;
        }

        public Builder maxtime(Integer maxtime) {
            this.maxtime = maxtime;
            return this;
        }

        public Builder minheight(Integer minheight) {
            this.minheight = minheight;
            return this;
        }

        public Builder maxheight(Integer maxheight) {
            this.maxheight = maxheight;
            return this;
        }

        public Builder minlight(Integer minlight) {
            this.minlight = minlight;
            return this;
        }

        public Builder maxlight(Integer maxlight) {
            this.maxlight = maxlight;
            return this;
        }

        public Builder minAdditionalDifficulty(Float minAdditionalDifficulty) {
            this.minAdditionalDifficulty = minAdditionalDifficulty;
            return this;
        }

        public Builder maxAdditionalDifficulty(Float maxAdditionalDifficulty) {
            this.maxAdditionalDifficulty = maxAdditionalDifficulty;
            return this;
        }

        public Builder passive(Boolean passive) {
            this.passive = passive;
            return this;
        }

        public Builder hostile(Boolean hostile) {
            this.hostile = hostile;
            return this;
        }

        public Builder seesky(Boolean seesky) {
            this.seesky = seesky;
            return this;
        }

        public Builder result(String result) {
            this.result = result;
            return this;
        }

        public Builder mob(String mob) {
            this.mobs.add(mob);
            return this;
        }

        public Builder mod(String mod) {
            this.mods.add(mod);
            return this;
        }

        public Builder block(String block) {
            this.blocks.add(block);
            return this;
        }

        public Builder biome(String biome) {
            this.biomes.add(biome);
            return this;
        }

        public Builder weather(String weather) {
            this.weather = weather;
            return this;
        }

        public Builder tempcategory(String tempcategory) {
            this.tempcategory = tempcategory;
            return this;
        }

        public Builder difficulty(String difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder dimension(Integer dimension) {
            this.dimensions.add(dimension);
            return this;
        }

        public SpawnRule build() {
            return new SpawnRule(this);
        }
    }
}
