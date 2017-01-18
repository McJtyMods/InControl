package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import mcjty.incontrol.InControl;
import mcjty.incontrol.cache.StructureCache;
import mcjty.incontrol.typed.Attribute;
import mcjty.incontrol.typed.AttributeMap;
import mcjty.incontrol.typed.GenericAttributeMapFactory;
import mcjty.incontrol.typed.Key;
import mcjty.incontrol.varia.Tools;
import mcjty.lib.tools.EntityTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static mcjty.incontrol.rules.RuleKeys.*;


public class SpawnRule {

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
                .attribute(Attribute.createMulti(MOB))
                .attribute(Attribute.createMulti(MOD))
                .attribute(Attribute.createMulti(BLOCK))
                .attribute(Attribute.createMulti(BIOME))
                .attribute(Attribute.createMulti(DIMENSION))

                .attribute(Attribute.create(RESULT))
                .attribute(Attribute.create(HEALTHMULTIPLY))
                .attribute(Attribute.create(HEALTHADD))
                .attribute(Attribute.create(SPEEDMULTIPLY))
                .attribute(Attribute.create(SPEEDADD))
                .attribute(Attribute.create(DAMAGEMULTIPLY))
                .attribute(Attribute.create(DAMAGEADD))
                .attribute(Attribute.create(SIZEMULTIPLY))
                .attribute(Attribute.create(SIZEADD))
                .attribute(Attribute.create(ANGRY))
                .attribute(Attribute.createMulti(HELDITEM))
                .attribute(Attribute.createMulti(ARMORBOOTS))
                .attribute(Attribute.createMulti(ARMORLEGS))
                .attribute(Attribute.createMulti(ARMORCHEST))
                .attribute(Attribute.createMulti(ARMORHELMET))
                .attribute(Attribute.createMulti(POTION))
        ;
    }

    private final Event.Result result;
    private final List<Function<LivingSpawnEvent.CheckSpawn, Boolean>> checks = new ArrayList<>();
    private final List<Consumer<LivingSpawnEvent.CheckSpawn>> actions = new ArrayList<>();

    private SpawnRule(AttributeMap map) {
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
        if (map.has(RANDOM)) {
            addRandomCheck(map);
        }

        if (map.has(RESULT)) {
            String br = map.get(RESULT);
            if ("default".equals(br) || br.startsWith("def")) {
                this.result = Event.Result.DEFAULT;
            } else if ("allow".equals(br) || "true".equals(br)) {
                this.result = Event.Result.ALLOW;
            } else {
                this.result = Event.Result.DENY;
            }
        } else {
            this.result = Event.Result.DEFAULT;
        }

        if (map.has(HEALTHMULTIPLY) || map.has(HEALTHADD)) {
            addHealthAction(map);
        }
        if (map.has(SPEEDMULTIPLY) || map.has(SPEEDADD)) {
            addSpeedAction(map);
        }
        if (map.has(DAMAGEMULTIPLY) || map.has(DAMAGEADD)) {
            addDamageAction(map);
        }
        if (map.has(SIZEMULTIPLY) || map.has(SIZEADD)) {
            addSizeActions(map);
        }
        if (map.has(ANGRY)) {
            addAngryAction(map);
        }
        if (map.has(HELDITEM)) {
            addHeldItem(map);
        }
        if (map.has(ARMORBOOTS)) {
            addArmorItem(map, ARMORBOOTS, EntityEquipmentSlot.FEET);
        }
        if (map.has(ARMORLEGS)) {
            addArmorItem(map, ARMORLEGS, EntityEquipmentSlot.LEGS);
        }
        if (map.has(ARMORHELMET)) {
            addArmorItem(map, ARMORHELMET, EntityEquipmentSlot.HEAD);
        }
        if (map.has(ARMORCHEST)) {
            addArmorItem(map, ARMORCHEST, EntityEquipmentSlot.CHEST);
        }
        if (map.has(POTION)) {
            addPotionsAction(map);
        }
    }

    private void addPotionsAction(AttributeMap map) {
        List<PotionEffect> effects = new ArrayList<>();
        for (String p : map.getList(POTION)) {
            String[] splitted = StringUtils.split(p, ',');
            if (splitted == null || splitted.length != 3) {
                InControl.logger.log(Level.ERROR, "Bad potion specifier '" + p + "'! Use <potion>,<duration>,<amplifier>");
                continue;
            }
            Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(splitted[0]));
            if (potion == null) {
                InControl.logger.log(Level.ERROR, "Can't find potion '" + p + "'!");
                continue;
            }
            int duration = 0;
            int amplifier = 0;
            try {
                duration = Integer.parseInt(splitted[1]);
                amplifier = Integer.parseInt(splitted[2]);
            } catch (NumberFormatException e) {
                InControl.logger.log(Level.ERROR, "Bad duration or amplifier integer for '" + p + "'!");
                continue;
            }
            effects.add(new PotionEffect(potion, duration, amplifier));
        }
        if (!effects.isEmpty()) {
            actions.add(event -> {
                EntityLivingBase living = event.getEntityLiving();
                if (living != null) {
                    for (PotionEffect effect : effects) {
                        PotionEffect neweffect = new PotionEffect(effect.getPotion(), effect.getDuration(), effect.getAmplifier());
                        living.addPotionEffect(neweffect);
                    }
                }
            });
        }
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

    private void addArmorItem(AttributeMap map, Key<String> itemKey, EntityEquipmentSlot slot) {
        final List<Item> items = getItems(map.getList(itemKey));
        if (items.isEmpty()) {
            return;
        }
        if (items.size() == 1) {
            Item item = items.get(0);
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getEntityLiving();
                if (entityLiving != null) {
                    entityLiving.setItemStackToSlot(slot, new ItemStack(item));
                }
            });
        } else {
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getEntityLiving();
                if (entityLiving != null) {
                    entityLiving.setItemStackToSlot(slot, new ItemStack(items.get(rnd.nextInt(items.size()))));
                }
            });
        }
    }

    private void addHeldItem(AttributeMap map) {
        final List<Item> items = getItems(map.getList(HELDITEM));
        if (items.isEmpty()) {
            return;
        }
        if (items.size() == 1) {
            Item item = items.get(0);
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getEntityLiving();
                if (entityLiving != null) {
                    entityLiving.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(item));
                }
            });
        } else {
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getEntityLiving();
                if (entityLiving != null) {
                    entityLiving.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(items.get(rnd.nextInt(items.size()))));
                }
            });
        }
    }

    private void addAngryAction(AttributeMap map) {
        if (map.get(ANGRY)) {
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getEntityLiving();
                if (entityLiving instanceof EntityPigZombie) {
                    EntityPigZombie pigZombie = (EntityPigZombie) entityLiving;
                    EntityPlayer player = event.getWorld().getClosestPlayerToEntity(entityLiving, 50);
                    if (player != null) {
                        pigZombie.becomeAngryAt(player);
                    }
                } else if (entityLiving instanceof EntityLiving) {
                    EntityPlayer player = event.getWorld().getClosestPlayerToEntity(entityLiving, 50);
                    if (player != null) {
                        ((EntityLiving) entityLiving).setAttackTarget(player);
                    }
                }
            });
        }
    }

    private void addHealthAction(AttributeMap map) {
        float m = map.has(HEALTHMULTIPLY) ? map.get(HEALTHMULTIPLY) : 1;
        float a = map.has(HEALTHADD) ? map.get(HEALTHADD) : 0;
        actions.add(event -> {
            EntityLivingBase entityLiving = event.getEntityLiving();
            if (entityLiving != null) {
                IAttributeInstance entityAttribute = entityLiving.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
                if (entityAttribute != null) {
                    double newMax = entityAttribute.getBaseValue() * m + a;
                    entityAttribute.setBaseValue(newMax);
                    entityLiving.setHealth((float) newMax);
                }
            }
        });
    }

    private void addSpeedAction(AttributeMap map) {
        float m = map.has(SPEEDMULTIPLY) ? map.get(SPEEDMULTIPLY) : 1;
        float a = map.has(SPEEDADD) ? map.get(SPEEDADD) : 0;
        actions.add(event -> {
            EntityLivingBase entityLiving = event.getEntityLiving();
            if (entityLiving != null) {
                IAttributeInstance entityAttribute = entityLiving.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                if (entityAttribute != null) {
                    double newMax = entityAttribute.getBaseValue() * m + a;
                    entityAttribute.setBaseValue(newMax);
                }
            }
        });
    }

    private void addSizeActions(AttributeMap map) {
        InControl.logger.log(Level.WARN, "Mob resizing not implemented yet!");
        float m = map.has(SIZEMULTIPLY) ? map.get(SIZEMULTIPLY) : 1;
        float a = map.has(SIZEADD) ? map.get(SIZEADD) : 0;
        actions.add(event -> {
            EntityLivingBase entityLiving = event.getEntityLiving();
            if (entityLiving != null) {
                // Not implemented yet
//                entityLiving.setSize(entityLiving.width * m + a, entityLiving.height * m + a);
            }
        });
    }

    private void addDamageAction(AttributeMap map) {
        float m = map.has(DAMAGEMULTIPLY) ? map.get(DAMAGEMULTIPLY) : 1;
        float a = map.has(DAMAGEADD) ? map.get(DAMAGEADD) : 0;
        actions.add(event -> {
            EntityLivingBase entityLiving = event.getEntityLiving();
            if (entityLiving != null) {
                IAttributeInstance entityAttribute = entityLiving.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
                if (entityAttribute != null) {
                    double newMax = entityAttribute.getBaseValue() * m + a;
                    entityAttribute.setBaseValue(newMax);
                }
            }
        });
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

    private void addHostileCheck(AttributeMap map) {
        if (map.get(HOSTILE)) {
            checks.add(event -> {
                return event.getEntity() instanceof IMob;
            });
        } else {
            checks.add(event -> {
                return !(event.getEntity() instanceof IMob);
            });
        }
    }

    private void addPassiveCheck(AttributeMap map) {
        if (map.get(PASSIVE)) {
            checks.add(event -> {
                return (event.getEntity() instanceof IAnimals && !(event.getEntity() instanceof IMob));
            });
        } else {
            checks.add(event -> {
                return !(event.getEntity() instanceof IAnimals && !(event.getEntity() instanceof IMob));
            });
        }
    }

    private void addMobsCheck(AttributeMap map) {
        List<String> mobs = map.getList(MOB);
        if (mobs.size() == 1) {
            String name = mobs.get(0);
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
                checks.add(event -> {
                    return classes.contains(event.getEntity().getClass());
                });
            }
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
        String weather = map.get(WEATHER);
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
            Biome biome = event.getWorld().getBiome(new BlockPos(event.getX(), event.getY(), event.getZ()));
            return biome.getTempCategory() == finalCat;
        });
    }

    private void addStructureCheck(AttributeMap map) {
        String structure = map.get(STRUCTURE);
        checks.add(event -> {
            BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
            return StructureCache.CACHE.isInStructure(event.getWorld(), structure, pos);
        });
    }

    private void addBiomesCheck(AttributeMap map) {
        List<String> biomes = map.getList(BIOME);
        if (biomes.size() == 1) {
            String biomename = biomes.get(0);
            checks.add(event -> {
                Biome biome = event.getWorld().getBiome(new BlockPos(event.getX(), event.getY(), event.getZ()));
                return biomename.equals(biome.getBiomeName());
            });
        } else {
            Set<String> biomenames = new HashSet<>(biomes);
            checks.add(event -> {
                Biome biome = event.getWorld().getBiome(new BlockPos(event.getX(), event.getY(), event.getZ()));
                return biomenames.contains(biome.getBiomeName());
            });
        }
    }

    private void addBlocksCheck(AttributeMap map) {
        List<String> blocks = map.getList(BLOCK);
        if (blocks.size() == 1) {
            String blockname = blocks.get(0);
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
            Set<String> blocknames = new HashSet<>(blocks);
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
        final Float d = map.get(MINSPAWNDIST) * map.get(MINSPAWNDIST);
        checks.add(event -> {
            BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
            double sqdist = pos.distanceSq(event.getWorld().getSpawnPoint());
            return sqdist >= d;
        });
    }

    private void addMaxSpawnDistCheck(AttributeMap map) {
        final Float d = map.get(MAXSPAWNDIST) * map.get(MAXSPAWNDIST);
        checks.add(event -> {
            BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
            double sqdist = pos.distanceSq(event.getWorld().getSpawnPoint());
            return sqdist <= d;
        });
    }


    private void addMinLightCheck(AttributeMap map) {
        final int minlight = map.get(MINLIGHT);
        checks.add(event -> {
            BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
            return event.getWorld().getLight(pos, true) >= minlight;
        });
    }

    private void addMaxLightCheck(AttributeMap map) {
        final int maxlight = map.get(MAXLIGHT);
        checks.add(event -> {
            BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
            return event.getWorld().getLight(pos, true) <= maxlight;
        });
    }

    private void addMinAdditionalDifficultyCheck(AttributeMap map) {
        final Float mindifficulty = map.get(MINDIFFICULTY);
        checks.add(event -> {
            return event.getWorld().getDifficultyForLocation(new BlockPos(event.getX(), event.getY(), event.getZ())).getAdditionalDifficulty() >= mindifficulty;
        });
    }

    private void addMaxAdditionalDifficultyCheck(AttributeMap map) {
        final Float maxdifficulty = map.get(MAXDIFFICULTY);
        checks.add(event -> {
            return event.getWorld().getDifficultyForLocation(new BlockPos(event.getX(), event.getY(), event.getZ())).getAdditionalDifficulty() <= maxdifficulty;
        });
    }

    private void addMaxHeightCheck(AttributeMap map) {
        final int maxheight = map.get(MAXHEIGHT);
        checks.add(event -> {
            return event.getY() <= maxheight;
        });
    }

    private void addMinHeightCheck(AttributeMap map) {
        final int minheight = map.get(MINHEIGHT);
        checks.add(event -> {
            return event.getY() >= minheight;
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
            AttributeMap map = FACTORY.parse(element);
            return new SpawnRule(map);
        }
    }
}
