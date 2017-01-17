package mcjty.incontrol.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.InControl;
import mcjty.incontrol.varia.JSonTools;
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

public class PotentialSpawnRule {

    private final List<Function<WorldEvent.PotentialSpawns, Boolean>> checks = new ArrayList<>();
    private List<Biome.SpawnListEntry> spawnEntries = new ArrayList<>();

    private PotentialSpawnRule(Builder builder) {
        if (builder.spawnEntryBuilders.isEmpty()) {
            InControl.logger.log(Level.ERROR, "No mobs specified!");
            return;
        }
        for (SpawnEntryBuilder entryBuilder : builder.spawnEntryBuilders) {
            String id = EntityTools.fixEntityId(entryBuilder.mob);
            Class<? extends Entity> clazz = EntityTools.findClassById(id);
            if (clazz == null) {
                InControl.logger.log(Level.ERROR, "Cannot find mob '" + entryBuilder.mob + "'!");
                return;
            }

            Biome.SpawnListEntry entry = new Biome.SpawnListEntry((Class<? extends EntityLiving>) clazz, entryBuilder.weight, entryBuilder.groupCountMin, entryBuilder.groupCountMax);
            spawnEntries.add(entry);
        }

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

        if (builder.minspawndist != null) {
            addMinSpawnDistCheck(builder);
        }
        if (builder.maxspawndist != null) {
            addMaxSpawnDistCheck(builder);
        }

        if (builder.minAdditionalDifficulty != null) {
            addMinAdditionalDifficultyCheck(builder);
        }
        if (builder.maxAdditionalDifficulty != null) {
            addMaxAdditionalDifficultyCheck(builder);
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
        if (!builder.blocks.isEmpty()) {
            addBlocksCheck(builder);
        }
        if (!builder.biomes.isEmpty()) {
            addBiomesCheck(builder);
        }
        if (!builder.dimensions.isEmpty()) {
            addDimensionCheck(builder);
        }
        if (builder.random != null) {
            addRandomCheck(builder);
        }

    }

    private static Random rnd = new Random();

    private void addRandomCheck(Builder builder) {
        float r = builder.random;
        checks.add(event -> {
            return rnd.nextFloat() < r;
        });
    }

    private void addSeeSkyCheck(Builder builder) {
        if (builder.seesky) {
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
            Biome biome = event.getWorld().getBiome(event.getPos());
            return biome.getTempCategory() == finalCat;
        });
    }

    private void addBiomesCheck(Builder builder) {
        if (builder.biomes.size() == 1) {
            String biomename = builder.biomes.get(0);
            checks.add(event -> {
                Biome biome = event.getWorld().getBiome(event.getPos());
                return biomename.equals(biome.getBiomeName());
            });
        } else {
            Set<String> biomenames = new HashSet<>(builder.biomes);
            checks.add(event -> {
                Biome biome = event.getWorld().getBiome(event.getPos());
                return biomenames.contains(biome.getBiomeName());
            });
        }
    }

    private void addBlocksCheck(Builder builder) {
        if (builder.blocks.size() == 1) {
            String blockname = builder.blocks.get(0);
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
            Set<String> blocknames = new HashSet<>(builder.blocks);
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

    private void addMinSpawnDistCheck(Builder builder) {
        Float d = builder.minspawndist * builder.minspawndist;
        checks.add(event -> {
            BlockPos pos = event.getPos();
            double sqdist = pos.distanceSqToCenter(0, 0, 0);
            return sqdist >= d;
        });
    }

    private void addMaxSpawnDistCheck(Builder builder) {
        Float d = builder.maxspawndist * builder.maxspawndist;
        checks.add(event -> {
            BlockPos pos = event.getPos();
            double sqdist = pos.distanceSqToCenter(0, 0, 0);
            return sqdist <= d;
        });
    }


    private void addMinLightCheck(Builder builder) {
        checks.add(event -> {
            BlockPos pos = event.getPos();
            return event.getWorld().getLight(pos, true) >= builder.minlight;
        });
    }

    private void addMaxLightCheck(Builder builder) {
        checks.add(event -> {
            BlockPos pos = event.getPos();
            return event.getWorld().getLight(pos, true) <= builder.maxlight;
        });
    }

    private void addMinAdditionalDifficultyCheck(Builder builder) {
        checks.add(event -> {
            return event.getWorld().getDifficultyForLocation(event.getPos()).getAdditionalDifficulty() >= builder.minAdditionalDifficulty;
        });
    }

    private void addMaxAdditionalDifficultyCheck(Builder builder) {
        checks.add(event -> {
            return event.getWorld().getDifficultyForLocation(event.getPos()).getAdditionalDifficulty() <= builder.maxAdditionalDifficulty;
        });
    }

    private void addMaxHeightCheck(Builder builder) {
        checks.add(event -> {
            return event.getPos().getY() <= builder.maxheight;
        });
    }

    private void addMinHeightCheck(Builder builder) {
        checks.add(event -> {
            return event.getPos().getY() >= builder.minheight;
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
            Builder builder = new Builder();
            JsonObject jsonObject = element.getAsJsonObject();
            if (!jsonObject.has("mobs")) {
                return null;
            }
            JsonArray mobs = jsonObject.get("mobs").getAsJsonArray();
            for (JsonElement mob : mobs) {
                JsonObject mobObject = mob.getAsJsonObject();
                SpawnEntryBuilder mobBuilder = new SpawnEntryBuilder();
                mobBuilder.mob(mobObject.get("mob").getAsString());
                mobBuilder.groupCountMin(JSonTools.parseInt(mobObject, "groupcountmin"));
                mobBuilder.groupCountMax(JSonTools.parseInt(mobObject, "groupcountmax"));
                mobBuilder.weight(JSonTools.parseInt(mobObject, "weight"));
                builder.spawnEntryBuilder(mobBuilder);
            }

            // Inputs
            builder.mintime(JSonTools.parseInt(jsonObject, "mintime"));
            builder.maxtime(JSonTools.parseInt(jsonObject, "maxtime"));
            builder.minheight(JSonTools.parseInt(jsonObject, "minheight"));
            builder.maxheight(JSonTools.parseInt(jsonObject, "maxheight"));
            builder.minlight(JSonTools.parseInt(jsonObject, "minlight"));
            builder.maxlight(JSonTools.parseInt(jsonObject, "maxlight"));
            builder.minspawndist(JSonTools.parseFloat(jsonObject, "minspawndist"));
            builder.maxspawndist(JSonTools.parseFloat(jsonObject, "maxspawndist"));
            builder.random(JSonTools.parseFloat(jsonObject, "random"));
            builder.minAdditionalDifficulty(JSonTools.parseFloat(jsonObject, "mindifficulty"));
            builder.maxAdditionalDifficulty(JSonTools.parseFloat(jsonObject, "maxdifficulty"));
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

    public static class Builder {
        private Integer mintime = null;
        private Integer maxtime = null;
        private Integer minlight = null;
        private Integer maxlight = null;
        private Integer minheight = null;
        private Integer maxheight = null;
        private Float minspawndist = null;
        private Float maxspawndist = null;
        private Float random = null;
        private Boolean seesky = null;
        private String weather = null;
        private String difficulty = null;
        private String tempcategory = null;
        private Float minAdditionalDifficulty = null;
        private Float maxAdditionalDifficulty = null;
        private List<String> blocks = new ArrayList<>();
        private List<String> biomes = new ArrayList<>();
        private List<Integer> dimensions = new ArrayList<>();
        private List<SpawnEntryBuilder> spawnEntryBuilders = new ArrayList<>();

        public Builder spawnEntryBuilder(SpawnEntryBuilder spawnEntryBuilder) {
            spawnEntryBuilders.add(spawnEntryBuilder);
            return this;
        }

        public Builder random(Float random) {
            this.random = random;
            return this;
        }

        public Builder minspawndist(Float minspawndist) {
            this.minspawndist = minspawndist;
            return this;
        }

        public Builder maxspawndist(Float maxspawndist) {
            this.maxspawndist = maxspawndist;
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

        public Builder seesky(Boolean seesky) {
            this.seesky = seesky;
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

        public PotentialSpawnRule build() {
            return new PotentialSpawnRule(this);
        }
    }
}

