package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.rules.support.GenericRuleEvaluator;
import mcjty.incontrol.tools.rules.IEventQuery;
import mcjty.incontrol.tools.typed.Attribute;
import mcjty.incontrol.tools.typed.AttributeMap;
import mcjty.incontrol.tools.typed.GenericAttributeMapFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import static mcjty.incontrol.rules.support.RuleKeys.*;
import static mcjty.incontrol.tools.rules.CommonRuleKeys.*;

public class PhaseRule {

    private final String name;
    private final GenericRuleEvaluator ruleEvaluator;


    public static final IEventQuery<LevelAccessor> EVENT_QUERY = new IEventQuery<>() {
        @Override
        public LevelAccessor getWorld(LevelAccessor o) {
            return o;
        }

        @Override
        public BlockPos getPos(LevelAccessor o) {
            return null;
        }

        @Override
        public BlockPos getValidBlockPos(LevelAccessor o) {
            return null;
        }

        @Override
        public int getY(LevelAccessor o) {
            return 0;
        }

        @Override
        public Entity getEntity(LevelAccessor o) {
            return null;
        }

        @Override
        public DamageSource getSource(LevelAccessor o) {
            return null;
        }

        @Override
        public Entity getAttacker(LevelAccessor o) {
            return null;
        }

        @Override
        public Player getPlayer(LevelAccessor o) {
            return null;
        }

        @Override
        public ItemStack getItem(LevelAccessor o) {
            return null;
        }
    };

    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    static {
        FACTORY
                .attribute(Attribute.create(MINTIME))
                .attribute(Attribute.create(MAXTIME))
                .attribute(Attribute.create(DAYCOUNT))
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

    public boolean match(LevelAccessor world) {
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
