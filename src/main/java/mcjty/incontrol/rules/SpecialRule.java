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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Consumer;

import static mcjty.incontrol.rules.support.RuleKeys.*;

public class SpecialRule extends RuleBase<RuleBase.EventGetter> {

    public static final IEventQuery<LivingSpawnEvent.SpecialSpawn> EVENT_QUERY = new IEventQuery<>() {
        @Override
        public LevelAccessor getWorld(LivingSpawnEvent.SpecialSpawn o) {
            return o.getLevel();
        }

        @Override
        public BlockPos getPos(LivingSpawnEvent.SpecialSpawn o) {
            return new BlockPos(o.getX(), o.getY(), o.getZ());
        }

        @Override
        public BlockPos getValidBlockPos(LivingSpawnEvent.SpecialSpawn o) {
            return new BlockPos(o.getX(), o.getY() - 1, o.getZ());
        }

        @Override
        public int getY(LivingSpawnEvent.SpecialSpawn o) {
            return (int) o.getY();
        }

        @Override
        public Entity getEntity(LivingSpawnEvent.SpecialSpawn o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(LivingSpawnEvent.SpecialSpawn o) {
            return null;
        }

        @Override
        public Entity getAttacker(LivingSpawnEvent.SpecialSpawn o) {
            return null;
        }

        @Override
        public Player getPlayer(LivingSpawnEvent.SpecialSpawn o) {
            return getClosestPlayer(o.getLevel(), new BlockPos(o.getX(), o.getY(), o.getZ()));
        }

        @Override
        public ItemStack getItem(LivingSpawnEvent.SpecialSpawn o) {
            return ItemStack.EMPTY;
        }
    };

    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();

    private static Player getClosestPlayer(LevelAccessor world, BlockPos pos) {
        return world.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 100, false);
    }

    static {
        FACTORY
                .attribute(Attribute.create(PHASE))
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
                .attribute(Attribute.create(SPAWNER))
                .attribute(Attribute.create(INCONTROL))
                .attribute(Attribute.create(INBUILDING))
                .attribute(Attribute.create(INCITY))
                .attribute(Attribute.create(INSTREET))
                .attribute(Attribute.create(INSPHERE))
                .attribute(Attribute.create(GAMESTAGE))
                .attribute(Attribute.create(PASSIVE))
                .attribute(Attribute.create(HOSTILE))
                .attribute(Attribute.create(BABY))
                .attribute(Attribute.create(SEESKY))
                .attribute(Attribute.create(SLIME))
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

                .attribute(Attribute.createMulti(HELMET))
                .attribute(Attribute.createMulti(CHESTPLATE))
                .attribute(Attribute.createMulti(LEGGINGS))
                .attribute(Attribute.createMulti(BOOTS))
                .attribute(Attribute.createMulti(AMULET))
                .attribute(Attribute.createMulti(RING))
                .attribute(Attribute.createMulti(BELT))
                .attribute(Attribute.createMulti(TRINKET))
                .attribute(Attribute.createMulti(HEAD))
                .attribute(Attribute.createMulti(BODY))
                .attribute(Attribute.createMulti(CHARM))
                .attribute(Attribute.createMulti(PLAYER_HELDITEM))
                .attribute(Attribute.createMulti(OFFHANDITEM))
                .attribute(Attribute.createMulti(BOTHHANDSITEM))

                .attribute(Attribute.create(ACTION_CONTINUE))
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
                .attribute(Attribute.create(ACTION_MOBNBT))
                .attribute(Attribute.create(ACTION_CUSTOMNAME))
                .attribute(Attribute.createMulti(ACTION_HELDITEM))
                .attribute(Attribute.createMulti(ACTION_ARMORBOOTS))
                .attribute(Attribute.createMulti(ACTION_ARMORLEGS))
                .attribute(Attribute.createMulti(ACTION_ARMORCHEST))
                .attribute(Attribute.createMulti(ACTION_ARMORHELMET))
                .attribute(Attribute.createMulti(ACTION_POTION))
        ;
    }

    public enum SpecialResult {
        BEFORE,
        AFTER,
        REPLACE
    }


    private final GenericRuleEvaluator ruleEvaluator;
    private final Set<String> phases;
    private SpecialResult result = SpecialResult.AFTER;
    private boolean doContinue = false;

    private SpecialRule(AttributeMap map, Set<String> phases) {
        super(InControl.setup.getLogger());
        this.phases = phases;
        ruleEvaluator = new GenericRuleEvaluator(map);
        addActions(map, new ModRuleCompatibilityLayer());
        if (!map.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            map.getKeys().forEach(k -> buffer.append(k).append(' '));
            ErrorHandler.error("Invalid keywords in special rule: " + buffer);
        }
    }

    public Set<String> getPhases() {
        return phases;
    }

    public static SpecialRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = FACTORY.parse(element, "spawn.json");
            return new SpecialRule(map, PhaseTools.getPhases(element));
        }
    }

    @Override
    protected void addActions(AttributeMap map, IModRuleCompatibilityLayer layer) {
        super.addActions(map, layer);

        map.consumeOrElse(ACTION_RESULT, br -> {
            if ("before".equals(br)) {
                this.result = SpecialResult.BEFORE;
            } else if ("replace".equals(br)) {
                this.result = SpecialResult.REPLACE;
            } else {
                this.result = SpecialResult.AFTER;
            }
        }, () -> {
            this.result = SpecialResult.AFTER;
        });
        map.consume(ACTION_CONTINUE, v -> this.doContinue = v);
    }

    public boolean match(LivingSpawnEvent.SpecialSpawn event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public void action(LivingSpawnEvent.SpecialSpawn event) {
        EventGetter getter = new EventGetter() {
            @Override
            public LivingEntity getEntityLiving() {
                return event.getEntity();
            }

            @Override
            public Player getPlayer() {
                return null;
            }

            @Override
            public LevelAccessor getWorld() {
                return event.getLevel();
            }

            @Override
            public BlockPos getPosition() {
                return event.getEntity().blockPosition();
            }
        };
        for (Consumer<EventGetter> action : actions) {
            action.accept(getter);
        }
    }

    @Nonnull
    public SpecialResult getResult() {
        return result;
    }

    public boolean isDoContinue() {
        return doContinue;
    }
}
