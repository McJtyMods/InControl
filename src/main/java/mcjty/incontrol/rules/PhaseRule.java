package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.rules.support.GenericRuleEvaluator;
import mcjty.tools.rules.IEventQuery;
import mcjty.tools.typed.Attribute;
import mcjty.tools.typed.AttributeMap;
import mcjty.tools.typed.GenericAttributeMapFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import static mcjty.incontrol.rules.support.RuleKeys.MAXDAYCOUNT;
import static mcjty.incontrol.rules.support.RuleKeys.MINDAYCOUNT;
import static mcjty.tools.rules.CommonRuleKeys.*;

public class PhaseRule {

    private final String name;
    private final GenericRuleEvaluator ruleEvaluator;


    public static final IEventQuery<IWorld> EVENT_QUERY = new IEventQuery<IWorld>() {
        @Override
        public IWorld getWorld(IWorld o) {
            return o;
        }

        @Override
        public BlockPos getPos(IWorld o) {
            return null;
        }

        @Override
        public BlockPos getValidBlockPos(IWorld o) {
            return null;
        }

        @Override
        public int getY(IWorld o) {
            return 0;
        }

        @Override
        public Entity getEntity(IWorld o) {
            return null;
        }

        @Override
        public DamageSource getSource(IWorld o) {
            return null;
        }

        @Override
        public Entity getAttacker(IWorld o) {
            return null;
        }

        @Override
        public PlayerEntity getPlayer(IWorld o) {
            return null;
        }

        @Override
        public ItemStack getItem(IWorld o) {
            return null;
        }
    };

    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    static {
        FACTORY
                .attribute(Attribute.create(MINTIME))
                .attribute(Attribute.create(MAXTIME))
                .attribute(Attribute.create(MINDAYCOUNT))
                .attribute(Attribute.create(MAXDAYCOUNT))
                .attribute(Attribute.create(WEATHER))
                .attribute(Attribute.create(WINTER))
                .attribute(Attribute.create(SUMMER))
                .attribute(Attribute.create(SPRING))
                .attribute(Attribute.create(AUTUMN))
                .attribute(Attribute.create(STATE))
        ;
    }

    private PhaseRule(String name, AttributeMap map) {
        this.name = name;
        ruleEvaluator = new GenericRuleEvaluator(map);
    }

    public String getName() {
        return name;
    }

    public boolean match(IWorld world) {
        return ruleEvaluator.match(world, EVENT_QUERY);
    }


    public static PhaseRule parse(JsonElement element) {
        if (element == null) {
            return null;
        }
        JsonObject object = element.getAsJsonObject();
        JsonElement conditions = object.get("conditions");
        AttributeMap map = FACTORY.parse(conditions, "phases.json");

        return new PhaseRule(object.get("name").getAsString(), map);
    }


}
