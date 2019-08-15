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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import static mcjty.incontrol.rules.support.RuleKeys.*;

public class ExperienceRule extends RuleBase<RuleBase.EventGetter> {

    public static final IEventQuery<LivingExperienceDropEvent> EVENT_QUERY = new IEventQuery<LivingExperienceDropEvent>() {
        @Override
        public World getWorld(LivingExperienceDropEvent o) {
            return o.getEntity().getEntityWorld();
        }

        @Override
        public BlockPos getPos(LivingExperienceDropEvent o) {
            return o.getEntity().getPosition();
        }

        @Override
        public BlockPos getValidBlockPos(LivingExperienceDropEvent o) {
            return o.getEntity().getPosition().down();
        }

        @Override
        public int getY(LivingExperienceDropEvent o) {
            return o.getEntity().getPosition().getY();
        }

        @Override
        public Entity getEntity(LivingExperienceDropEvent o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(LivingExperienceDropEvent o) {
            return null;
        }

        @Override
        public Entity getAttacker(LivingExperienceDropEvent o) {
            return o.getAttackingPlayer();
        }

        @Override
        public EntityPlayer getPlayer(LivingExperienceDropEvent o) {
            return o.getAttackingPlayer();
        }

        @Override
        public ItemStack getItem(LivingExperienceDropEvent o) {
            return ItemStack.EMPTY;
        }
    };
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
                .attribute(Attribute.create(INBUILDING))
                .attribute(Attribute.create(INCITY))
                .attribute(Attribute.create(INSTREET))
                .attribute(Attribute.create(INSPHERE))
                .attribute(Attribute.create(PASSIVE))
                .attribute(Attribute.create(HOSTILE))
                .attribute(Attribute.create(SEESKY))
                .attribute(Attribute.create(WEATHER))
                .attribute(Attribute.create(TEMPCATEGORY))
                .attribute(Attribute.create(DIFFICULTY))
                .attribute(Attribute.create(STRUCTURE))
                .attribute(Attribute.create(PLAYER))
                .attribute(Attribute.create(REALPLAYER))
                .attribute(Attribute.create(FAKEPLAYER))
                .attribute(Attribute.create(WINTER))
                .attribute(Attribute.create(SUMMER))
                .attribute(Attribute.create(SPRING))
                .attribute(Attribute.create(AUTUMN))
                .attribute(Attribute.create(STATE))
                .attribute(Attribute.create(PSTATE))
                .attribute(Attribute.createMulti(MOB))
                .attribute(Attribute.createMulti(MOD))
                .attribute(Attribute.createMulti(BLOCK))
                .attribute(Attribute.create(BLOCKOFFSET))
                .attribute(Attribute.createMulti(BIOME))
                .attribute(Attribute.createMulti(BIOMETYPE))
                .attribute(Attribute.createMulti(DIMENSION))
                .attribute(Attribute.createMulti(HELDITEM))
                .attribute(Attribute.createMulti(PLAYER_HELDITEM))
                .attribute(Attribute.createMulti(OFFHANDITEM))
                .attribute(Attribute.createMulti(BOTHHANDSITEM))

                .attribute(Attribute.create(ACTION_RESULT))
                .attribute(Attribute.create(ACTION_SETXP))
                .attribute(Attribute.create(ACTION_ADDXP))
                .attribute(Attribute.create(ACTION_MULTXP))
        ;
    }

    private final GenericRuleEvaluator ruleEvaluator;
    private Event.Result result;
    private Integer xp = null;
    private float multxp = 1.0f;
    private float addxp = 0.0f;

    private ExperienceRule(AttributeMap map) {
        super(InControl.setup.getLogger());
        ruleEvaluator = new GenericRuleEvaluator(map);
        addActions(map, new ModRuleCompatibilityLayer());
    }

    public static ExperienceRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = FACTORY.parse(element);
            return new ExperienceRule(map);
        }
    }

    public int modifyXp(int xpIn) {
        if (xp != null) {
            xpIn = xp;
        }
        return (int) (xpIn * multxp + addxp);
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
        if (map.has(ACTION_SETXP)) {
            xp = map.get(ACTION_SETXP);
        }
        if (map.has(ACTION_ADDXP)) {
            addxp = map.get(ACTION_ADDXP);
        }
        if (map.has(ACTION_MULTXP)) {
            multxp = map.get(ACTION_MULTXP);
        }

    }

    public boolean match(LivingExperienceDropEvent event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public Event.Result getResult() {
        return result;
    }
}
