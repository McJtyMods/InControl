package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.varia.JSonTools;
import mcjty.incontrol.varia.Tools;
import mcjty.lib.tools.EntityTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;


public class SpawnRule {
    private final Event.Result result;
    private final List<Function<LivingSpawnEvent.CheckSpawn, Boolean>> checks = new ArrayList<>();

    private SpawnRule(Builder builder) {
        if (builder.minheight != -1) {
            addMinHeightCheck(builder);
        }
        if (builder.maxheight != -1) {
            addMaxHeightCheck(builder);
        }
        if (builder.hostile != null) {
            addHostileCheck(builder);
        }
        if (builder.passive != null) {
            addPassiveCheck(builder);
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
            }
        } else {
            Set<Class> classes = new HashSet<>();
            for (String name : builder.mobs) {
                String id = EntityTools.fixEntityId(name);
                Class<? extends Entity> clazz = EntityTools.findClassById(id);
                if (clazz != null) {
                    classes.add(clazz);
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
                String name = event.getWorld().getBlockState(new BlockPos(event.getX(), event.getY(), event.getZ())).getBlock().getRegistryName().toString();
                return blockname.equals(name);
            });
        } else {
            Set<String> blocknames = new HashSet<>(builder.blocks);
            checks.add(event -> {
                String name = event.getWorld().getBlockState(new BlockPos(event.getX(), event.getY(), event.getZ())).getBlock().getRegistryName().toString();
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

    public Event.Result getResult() {
        return result;
    }

    public static SpawnRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            Builder builder = new Builder();
            JsonObject jsonObject = element.getAsJsonObject();
            builder.minheight(JSonTools.parseInt(jsonObject, "minheight"));
            builder.maxheight(JSonTools.parseInt(jsonObject, "maxheight"));
            builder.passive(JSonTools.parseBool(jsonObject, "passive"));
            builder.hostile(JSonTools.parseBool(jsonObject, "hostile"));
            if (jsonObject.has("result")) {
                builder.result(jsonObject.get("result").getAsString());
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
        private int minheight = -1;
        private int maxheight = -1;
        private Boolean passive = null;
        private Boolean hostile = null;
        private List<String> mobs = new ArrayList<>();
        private List<String> mods = new ArrayList<>();
        private List<String> blocks = new ArrayList<>();
        private List<String> biomes = new ArrayList<>();
        private List<Integer> dimensions = new ArrayList<>();

        private String result = "default";

        public Builder minheight(Integer minheight) {
            this.minheight = minheight == null ? -1 : minheight;
            return this;
        }

        public Builder maxheight(Integer maxheight) {
            this.maxheight = maxheight == null ? -1 : maxheight;
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

        public Builder dimension(Integer dimension) {
            this.dimensions.add(dimension);
            return this;
        }

        public SpawnRule build() {
            return new SpawnRule(this);
        }
    }
}
