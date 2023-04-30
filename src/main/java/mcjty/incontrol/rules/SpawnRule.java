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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Consumer;

import static mcjty.incontrol.rules.support.RuleKeys.*;

public class SpawnRule extends RuleBase<RuleBase.EventGetter> {

    public static final IEventQuery<MobSpawnEvent.FinalizeSpawn> EVENT_QUERY = new IEventQuery<>() {
        @Override
        public LevelAccessor getWorld(MobSpawnEvent.FinalizeSpawn o) {
            return o.getLevel();
        }

        @Override
        public BlockPos getPos(MobSpawnEvent.FinalizeSpawn o) {
            return new BlockPos((int) o.getX(), (int) o.getY(), (int) o.getZ());
        }

        @Override
        public BlockPos getValidBlockPos(MobSpawnEvent.FinalizeSpawn o) {
            return new BlockPos((int) o.getX(), (int) (o.getY() - 1), (int) o.getZ());
        }

        @Override
        public int getY(MobSpawnEvent.FinalizeSpawn o) {
            return (int) o.getY();
        }

        @Override
        public Entity getEntity(MobSpawnEvent.FinalizeSpawn o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(MobSpawnEvent.FinalizeSpawn o) {
            return null;
        }

        @Override
        public Entity getAttacker(MobSpawnEvent.FinalizeSpawn o) {
            return null;
        }

        @Override
        public Player getPlayer(MobSpawnEvent.FinalizeSpawn o) {
            return getClosestPlayer(o.getLevel(), new BlockPos((int) o.getX(), (int) o.getY(), (int) o.getZ()));
        }

        @Override
        public ItemStack getItem(MobSpawnEvent.FinalizeSpawn o) {
            return ItemStack.EMPTY;
        }
    };
    public static final IEventQuery<EntityJoinLevelEvent> EVENT_QUERY_JOIN = new IEventQuery<>() {
        @Override
        public Level getWorld(EntityJoinLevelEvent o) {
            return o.getLevel();
        }

        @Override
        public BlockPos getPos(EntityJoinLevelEvent o) {
            return o.getEntity().blockPosition();
        }

        @Override
        public BlockPos getValidBlockPos(EntityJoinLevelEvent o) {
            return o.getEntity().blockPosition().below();
        }

        @Override
        public int getY(EntityJoinLevelEvent o) {
            return o.getEntity().blockPosition().getY();
        }

        @Override
        public Entity getEntity(EntityJoinLevelEvent o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(EntityJoinLevelEvent o) {
            return null;
        }

        @Override
        public Entity getAttacker(EntityJoinLevelEvent o) {
            return null;
        }

        @Override
        public Player getPlayer(EntityJoinLevelEvent o) {
            return getClosestPlayer(o.getLevel(), o.getEntity().blockPosition());
        }

        @Override
        public ItemStack getItem(EntityJoinLevelEvent o) {
            return ItemStack.EMPTY;
        }
    };
    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();

    private static Player getClosestPlayer(LevelAccessor world, BlockPos pos) {
        return world.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 100, false);
    }

    static {
        FACTORY
                .attribute(Attribute.create(ONJOIN))
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
                .attribute(Attribute.createMulti(BUILDING))
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

    private final boolean onJoin;
    private final GenericRuleEvaluator ruleEvaluator;
    private final Set<String> phases;
    private Event.Result result = null;
    private boolean doContinue = false;

    private SpawnRule(AttributeMap map, boolean onJoin, Set<String> phases) {
        super(InControl.setup.getLogger());
        this.onJoin = onJoin;
        this.phases = phases;
        ruleEvaluator = new GenericRuleEvaluator(map);
        addActions(map, new ModRuleCompatibilityLayer());
        if (!map.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            map.getKeys().forEach(k -> buffer.append(k).append(' '));
            ErrorHandler.error("Invalid keywords in spawn rule: " + buffer);
        }
    }

    public Set<String> getPhases() {
        return phases;
    }

    public static SpawnRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = FACTORY.parse(element, "spawn.json");
            boolean onJoin = element.getAsJsonObject().has("onjoin") && element.getAsJsonObject().get("onjoin").getAsBoolean();
            return new SpawnRule(map, onJoin, PhaseTools.getPhases(element));
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
        map.consume(ACTION_CONTINUE, v -> this.doContinue = v);
    }

    public boolean match(MobSpawnEvent.FinalizeSpawn event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public boolean match(EntityJoinLevelEvent event) {
        return ruleEvaluator.match(event, EVENT_QUERY_JOIN);
    }

    public void action(MobSpawnEvent.FinalizeSpawn event) {
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

    public void action(EntityJoinLevelEvent event) {
        EventGetter getter = new EventGetter() {
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
                return event.getLevel();
            }

            @Override
            public BlockPos getPosition() {
                return event.getEntity() != null ? event.getEntity().blockPosition() : null;
            }
        };
        for (Consumer<EventGetter> action : actions) {
            action.accept(getter);
        }
    }

    @Nullable
    public Event.Result getResult() {
        return result;
    }

    public boolean isDoContinue() {
        return doContinue;
    }

    public boolean isOnJoin() {
        return onJoin;
    }
}
