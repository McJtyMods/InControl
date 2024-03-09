package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import org.apache.logging.log4j.Level;

import java.util.Set;
import java.util.function.Consumer;

import static mcjty.incontrol.rules.support.RuleKeys.*;

public class HarvestRule extends RuleBase<RuleBase.EventGetter> {

    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    public static final IEventQuery<BlockEvent.BreakEvent> EVENT_QUERY = new IEventQuery<BlockEvent.BreakEvent>() {
        @Override
        public LevelAccessor getWorld(BlockEvent.BreakEvent o) {
            return o.getLevel();
        }

        @Override
        public BlockPos getPos(BlockEvent.BreakEvent o) {
            return o.getPos();
        }

        @Override
        public BlockPos getValidBlockPos(BlockEvent.BreakEvent o) {
            return o.getPos();
        }

        @Override
        public int getY(BlockEvent.BreakEvent o) {
            return o.getPos().getY();
        }

        @Override
        public Entity getEntity(BlockEvent.BreakEvent o) {
            return o.getPlayer();
        }

        @Override
        public DamageSource getSource(BlockEvent.BreakEvent o) {
            return null;
        }

        @Override
        public Entity getAttacker(BlockEvent.BreakEvent o) {
            return null;
        }

        @Override
        public Player getPlayer(BlockEvent.BreakEvent o) {
            return o.getPlayer();
        }

        @Override
        public ItemStack getItem(BlockEvent.BreakEvent o) {
            return ItemStack.EMPTY;
        }
    };

    static {
        FACTORY
                .attribute(Attribute.create(PHASE))
                .attribute(Attribute.create(NUMBER))

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
                .attribute(Attribute.create(SEESKY))
                .attribute(Attribute.create(WEATHER))
                .attribute(Attribute.createMulti(BIOMETAGS))
                .attribute(Attribute.create(DIFFICULTY))
                .attribute(Attribute.createMulti(STRUCTURE))
                .attribute(Attribute.createMulti(STRUCTURETAGS))

                .attribute(Attribute.create(GAMESTAGE))

                .attribute(Attribute.create(WINTER))
                .attribute(Attribute.create(SUMMER))
                .attribute(Attribute.create(SPRING))
                .attribute(Attribute.create(AUTUMN))

                .attribute(Attribute.create(INBUILDING))
                .attribute(Attribute.create(INCITY))
                .attribute(Attribute.create(INSTREET))
                .attribute(Attribute.create(INSPHERE))

                .attribute(Attribute.createMulti(AMULET))
                .attribute(Attribute.createMulti(RING))
                .attribute(Attribute.createMulti(BELT))
                .attribute(Attribute.createMulti(TRINKET))
                .attribute(Attribute.createMulti(HEAD))
                .attribute(Attribute.createMulti(BODY))
                .attribute(Attribute.createMulti(CHARM))

                .attribute(Attribute.create(AREA))
                .attribute(Attribute.createMulti(BLOCK))
                .attribute(Attribute.create(BLOCKOFFSET))
                .attribute(Attribute.createMulti(HELMET))
                .attribute(Attribute.createMulti(CHESTPLATE))
                .attribute(Attribute.createMulti(LEGGINGS))
                .attribute(Attribute.createMulti(BOOTS))
                .attribute(Attribute.createMulti(LACKHELMET))
                .attribute(Attribute.createMulti(LACKCHESTPLATE))
                .attribute(Attribute.createMulti(LACKLEGGINGS))
                .attribute(Attribute.createMulti(LACKBOOTS))
                .attribute(Attribute.createMulti(HELDITEM))
                .attribute(Attribute.createMulti(PLAYER_HELDITEM))
                .attribute(Attribute.createMulti(LACKHELDITEM))
                .attribute(Attribute.createMulti(OFFHANDITEM))
                .attribute(Attribute.createMulti(LACKOFFHANDITEM))
                .attribute(Attribute.createMulti(BOTHHANDSITEM))
                .attribute(Attribute.createMulti(BIOME))
                .attribute(Attribute.createMulti(BIOMETYPE))
                .attribute(Attribute.createMulti(DIMENSION))
                .attribute(Attribute.createMulti(DIMENSION_MOD))

                .attribute(Attribute.create(ACTION_CUSTOMEVENT))
                .attribute(Attribute.create(ACTION_COMMAND))
                .attribute(Attribute.create(ACTION_ADDSTAGE))
                .attribute(Attribute.create(ACTION_REMOVESTAGE))
                .attribute(Attribute.create(ACTION_MESSAGE))
                .attribute(Attribute.create(ACTION_FIRE))
                .attribute(Attribute.create(ACTION_EXPLOSION))
                .attribute(Attribute.create(ACTION_CLEAR))
                .attribute(Attribute.create(ACTION_DAMAGE))
                .attribute(Attribute.create(ACTION_SETBLOCK))
                .attribute(Attribute.create(ACTION_SETHELDITEM))
                .attribute(Attribute.create(ACTION_SETHELDAMOUNT))
                .attribute(Attribute.create(ACTION_RESULT))
                .attribute(Attribute.create(ACTION_SETSTATE))
                .attribute(Attribute.create(ACTION_SETPSTATE))
                .attribute(Attribute.create(ACTION_SETPHASE))
                .attribute(Attribute.create(ACTION_CLEARPHASE))
                .attribute(Attribute.create(ACTION_TOGGLEPHASE))
                .attribute(Attribute.create(ACTION_CHANGENUMBER))
                .attribute(Attribute.createMulti(ACTION_POTION))
                .attribute(Attribute.createMulti(ACTION_GIVE))
                .attribute(Attribute.createMulti(ACTION_DROP))
        ;
    }

    private Event.Result result;
    private final GenericRuleEvaluator ruleEvaluator;

    private HarvestRule(AttributeMap map, Set<String> phases) {
        super(phases);
        ruleEvaluator = new GenericRuleEvaluator(map);
        addActions(map, new ModRuleCompatibilityLayer());
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
    }

    public boolean match(BlockEvent.BreakEvent event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public void action(BlockEvent.BreakEvent event) {
        EventGetter getter = new EventGetter() {
            @Override
            public LivingEntity getEntityLiving() {
                return event.getPlayer();
            }

            @Override
            public Player getPlayer() {
                return event.getPlayer();
            }

            @Override
            public LevelAccessor getWorld() {
                return event.getLevel();
            }

            @Override
            public BlockPos getPosition() {
                return event.getPos();
            }
        };
        for (Consumer<EventGetter> action : actions) {
            action.accept(getter);
        }
    }

    public Event.Result getResult() {
        return result;
    }


    public static HarvestRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = null;
            try {
                map = FACTORY.parse(element, "breakevents.json");
            } catch (Exception e) {
                InControl.setup.getLogger().log(Level.ERROR, e);
                return null;
            }
            return new HarvestRule(map, PhaseTools.getPhases(element));
        }
    }
}
