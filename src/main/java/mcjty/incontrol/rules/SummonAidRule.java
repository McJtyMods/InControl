package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import mcjty.incontrol.InControl;
import mcjty.incontrol.typed.Attribute;
import mcjty.incontrol.typed.AttributeMap;
import mcjty.incontrol.typed.GenericAttributeMapFactory;
import mcjty.incontrol.typed.Key;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static mcjty.incontrol.rules.RuleKeys.*;


public class SummonAidRule {

    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    public static final IEventQuery EVENT_QUERY = new IEventQuery() {
        @Override
        public World getWorld(Object o) {
            return ((ZombieEvent.SummonAidEvent) o).getWorld();
        }

        @Override
        public BlockPos getPos(Object o) {
            ZombieEvent.SummonAidEvent s = (ZombieEvent.SummonAidEvent) o;
            return new BlockPos(s.getX(), s.getY(), s.getZ());
        }

        @Override
        public int getY(Object o) {
            return ((ZombieEvent.SummonAidEvent) o).getY();
        }

        @Override
        public Entity getEntity(Object o) {
            return ((ZombieEvent.SummonAidEvent) o).getEntity();
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

    private Event.Result result;
    private final GenericRuleEvaluator ruleEvaluator;
    private final List<Consumer<ZombieEvent.SummonAidEvent>> actions = new ArrayList<>();

    private SummonAidRule(AttributeMap map) {
        ruleEvaluator = new GenericRuleEvaluator(map);
        addActions(map);
    }

    private void addActions(AttributeMap map) {
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

    private EntityZombie getHelper(ZombieEvent.SummonAidEvent event) {
        EntityZombie helper = event.getCustomSummonedAid();
        if (helper == null) {
            helper = new EntityZombie(event.getWorld());
        }
        return helper;
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
                EntityLivingBase living = getHelper(event);
                for (PotionEffect effect : effects) {
                    PotionEffect neweffect = new PotionEffect(effect.getPotion(), effect.getDuration(), effect.getAmplifier());
                    living.addPotionEffect(neweffect);
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
                EntityZombie helper = getHelper(event);
                helper.setItemStackToSlot(slot, new ItemStack(item));
            });
        } else {
            actions.add(event -> {
                EntityZombie helper = getHelper(event);
                helper.setItemStackToSlot(slot, new ItemStack(items.get(rnd.nextInt(items.size()))));
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
                EntityZombie helper = getHelper(event);
                helper.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(item));
            });
        } else {
            actions.add(event -> {
                EntityZombie helper = getHelper(event);
                helper.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(items.get(rnd.nextInt(items.size()))));
            });
        }
    }

    private void addAngryAction(AttributeMap map) {
        if (map.get(ANGRY)) {
            actions.add(event -> {
                EntityZombie helper = getHelper(event);
                EntityPlayer player = event.getWorld().getClosestPlayerToEntity(helper, 50);
                if (player != null) {
                    helper.setAttackTarget(player);
                }
            });
        }
    }

    private void addHealthAction(AttributeMap map) {
        float m = map.has(HEALTHMULTIPLY) ? map.get(HEALTHMULTIPLY) : 1;
        float a = map.has(HEALTHADD) ? map.get(HEALTHADD) : 0;
        actions.add(event -> {
            EntityZombie helper = getHelper(event);
            IAttributeInstance entityAttribute = helper.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
            if (entityAttribute != null) {
                double newMax = entityAttribute.getBaseValue() * m + a;
                entityAttribute.setBaseValue(newMax);
                helper.setHealth((float) newMax);
            }
        });
    }

    private void addSpeedAction(AttributeMap map) {
        float m = map.has(SPEEDMULTIPLY) ? map.get(SPEEDMULTIPLY) : 1;
        float a = map.has(SPEEDADD) ? map.get(SPEEDADD) : 0;
        actions.add(event -> {
            EntityZombie helper = getHelper(event);
            IAttributeInstance entityAttribute = helper.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            if (entityAttribute != null) {
                double newMax = entityAttribute.getBaseValue() * m + a;
                entityAttribute.setBaseValue(newMax);
            }
        });
    }

    private void addSizeActions(AttributeMap map) {
        InControl.logger.log(Level.WARN, "Mob resizing not implemented yet!");
        float m = map.has(SIZEMULTIPLY) ? map.get(SIZEMULTIPLY) : 1;
        float a = map.has(SIZEADD) ? map.get(SIZEADD) : 0;
        actions.add(event -> {
            EntityZombie helper = getHelper(event);
            // Not implemented yet
//                entityLiving.setSize(entityLiving.width * m + a, entityLiving.height * m + a);
        });
    }

    private void addDamageAction(AttributeMap map) {
        float m = map.has(DAMAGEMULTIPLY) ? map.get(DAMAGEMULTIPLY) : 1;
        float a = map.has(DAMAGEADD) ? map.get(DAMAGEADD) : 0;
        actions.add(event -> {
            EntityZombie helper = getHelper(event);
            IAttributeInstance entityAttribute = helper.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
            if (entityAttribute != null) {
                double newMax = entityAttribute.getBaseValue() * m + a;
                entityAttribute.setBaseValue(newMax);
            }
        });
    }

    private static Random rnd = new Random();

    public boolean match(ZombieEvent.SummonAidEvent event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }


    public void action(ZombieEvent.SummonAidEvent event) {
        for (Consumer<ZombieEvent.SummonAidEvent> action : actions) {
            action.accept(event);
        }
    }

    public Event.Result getResult() {
        return result;
    }

    public static SummonAidRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = FACTORY.parse(element);
            return new SummonAidRule(map);
        }
    }
}
