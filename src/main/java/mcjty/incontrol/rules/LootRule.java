package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import mcjty.incontrol.InControl;
import mcjty.incontrol.cache.StructureCache;
import mcjty.incontrol.typed.Attribute;
import mcjty.incontrol.typed.AttributeMap;
import mcjty.incontrol.typed.GenericAttributeMapFactory;
import mcjty.incontrol.varia.Tools;
import mcjty.lib.tools.EntityTools;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.function.Function;

import static mcjty.incontrol.rules.RuleKeys.*;

public class LootRule {

    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();

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
                .attribute(Attribute.create(PASSIVE))
                .attribute(Attribute.create(HOSTILE))
                .attribute(Attribute.create(SEESKY))
                .attribute(Attribute.create(WEATHER))
                .attribute(Attribute.create(TEMPCATEGORY))
                .attribute(Attribute.create(DIFFICULTY))
                .attribute(Attribute.create(STRUCTURE))
                .attribute(Attribute.create(PLAYER))
                .attribute(Attribute.create(PROJECTILE))
                .attribute(Attribute.create(EXPLOSION))
                .attribute(Attribute.create(FIRE))
                .attribute(Attribute.create(MAGIC))
                .attribute(Attribute.createMulti(MOB))
                .attribute(Attribute.createMulti(MOD))
                .attribute(Attribute.createMulti(BLOCK))
                .attribute(Attribute.createMulti(BIOME))
                .attribute(Attribute.createMulti(DIMENSION))
                .attribute(Attribute.createMulti(SOURCE))
                .attribute(Attribute.createMulti(HELDITEM))

                .attribute(Attribute.createMulti(ITEM))
                .attribute(Attribute.createMulti(REMOVE))
        ;
    }

    private final List<Function<LivingDropsEvent, Boolean>> checks = new ArrayList<>();
    private List<Item> toRemoveItems = new ArrayList<>();
    private List<Item> toAddItems = new ArrayList<>();

    private LootRule(AttributeMap map) {
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

        if (map.has(HOSTILE)) {
            addHostileCheck(map);
        }
        if (map.has(PASSIVE)) {
            addPassiveCheck(map);
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
        if (map.has(PLAYER)) {
            addPlayerCheck(map);
        }
        if (map.has(EXPLOSION)) {
            addExplosionCheck(map);
        }
        if (map.has(PROJECTILE)) {
            addProjectileCheck(map);
        }
        if (map.has(FIRE)) {
            addFireCheck(map);
        }
        if (map.has(MAGIC)) {
            addMagicCheck(map);
        }
        if (map.has(MOB)) {
            addMobsCheck(map);
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
        if (map.has(DIMENSION)) {
            addDimensionCheck(map);
        }
        if (map.has(STRUCTURE)) {
            addStructureCheck(map);
        }
        if (map.has(SOURCE)) {
            addSourceCheck(map);
        }

        if (map.has(RANDOM)) {
            addRandomCheck(map);
        }
        if (map.has(HELDITEM)) {
            addHeldItemCheck(map);
        }

        if (map.has(ITEM)) {
            addItem(map);
        }
        if (map.has(REMOVE)) {
            removeItem(map);
        }
    }

    public List<Item> getToRemoveItems() {
        return toRemoveItems;
    }

    public List<Item> getToAddItems() {
        return toAddItems;
    }

    private List<Item> getItems(List<String> itemNames) {
        List<Item> items = new ArrayList<>();
        for (String name : itemNames) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
            if (item == null) {
                InControl.logger.log(Level.ERROR, "Unknown item '" + name + "'!");
            } else {
                items.add(item);
            }
        }
        return items;
    }

    private void addItem(AttributeMap map) {
        toAddItems.addAll(getItems(map.getList(ITEM)));
    }

    private void removeItem(AttributeMap map) {
        toRemoveItems.addAll(getItems(map.getList(REMOVE)));
    }

    private void addHeldItemCheck(AttributeMap map) {
        List<Item> items = getItems(map.getList(HELDITEM));
        checks.add(event -> {
            if (event.getSource() == null) {
                return false;
            }
            Entity entity = event.getSource().getEntity();
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                ItemStack mainhand = player.getHeldItemMainhand();
                if (ItemStackTools.isValid(mainhand)) {
                    for (Item item : items) {
                        if (mainhand.getItem() == item) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }


    private static Random rnd = new Random();

    private void addRandomCheck(AttributeMap map) {
        final float r = map.get(RANDOM);
        checks.add(event -> rnd.nextFloat() < r);
    }

    private void addSeeSkyCheck(AttributeMap map) {
        if (map.get(SEESKY)) {
            checks.add(event -> {
                World world = event.getEntity().getEntityWorld();
                return world.canBlockSeeSky(event.getEntity().getPosition());
            });
        } else {
            checks.add(event -> {
                World world = event.getEntity().getEntityWorld();
                return !world.canBlockSeeSky(event.getEntity().getPosition());
            });
        }
    }

    private void addHostileCheck(AttributeMap map) {
        if (map.get(HOSTILE)) {
            checks.add(event -> event.getEntity() instanceof IMob);
        } else {
            checks.add(event -> !(event.getEntity() instanceof IMob));
        }
    }

    private void addPassiveCheck(AttributeMap map) {
        if (map.get(PASSIVE)) {
            checks.add(event -> (event.getEntity() instanceof IAnimals && !(event.getEntity() instanceof IMob)));
        } else {
            checks.add(event -> !(event.getEntity() instanceof IAnimals && !(event.getEntity() instanceof IMob)));
        }
    }

    private void addMobsCheck(AttributeMap map) {
        List<String> mobs = map.getList(MOB);
        if (mobs.size() == 1) {
            String name = mobs.get(0);
            String id = EntityTools.fixEntityId(name);
            Class<? extends Entity> clazz = EntityTools.findClassById(id);
            if (clazz != null) {
                checks.add(event -> clazz.equals(event.getEntity().getClass()));
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
                checks.add(event -> classes.contains(event.getEntity().getClass()));
            }
        }
    }

    private void addDimensionCheck(AttributeMap map) {
        List<Integer> dimensions = map.getList(DIMENSION);
        if (dimensions.size() == 1) {
            Integer dim = dimensions.get(0);
            checks.add(event -> {
                World world = event.getEntity().getEntityWorld();
                return world.provider.getDimension() == dim;
            });
        } else {
            Set<Integer> dims = new HashSet<>(dimensions);
            checks.add(event -> {
                World world = event.getEntity().getEntityWorld();
                return dims.contains(world.provider.getDimension());
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
                World world = event.getEntity().getEntityWorld();
                return world.getDifficulty() == finalDiff;
            });
        } else {
            InControl.logger.log(Level.ERROR, "Unknown difficulty '" + difficulty + "'! Use one of 'easy', 'normal', 'hard',  or 'peaceful'");
        }
    }

    private void addWeatherCheck(AttributeMap map) {
        String weather = map.get(WEATHER);
        boolean raining = weather.toLowerCase().startsWith("rain");
        boolean thunder = weather.toLowerCase().startsWith("thunder");
        if (raining) {
            checks.add(event -> {
                World world = event.getEntity().getEntityWorld();
                return world.isRaining();
            });
        } else if (thunder) {
            checks.add(event -> {
                World world = event.getEntity().getEntityWorld();
                return world.isThundering();
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
            World world = event.getEntity().getEntityWorld();
            Biome biome = world.getBiome(event.getEntity().getPosition());
            return biome.getTempCategory() == finalCat;
        });
    }

    private void addPlayerCheck(AttributeMap map) {
        boolean asPlayer = map.get(PLAYER);
        if (asPlayer) {
            checks.add(event -> event.getEntity() instanceof EntityPlayer);
        } else {
            checks.add(event -> !(event.getEntity() instanceof EntityPlayer));
        }
    }

    private void addExplosionCheck(AttributeMap map) {
        boolean explosion = map.get(EXPLOSION);
        if (explosion) {
            checks.add(event -> event.getSource() == null ? false : event.getSource().isExplosion());
        } else {
            checks.add(event -> event.getSource() == null ? false : !event.getSource().isExplosion());
        }
    }

    private void addProjectileCheck(AttributeMap map) {
        boolean projectile = map.get(PROJECTILE);
        if (projectile) {
            checks.add(event -> event.getSource() == null ? false : event.getSource().isProjectile());
        } else {
            checks.add(event -> event.getSource() == null ? false : !event.getSource().isProjectile());
        }
    }

    private void addFireCheck(AttributeMap map) {
        boolean fire = map.get(FIRE);
        if (fire) {
            checks.add(event -> event.getSource() == null ? false : event.getSource().isFireDamage());
        } else {
            checks.add(event -> event.getSource() == null ? false : !event.getSource().isFireDamage());
        }
    }

    private void addMagicCheck(AttributeMap map) {
        boolean magic = map.get(MAGIC);
        if (magic) {
            checks.add(event -> event.getSource() == null ? false : event.getSource().isMagicDamage());
        } else {
            checks.add(event -> event.getSource() == null ? false : !event.getSource().isMagicDamage());
        }
    }

    private void addSourceCheck(AttributeMap map) {
        List<String> sources = map.getList(SOURCE);
        Set<String> sourceSet = new HashSet<>(sources);
        checks.add(event -> {
            if (event.getSource() == null) {
                return false;
            }
            return sourceSet.contains(event.getSource().getDamageType());
        });
    }

    private void addStructureCheck(AttributeMap map) {
        String structure = map.get(STRUCTURE);
        checks.add(event -> {
            World world = event.getEntity().getEntityWorld();
            BlockPos pos = event.getEntity().getPosition();
            return StructureCache.CACHE.isInStructure(world, structure, pos);
        });
    }

    private void addBiomesCheck(AttributeMap map) {
        List<String> biomes = map.getList(BIOME);
        if (biomes.size() == 1) {
            String biomename = biomes.get(0);
            checks.add(event -> {
                World world = event.getEntity().getEntityWorld();
                BlockPos pos = event.getEntity().getPosition();
                Biome biome = world.getBiome(pos);
                return biomename.equals(biome.getBiomeName());
            });
        } else {
            Set<String> biomenames = new HashSet<>(biomes);
            checks.add(event -> {
                World world = event.getEntity().getEntityWorld();
                BlockPos pos = event.getEntity().getPosition();
                Biome biome = world.getBiome(pos);
                return biomenames.contains(biome.getBiomeName());
            });
        }
    }

    private void addBlocksCheck(AttributeMap map) {
        List<String> blocks = map.getList(BLOCK);
        if (blocks.size() == 1) {
            String blockname = blocks.get(0);
            checks.add(event -> {
                World world = event.getEntity().getEntityWorld();
                BlockPos pos = event.getEntity().getPosition();
                ResourceLocation registryName = world.getBlockState(pos.down()).getBlock().getRegistryName();
                if (registryName == null) {
                    return false;
                }
                String name = registryName.toString();
                return blockname.equals(name);
            });
        } else {
            Set<String> blocknames = new HashSet<>(blocks);
            checks.add(event -> {
                World world = event.getEntity().getEntityWorld();
                BlockPos pos = event.getEntity().getPosition();
                ResourceLocation registryName = world.getBlockState(pos.down()).getBlock().getRegistryName();
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
            checks.add(event -> {
                String id = Tools.findModID(event.getEntity());
                return modid.equals(id);
            });
        } else {
            Set<String> modids = new HashSet<>();
            for (String modid : mods) {
                modids.add(modid);
            }
            checks.add(event -> {
                String id = Tools.findModID(event.getEntity());
                return modids.contains(id);
            });
        }
    }

    private void addMinTimeCheck(AttributeMap map) {
        final int mintime = map.get(MINTIME);
        checks.add(event -> {
            World world = event.getEntity().getEntityWorld();
            int time = (int) world.getWorldTime();
            return time >= mintime;
        });
    }

    private void addMaxTimeCheck(AttributeMap map) {
        final int maxtime = map.get(MAXTIME);
        checks.add(event -> {
            World world = event.getEntity().getEntityWorld();
            int time = (int) world.getWorldTime();
            return time <= maxtime;
        });
    }

    private void addMinSpawnDistCheck(AttributeMap map) {
        final Float d = map.get(MINSPAWNDIST) * map.get(MINSPAWNDIST);
        checks.add(event -> {
            World world = event.getEntity().getEntityWorld();
            BlockPos pos = event.getEntity().getPosition();
            double sqdist = pos.distanceSq(world.getSpawnPoint());
            return sqdist >= d;
        });
    }

    private void addMaxSpawnDistCheck(AttributeMap map) {
        final Float d = map.get(MAXSPAWNDIST) * map.get(MAXSPAWNDIST);
        checks.add(event -> {
            World world = event.getEntity().getEntityWorld();
            BlockPos pos = event.getEntity().getPosition();
            double sqdist = pos.distanceSq(world.getSpawnPoint());
            return sqdist <= d;
        });
    }


    private void addMinLightCheck(AttributeMap map) {
        final int minlight = map.get(MINLIGHT);
        checks.add(event -> {
            World world = event.getEntity().getEntityWorld();
            BlockPos pos = event.getEntity().getPosition();
            double sqdist = pos.distanceSq(world.getSpawnPoint());
            return world.getLight(pos, true) >= minlight;
        });
    }

    private void addMaxLightCheck(AttributeMap map) {
        final int maxlight = map.get(MAXLIGHT);
        checks.add(event -> {
            World world = event.getEntity().getEntityWorld();
            BlockPos pos = event.getEntity().getPosition();
            double sqdist = pos.distanceSq(world.getSpawnPoint());
            return world.getLight(pos, true) <= maxlight;
        });
    }

    private void addMinAdditionalDifficultyCheck(AttributeMap map) {
        final Float mindifficulty = map.get(MINDIFFICULTY);
        checks.add(event -> {
            World world = event.getEntity().getEntityWorld();
            BlockPos pos = event.getEntity().getPosition();
            return world.getDifficultyForLocation(pos).getAdditionalDifficulty() >= mindifficulty;
        });
    }

    private void addMaxAdditionalDifficultyCheck(AttributeMap map) {
        final Float maxdifficulty = map.get(MAXDIFFICULTY);
        checks.add(event -> {
            World world = event.getEntity().getEntityWorld();
            BlockPos pos = event.getEntity().getPosition();
            return world.getDifficultyForLocation(pos).getAdditionalDifficulty() <= maxdifficulty;
        });
    }

    private void addMaxHeightCheck(AttributeMap map) {
        final int maxheight = map.get(MAXHEIGHT);
        checks.add(event -> event.getEntity().getPosition().getY() <= maxheight);
    }

    private void addMinHeightCheck(AttributeMap map) {
        final int minheight = map.get(MINHEIGHT);
        checks.add(event -> event.getEntity().getPosition().getY() >= minheight);
    }

    public boolean match(LivingDropsEvent event) {
        for (Function<LivingDropsEvent, Boolean> rule : checks) {
            if (!rule.apply(event)) {
                return false;
            }
        }
        return true;
    }

    public static LootRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = FACTORY.parse(element);
            return new LootRule(map);
        }
    }}
