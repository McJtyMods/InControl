package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import mcjty.incontrol.compat.ModRuleCompatibilityLayer;
import mcjty.incontrol.data.PhaseTools;
import mcjty.incontrol.rules.support.GenericRuleEvaluator;
import mcjty.incontrol.tools.rules.IEventQuery;
import mcjty.incontrol.tools.rules.IModRuleCompatibilityLayer;
import mcjty.incontrol.tools.rules.RuleBase;
import mcjty.incontrol.tools.typed.Attribute;
import mcjty.incontrol.tools.typed.AttributeMap;
import mcjty.incontrol.tools.typed.GenericAttributeMapFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import static mcjty.incontrol.rules.support.RuleKeys.*;


public class SummonAidRule extends RuleBase<SummonEventGetter> {

    public static final IEventQuery<ZombieEvent.SummonAidEvent> EVENT_QUERY = new IEventQuery<ZombieEvent.SummonAidEvent>() {
        @Override
        public Level getWorld(ZombieEvent.SummonAidEvent o) {
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
        public Player getPlayer(ZombieEvent.SummonAidEvent o) {
            return null;
        }

        @Override
        public ItemStack getItem(ZombieEvent.SummonAidEvent o) {
            return ItemStack.EMPTY;
        }
    };
    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    private static final Random rnd = new Random();

    static {
        FACTORY
                .attribute(Attribute.create(MINTIME))
                .attribute(Attribute.create(MAXTIME))
                .attribute(Attribute.create(DAYCOUNT))
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
                .attribute(Attribute.create(BABY))
                .attribute(Attribute.create(SEESKY))
                .attribute(Attribute.create(WEATHER))
                .attribute(Attribute.createMulti(BIOMETAGS))
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
                .attribute(Attribute.create(ACTION_HEALTHSET))
                .attribute(Attribute.create(ACTION_HEALTHMULTIPLY))
                .attribute(Attribute.create(ACTION_HEALTHADD))
                .attribute(Attribute.create(ACTION_SPEEDSET))
                .attribute(Attribute.create(ACTION_SPEEDMULTIPLY))
                .attribute(Attribute.create(ACTION_SPEEDADD))
                .attribute(Attribute.create(ACTION_DAMAGESET))
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
    private final Set<String> phases;
    private Event.Result result;

    private SummonAidRule(AttributeMap map, Set<String> phases) {
        super(InControl.setup.getLogger());
        this.phases = phases;
        ruleEvaluator = new GenericRuleEvaluator(map);
        addActions(map, new ModRuleCompatibilityLayer());
    }

    public Set<String> getPhases() {
        return phases;
    }

    public static SummonAidRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = FACTORY.parse(element, "summonaid.json");
            return new SummonAidRule(map, PhaseTools.getPhases(element));
        }
    }

    @Override
    protected void addActions(AttributeMap map, IModRuleCompatibilityLayer layer) {
        super.addActions(map, layer);

        map.consumeOrElse(ACTION_RESULT, br -> {
            if ("default".equals(br) || br.startsWith("def")) {
                this.result = Event.Result.DEFAULT;
            } else if ("allow".equals(br) || "true".equals(br)) {
                this.result = Event.Result.ALLOW;
            } else {
                this.result = Event.Result.DENY;
            }
        }, () -> {
            this.result = null;
        });

        if (!map.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            map.getKeys().forEach(k -> buffer.append(k).append(' '));
            ErrorHandler.error("Invalid keywords in additional spawn rule: " + buffer);
        }
    }

    @Override
    protected void addPotionsAction(List<String> potions) {
        List<MobEffectInstance> effects = new ArrayList<>();
        for (String p : potions) {
            String[] splitted = StringUtils.split(p, ',');
            if (splitted == null || splitted.length != 3) {
                InControl.setup.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Bad potion specifier '" + p + "'! Use <potion>,<duration>,<amplifier>");
                continue;
            }
            MobEffect potion = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(splitted[0]));
            if (potion == null) {
                InControl.setup.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Can't find potion '" + p + "'!");
                continue;
            }
            int duration = 0;
            int amplifier = 0;
            try {
                duration = Integer.parseInt(splitted[1]);
                amplifier = Integer.parseInt(splitted[2]);
            } catch (NumberFormatException e) {
                InControl.setup.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Bad duration or amplifier integer for '" + p + "'!");
                continue;
            }
            effects.add(new MobEffectInstance(potion, duration, amplifier));
        }
        if (!effects.isEmpty()) {
            actions.add(event -> {
                LivingEntity living = event.getZombieHelper();
                for (MobEffectInstance effect : effects) {
                    MobEffectInstance neweffect = new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier());
                    living.addEffect(neweffect);
                }
            });
        }
    }

    @Override
    protected void addArmorItem(List<String> itemList, EquipmentSlot slot) {
        List<Pair<Float, ItemStack>> items = getItemsWeighted(itemList);
        if (items.isEmpty()) {
            return;
        }
        if (items.size() == 1) {
            Pair<Float, ItemStack> pair = items.get(0);
            actions.add(event -> {
                Zombie helper = event.getZombieHelper();
                helper.setItemSlot(slot, pair.getRight().copy());
            });
        } else {
            final float total = getTotal(items);
            actions.add(event -> {
                ItemStack item = getRandomItem(items, total);
                Zombie helper = event.getZombieHelper();
                helper.setItemSlot(slot, item.copy());
            });
        }
    }

    @Override
    protected void addHeldItem(List<String> heldItems) {
        List<Pair<Float, ItemStack>> items = getItemsWeighted(heldItems);
        if (items.isEmpty()) {
            return;
        }
        if (items.size() == 1) {
            Pair<Float, ItemStack> pair = items.get(0);
            actions.add(event -> {
                Zombie helper = event.getZombieHelper();
                helper.setItemInHand(InteractionHand.MAIN_HAND, pair.getRight().copy());
            });
        } else {
            final float total = getTotal(items);
            actions.add(event -> {
                ItemStack item = getRandomItem(items, total);
                Zombie helper = event.getZombieHelper();
                helper.setItemInHand(InteractionHand.MAIN_HAND, item.copy());
            });
        }
    }

    @Override
    protected void addAngryAction(boolean angry) {
        if (angry) {
            actions.add(event -> {
                Zombie helper = event.getZombieHelper();
                Player player = event.getWorld().getNearestPlayer(helper, 50);
                if (player != null) {
                    helper.setTarget(player);
                }
            });
        }
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
            public Player getPlayer() {
                return null;
            }

            @Override
            public Level getWorld() {
                return event.getWorld();
            }

            @Override
            public BlockPos getPosition() {
                return new BlockPos(event.getX(), event.getY(), event.getZ());
            }

            @Override
            public Zombie getZombieHelper() {
                Zombie helper = event.getCustomSummonedAid();
                if (helper == null) {
                    helper = new Zombie(event.getWorld());
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
