package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import mcjty.incontrol.InControl;
import mcjty.incontrol.rules.support.GenericRuleEvaluator;
import mcjty.incontrol.rules.support.IEventQuery;
import mcjty.incontrol.typed.Attribute;
import mcjty.incontrol.typed.AttributeMap;
import mcjty.incontrol.typed.GenericAttributeMapFactory;
import mcjty.incontrol.typed.Key;
import mcjty.incontrol.varia.Tools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static mcjty.incontrol.rules.support.RuleKeys.*;


public class SpawnRule {

    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    public static final IEventQuery<LivingSpawnEvent.CheckSpawn> EVENT_QUERY = new IEventQuery<LivingSpawnEvent.CheckSpawn>() {
        @Override
        public World getWorld(LivingSpawnEvent.CheckSpawn o) {
            return o.getWorld();
        }

        @Override
        public BlockPos getPos(LivingSpawnEvent.CheckSpawn o) {
            LivingSpawnEvent.CheckSpawn s = o;
            return new BlockPos(s.getX(), s.getY(), s.getZ());
        }

        @Override
        public int getY(LivingSpawnEvent.CheckSpawn o) {
            return (int) o.getY();
        }

        @Override
        public Entity getEntity(LivingSpawnEvent.CheckSpawn o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(LivingSpawnEvent.CheckSpawn o) {
            return null;
        }
    };

    public static final IEventQuery<EntityJoinWorldEvent> EVENT_QUERY_JOIN = new IEventQuery<EntityJoinWorldEvent>() {
        @Override
        public World getWorld(EntityJoinWorldEvent o) {
            return o.getWorld();
        }

        @Override
        public BlockPos getPos(EntityJoinWorldEvent o) {
            EntityJoinWorldEvent s = o;
            return s.getEntity().getPosition();
        }

        @Override
        public int getY(EntityJoinWorldEvent o) {
            return (int) o.getEntity().getPosition().getY();
        }

        @Override
        public Entity getEntity(EntityJoinWorldEvent o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(EntityJoinWorldEvent o) {
            return null;
        }
    };

    static {
        FACTORY
                .attribute(Attribute.create(MINTIME))
                .attribute(Attribute.create(MAXTIME))
                .attribute(Attribute.create(MINCOUNT))
                .attribute(Attribute.create(MAXCOUNT))
                .attribute(Attribute.create(MINLIGHT))
                .attribute(Attribute.create(MAXLIGHT))
                .attribute(Attribute.create(MINHEIGHT))
                .attribute(Attribute.create(MAXHEIGHT))
                .attribute(Attribute.create(MINDIFFICULTY))
                .attribute(Attribute.create(MAXDIFFICULTY))
                .attribute(Attribute.create(MINSPAWNDIST))
                .attribute(Attribute.create(MAXSPAWNDIST))
                .attribute(Attribute.create(RANDOM))
                .attribute(Attribute.create(CANSPAWNHERE))
                .attribute(Attribute.create(NOTCOLLIDING))
                .attribute(Attribute.create(INBUILDING))
                .attribute(Attribute.create(INCITY))
                .attribute(Attribute.create(INSTREET))
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

                .attribute(Attribute.create(ACTION_RESULT))
                .attribute(Attribute.create(ACTION_HEALTHMULTIPLY))
                .attribute(Attribute.create(ACTION_HEALTHADD))
                .attribute(Attribute.create(ACTION_SPEEDMULTIPLY))
                .attribute(Attribute.create(ACTION_SPEEDADD))
                .attribute(Attribute.create(ACTION_DAMAGEMULTIPLY))
                .attribute(Attribute.create(ACTION_DAMAGEADD))
                .attribute(Attribute.create(ACTION_SIZEMULTIPLY))
                .attribute(Attribute.create(ACTION_SIZEADD))
                .attribute(Attribute.create(ACTION_ANGRY))
                .attribute(Attribute.create(ACTION_MOBNBT))
                .attribute(Attribute.createMulti(ACTION_HELDITEM))
                .attribute(Attribute.createMulti(ACTION_ARMORBOOTS))
                .attribute(Attribute.createMulti(ACTION_ARMORLEGS))
                .attribute(Attribute.createMulti(ACTION_ARMORCHEST))
                .attribute(Attribute.createMulti(ACTION_ARMORHELMET))
                .attribute(Attribute.createMulti(ACTION_POTION))
        ;
    }

    private Event.Result result;
    private final boolean onJoin;
    private final GenericRuleEvaluator ruleEvaluator;
    private final List<Consumer<EventGetter>> actions = new ArrayList<>();

    private static interface EventGetter {
        EntityLivingBase getEntityLiving();
        World getWorld();
    }

    private SpawnRule(AttributeMap map, boolean onJoin) {
        this.onJoin = onJoin;
        ruleEvaluator = new GenericRuleEvaluator(map);
        addActions(map);
    }

    private void addActions(AttributeMap map) {
        if (map.has(ACTION_RESULT)) {
            String br = map.get(ACTION_RESULT);
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

        if (map.has(ACTION_HEALTHMULTIPLY) || map.has(ACTION_HEALTHADD)) {
            addHealthAction(map);
        }
        if (map.has(ACTION_SPEEDMULTIPLY) || map.has(ACTION_SPEEDADD)) {
            addSpeedAction(map);
        }
        if (map.has(ACTION_DAMAGEMULTIPLY) || map.has(ACTION_DAMAGEADD)) {
            addDamageAction(map);
        }
        if (map.has(ACTION_SIZEMULTIPLY) || map.has(ACTION_SIZEADD)) {
            addSizeActions(map);
        }
        if (map.has(ACTION_ANGRY)) {
            addAngryAction(map);
        }
        if (map.has(ACTION_MOBNBT)) {
            addMobNBT(map);
        }
        if (map.has(ACTION_HELDITEM)) {
            addHeldItem(map);
        }
        if (map.has(ACTION_ARMORBOOTS)) {
            addArmorItem(map, ACTION_ARMORBOOTS, EntityEquipmentSlot.FEET);
        }
        if (map.has(ACTION_ARMORLEGS)) {
            addArmorItem(map, ACTION_ARMORLEGS, EntityEquipmentSlot.LEGS);
        }
        if (map.has(ACTION_ARMORHELMET)) {
            addArmorItem(map, ACTION_ARMORHELMET, EntityEquipmentSlot.HEAD);
        }
        if (map.has(ACTION_ARMORCHEST)) {
            addArmorItem(map, ACTION_ARMORCHEST, EntityEquipmentSlot.CHEST);
        }
        if (map.has(ACTION_POTION)) {
            addPotionsAction(map);
        }
    }

    private void addPotionsAction(AttributeMap map) {
        List<PotionEffect> effects = new ArrayList<>();
        for (String p : map.getList(ACTION_POTION)) {
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

    private List<Pair<Float, ItemStack>> getItems(List<String> itemNames) {
        List<Pair<Float, ItemStack>> items = new ArrayList<>();
        for (String name : itemNames) {
            Pair<Float, ItemStack> pair = Tools.parseStackWithFactor(name);
            if (pair.getValue().isEmpty()) {
                InControl.logger.log(Level.ERROR, "Unknown item '" + name + "'!");
            } else {
                items.add(pair);
            }
        }
        return items;
    }

    private ItemStack getRandomItem(List<Pair<Float, ItemStack>> items, float total) {
        float r = rnd.nextFloat() * total;
        for (Pair<Float, ItemStack> pair : items) {
            if (r <= pair.getLeft()) {
                return pair.getRight();
            }
            r -= pair.getLeft();
        }
        return ItemStack.EMPTY;
    }

    private void addArmorItem(AttributeMap map, Key<String> itemKey, EntityEquipmentSlot slot) {
        final List<Pair<Float, ItemStack>> items = getItems(map.getList(itemKey));
        if (items.isEmpty()) {
            return;
        }
        if (items.size() == 1) {
            ItemStack item = items.get(0).getRight();
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getEntityLiving();
                if (entityLiving != null) {
                    entityLiving.setItemStackToSlot(slot, item);
                }
            });
        } else {
            final float total = getTotal(items);
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getEntityLiving();
                if (entityLiving != null) {
                    entityLiving.setItemStackToSlot(slot, getRandomItem(items, total));
                }
            });
        }
    }

    private float getTotal(List<Pair<Float, ItemStack>> items) {
        float total = 0.0f;
        for (Pair<Float, ItemStack> pair : items) {
            total += pair.getLeft();
        }
        return total;
    }

    private void addHeldItem(AttributeMap map) {
        final List<Pair<Float, ItemStack>> items = getItems(map.getList(ACTION_HELDITEM));
        if (items.isEmpty()) {
            return;
        }
        if (items.size() == 1) {
            ItemStack item = items.get(0).getRight();
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getEntityLiving();
                if (entityLiving != null) {
                    if (entityLiving instanceof EntityEnderman) {
                        if (item.getItem() instanceof ItemBlock) {
                            ItemBlock b = (ItemBlock) item.getItem();
                            ((EntityEnderman) entityLiving).setHeldBlockState(b.getBlock().getStateFromMeta(b.getMetadata(item.getItemDamage())));
                        }
                    } else {
                        entityLiving.setHeldItem(EnumHand.MAIN_HAND, item);
                    }
                }
            });
        } else {
            final float total = getTotal(items);
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getEntityLiving();
                if (entityLiving != null) {
                    ItemStack item = getRandomItem(items, total);
                    if (entityLiving instanceof EntityEnderman) {
                        if (item.getItem() instanceof ItemBlock) {
                            ItemBlock b = (ItemBlock) item.getItem();
                            ((EntityEnderman) entityLiving).setHeldBlockState(b.getBlock().getStateFromMeta(b.getMetadata(item.getItemDamage())));
                        }
                    } else {
                        entityLiving.setHeldItem(EnumHand.MAIN_HAND, item);
                    }
                }
            });
        }
    }

    private void addMobNBT(AttributeMap map) {
        String mobnbt = map.get(ACTION_MOBNBT);
        if (mobnbt != null) {
            NBTTagCompound tagCompound;
            try {
                tagCompound = JsonToNBT.getTagFromJson(mobnbt);
            } catch (NBTException e) {
                InControl.logger.log(Level.ERROR, "Bad NBT for mob!");
                return;
            }
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getEntityLiving();
                entityLiving.readEntityFromNBT(tagCompound);
            });
        }
    }

    private void addAngryAction(AttributeMap map) {
        if (map.get(ACTION_ANGRY)) {
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
        float m = map.has(ACTION_HEALTHMULTIPLY) ? map.get(ACTION_HEALTHMULTIPLY) : 1;
        float a = map.has(ACTION_HEALTHADD) ? map.get(ACTION_HEALTHADD) : 0;
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
        float m = map.has(ACTION_SPEEDMULTIPLY) ? map.get(ACTION_SPEEDMULTIPLY) : 1;
        float a = map.has(ACTION_SPEEDADD) ? map.get(ACTION_SPEEDADD) : 0;
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
        float m = map.has(ACTION_SIZEMULTIPLY) ? map.get(ACTION_SIZEMULTIPLY) : 1;
        float a = map.has(ACTION_SIZEADD) ? map.get(ACTION_SIZEADD) : 0;
        actions.add(event -> {
            EntityLivingBase entityLiving = event.getEntityLiving();
            if (entityLiving != null) {
                // Not implemented yet
//                entityLiving.setSize(entityLiving.width * m + a, entityLiving.height * m + a);
            }
        });
    }

    private void addDamageAction(AttributeMap map) {
        float m = map.has(ACTION_DAMAGEMULTIPLY) ? map.get(ACTION_DAMAGEMULTIPLY) : 1;
        float a = map.has(ACTION_DAMAGEADD) ? map.get(ACTION_DAMAGEADD) : 0;
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

    public boolean match(LivingSpawnEvent.CheckSpawn event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public boolean match(EntityJoinWorldEvent event) {
        return ruleEvaluator.match(event, EVENT_QUERY_JOIN);
    }

    public void action(LivingSpawnEvent.CheckSpawn event) {
        EventGetter getter = new EventGetter() {
            @Override
            public EntityLivingBase getEntityLiving() {
                return event.getEntityLiving();
            }

            @Override
            public World getWorld() {
                return event.getWorld();
            }
        };
        for (Consumer<EventGetter> action : actions) {
            action.accept(getter);
        }
    }

    public void action(EntityJoinWorldEvent event) {
        EventGetter getter = new EventGetter() {
            @Override
            public EntityLivingBase getEntityLiving() {
                return event.getEntity() instanceof EntityLivingBase ? (EntityLivingBase) event.getEntity() : null;
            }

            @Override
            public World getWorld() {
                return event.getWorld();
            }
        };
        for (Consumer<EventGetter> action : actions) {
            action.accept(getter);
        }
    }


    public Event.Result getResult() {
        return result;
    }

    public boolean isOnJoin() {
        return onJoin;
    }

    public static SpawnRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = FACTORY.parse(element);
            boolean onJoin = element.getAsJsonObject().has("onjoin") && element.getAsJsonObject().get("onjoin").getAsBoolean();
            return new SpawnRule(map, onJoin);
        }
    }
}
