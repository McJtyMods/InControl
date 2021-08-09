package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import mcjty.incontrol.InControl;
import mcjty.incontrol.compat.ModRuleCompatibilityLayer;
import mcjty.incontrol.rules.support.GenericRuleEvaluator;
import mcjty.tools.rules.IEventQuery;
import mcjty.tools.rules.IModRuleCompatibilityLayer;
import mcjty.tools.rules.RuleBase;
import mcjty.tools.typed.Attribute;
import mcjty.tools.typed.AttributeMap;
import mcjty.tools.typed.GenericAttributeMapFactory;
import mcjty.tools.typed.Key;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static mcjty.incontrol.rules.support.RuleKeys.*;


public class SummonAidRule extends RuleBase<SummonEventGetter> {

    public static final IEventQuery<ZombieEvent.SummonAidEvent> EVENT_QUERY = new IEventQuery<ZombieEvent.SummonAidEvent>() {
        @Override
        public World getWorld(ZombieEvent.SummonAidEvent o) {
            return o.getWorld();
        }

        @Override
        public BlockPos getPos(ZombieEvent.SummonAidEvent o) {
            return new BlockPos(o.getX(), o.getY(), o.getZ());
        }

        @Override
        public BlockPos getValidBlockPos(ZombieEvent.SummonAidEvent o) {
            return new BlockPos(o.getX(), o.getY() - 1, o.getZ());
        }

        @Override
        public int getY(ZombieEvent.SummonAidEvent o) {
            return o.getY();
        }

        @Override
        public Entity getEntity(ZombieEvent.SummonAidEvent o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(ZombieEvent.SummonAidEvent o) {
            return null;
        }

        @Override
        public Entity getAttacker(ZombieEvent.SummonAidEvent o) {
            return null;
        }

        @Override
        public PlayerEntity getPlayer(ZombieEvent.SummonAidEvent o) {
            return null;
        }

        @Override
        public ItemStack getItem(ZombieEvent.SummonAidEvent o) {
            return ItemStack.EMPTY;
        }
    };
    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    private static Random rnd = new Random();

    static {
        FACTORY
                .attribute(Attribute.create(MINTIME))
                .attribute(Attribute.create(MAXTIME))
                .attribute(Attribute.create(MINDAYCOUNT))
                .attribute(Attribute.create(MAXDAYCOUNT))
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
                .attribute(Attribute.create(INSPHERE))
                .attribute(Attribute.create(PASSIVE))
                .attribute(Attribute.create(HOSTILE))
                .attribute(Attribute.create(SEESKY))
                .attribute(Attribute.create(WEATHER))
                .attribute(Attribute.createMulti(CATEGORY))
                .attribute(Attribute.create(DIFFICULTY))
                .attribute(Attribute.create(STRUCTURE))
                .attribute(Attribute.create(WINTER))
                .attribute(Attribute.create(SUMMER))
                .attribute(Attribute.create(SPRING))
                .attribute(Attribute.create(AUTUMN))
                .attribute(Attribute.createMulti(MOB))
                .attribute(Attribute.createMulti(MOD))
                .attribute(Attribute.createMulti(BLOCK))
                .attribute(Attribute.create(BLOCKOFFSET))
                .attribute(Attribute.createMulti(BIOME))
                .attribute(Attribute.createMulti(BIOMETYPE))
                .attribute(Attribute.createMulti(DIMENSION))
                .attribute(Attribute.createMulti(DIMENSION_MOD))
                .attribute(Attribute.create(STATE))

                .attribute(Attribute.create(ACTION_RESULT))
                .attribute(Attribute.create(ACTION_MESSAGE))
                .attribute(Attribute.create(ACTION_HEALTHMULTIPLY))
                .attribute(Attribute.create(ACTION_HEALTHADD))
                .attribute(Attribute.create(ACTION_SPEEDMULTIPLY))
                .attribute(Attribute.create(ACTION_SPEEDADD))
                .attribute(Attribute.create(ACTION_DAMAGEMULTIPLY))
                .attribute(Attribute.create(ACTION_DAMAGEADD))
                .attribute(Attribute.create(ACTION_SIZEMULTIPLY))
                .attribute(Attribute.create(ACTION_SIZEADD))
                .attribute(Attribute.create(ACTION_ANGRY))
                .attribute(Attribute.createMulti(ACTION_HELDITEM))
                .attribute(Attribute.createMulti(ACTION_ARMORBOOTS))
                .attribute(Attribute.createMulti(ACTION_ARMORLEGS))
                .attribute(Attribute.createMulti(ACTION_ARMORCHEST))
                .attribute(Attribute.createMulti(ACTION_ARMORHELMET))
                .attribute(Attribute.createMulti(ACTION_POTION))
        ;
    }

    private final GenericRuleEvaluator ruleEvaluator;
    private Event.Result result;

    private SummonAidRule(AttributeMap map) {
        super(InControl.setup.getLogger());
        ruleEvaluator = new GenericRuleEvaluator(map);
        addActions(map, new ModRuleCompatibilityLayer());
    }

    public static SummonAidRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = FACTORY.parse(element);
            return new SummonAidRule(map);
        }
    }

    @Override
    protected void addActions(AttributeMap map, IModRuleCompatibilityLayer layer) {
        super.addActions(map, layer);

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

        if (map.has(ACTION_ANGRY)) {
            addAngryAction(map);
        }
        if (map.has(ACTION_HELDITEM)) {
            addHeldItem(map);
        }
        if (map.has(ACTION_ARMORBOOTS)) {
            addArmorItem(map, ACTION_ARMORBOOTS, EquipmentSlotType.FEET);
        }
        if (map.has(ACTION_ARMORLEGS)) {
            addArmorItem(map, ACTION_ARMORLEGS, EquipmentSlotType.LEGS);
        }
        if (map.has(ACTION_ARMORHELMET)) {
            addArmorItem(map, ACTION_ARMORHELMET, EquipmentSlotType.HEAD);
        }
        if (map.has(ACTION_ARMORCHEST)) {
            addArmorItem(map, ACTION_ARMORCHEST, EquipmentSlotType.CHEST);
        }
        if (map.has(ACTION_POTION)) {
            addPotionsAction(map);
        }
    }

    private void addPotionsAction(AttributeMap map) {
        List<EffectInstance> effects = new ArrayList<>();
        for (String p : map.getList(ACTION_POTION)) {
            String[] splitted = StringUtils.split(p, ',');
            if (splitted == null || splitted.length != 3) {
                InControl.setup.getLogger().log(Level.ERROR, "Bad potion specifier '" + p + "'! Use <potion>,<duration>,<amplifier>");
                continue;
            }
            Effect potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(splitted[0]));
            if (potion == null) {
                InControl.setup.getLogger().log(Level.ERROR, "Can't find potion '" + p + "'!");
                continue;
            }
            int duration = 0;
            int amplifier = 0;
            try {
                duration = Integer.parseInt(splitted[1]);
                amplifier = Integer.parseInt(splitted[2]);
            } catch (NumberFormatException e) {
                InControl.setup.getLogger().log(Level.ERROR, "Bad duration or amplifier integer for '" + p + "'!");
                continue;
            }
            effects.add(new EffectInstance(potion, duration, amplifier));
        }
        if (!effects.isEmpty()) {
            actions.add(event -> {
                LivingEntity living = event.getZombieHelper();
                for (EffectInstance effect : effects) {
                    EffectInstance neweffect = new EffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier());
                    living.addEffect(neweffect);
                }
            });
        }
    }

    private void addArmorItem(AttributeMap map, Key<String> itemKey, EquipmentSlotType slot) {
        List<Pair<Float, ItemStack>> items = getItemsWeighted(map.getList(itemKey));
        if (items.isEmpty()) {
            return;
        }
        if (items.size() == 1) {
            Pair<Float, ItemStack> pair = items.get(0);
            actions.add(event -> {
                ZombieEntity helper = event.getZombieHelper();
                helper.setItemSlot(slot, pair.getRight().copy());
            });
        } else {
            final float total = getTotal(items);
            actions.add(event -> {
                ItemStack item = getRandomItem(items, total);
                ZombieEntity helper = event.getZombieHelper();
                helper.setItemSlot(slot, item.copy());
            });
        }
    }

    private void addHeldItem(AttributeMap map) {
        List<Pair<Float, ItemStack>> items = getItemsWeighted(map.getList(ACTION_HELDITEM));
        if (items.isEmpty()) {
            return;
        }
        if (items.size() == 1) {
            Pair<Float, ItemStack> pair = items.get(0);
            actions.add(event -> {
                ZombieEntity helper = event.getZombieHelper();
                helper.setItemInHand(Hand.MAIN_HAND, pair.getRight().copy());
            });
        } else {
            final float total = getTotal(items);
            actions.add(event -> {
                ItemStack item = getRandomItem(items, total);
                ZombieEntity helper = event.getZombieHelper();
                helper.setItemInHand(Hand.MAIN_HAND, item.copy());
            });
        }
    }

    private void addAngryAction(AttributeMap map) {
        if (map.get(ACTION_ANGRY)) {
            actions.add(event -> {
                ZombieEntity helper = event.getZombieHelper();
                PlayerEntity player = event.getWorld().getNearestPlayer(helper, 50);
                if (player != null) {
                    helper.setTarget(player);
                }
            });
        }
    }

    private void addHealthAction(AttributeMap map) {
        float m = map.has(ACTION_HEALTHMULTIPLY) ? map.get(ACTION_HEALTHMULTIPLY) : 1;
        float a = map.has(ACTION_HEALTHADD) ? map.get(ACTION_HEALTHADD) : 0;
        actions.add(event -> {
            ZombieEntity helper = event.getZombieHelper();
            ModifiableAttributeInstance entityAttribute = helper.getAttribute(Attributes.MAX_HEALTH);
            if (entityAttribute != null) {
                double newMax = entityAttribute.getBaseValue() * m + a;
                entityAttribute.setBaseValue(newMax);
                helper.setHealth((float) newMax);
            }
        });
    }

    private void addSpeedAction(AttributeMap map) {
        float m = map.has(ACTION_SPEEDMULTIPLY) ? map.get(ACTION_SPEEDMULTIPLY) : 1;
        float a = map.has(ACTION_SPEEDADD) ? map.get(ACTION_SPEEDADD) : 0;
        actions.add(event -> {
            ZombieEntity helper = event.getZombieHelper();
            ModifiableAttributeInstance entityAttribute = helper.getAttribute(Attributes.MOVEMENT_SPEED);
            if (entityAttribute != null) {
                double newMax = entityAttribute.getBaseValue() * m + a;
                entityAttribute.setBaseValue(newMax);
            }
        });
    }

    private void addSizeActions(AttributeMap map) {
        InControl.setup.getLogger().log(Level.WARN, "Mob resizing not implemented yet!");
        float m = map.has(ACTION_SIZEMULTIPLY) ? map.get(ACTION_SIZEMULTIPLY) : 1;
        float a = map.has(ACTION_SIZEADD) ? map.get(ACTION_SIZEADD) : 0;
        actions.add(event -> {
            ZombieEntity helper = event.getZombieHelper();
            // Not implemented yet
//                entityLiving.setSize(entityLiving.width * m + a, entityLiving.height * m + a);
        });
    }

    private void addDamageAction(AttributeMap map) {
        float m = map.has(ACTION_DAMAGEMULTIPLY) ? map.get(ACTION_DAMAGEMULTIPLY) : 1;
        float a = map.has(ACTION_DAMAGEADD) ? map.get(ACTION_DAMAGEADD) : 0;
        actions.add(event -> {
            ZombieEntity helper = event.getZombieHelper();
            ModifiableAttributeInstance entityAttribute = helper.getAttribute(Attributes.ATTACK_DAMAGE);
            if (entityAttribute != null) {
                double newMax = entityAttribute.getBaseValue() * m + a;
                entityAttribute.setBaseValue(newMax);
            }
        });
    }

    public boolean match(ZombieEvent.SummonAidEvent event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public void action(ZombieEvent.SummonAidEvent event) {
        SummonEventGetter getter = new SummonEventGetter() {
            @Override
            public LivingEntity getEntityLiving() {
                return event.getEntity() instanceof LivingEntity ? (LivingEntity) event.getEntity() : null;
            }

            @Override
            public PlayerEntity getPlayer() {
                return null;
            }

            @Override
            public World getWorld() {
                return event.getWorld();
            }

            @Override
            public BlockPos getPosition() {
                return new BlockPos(event.getX(), event.getY(), event.getZ());
            }

            @Override
            public ZombieEntity getZombieHelper() {
                ZombieEntity helper = event.getCustomSummonedAid();
                if (helper == null) {
                    helper = new ZombieEntity(event.getWorld());
                }
                return helper;
            }
        };
        for (Consumer<SummonEventGetter> action : actions) {
            action.accept(getter);
        }
    }

    public Event.Result getResult() {
        return result;
    }
}
