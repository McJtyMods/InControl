package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.compat.ModRuleCompatibilityLayer;
import mcjty.incontrol.data.PhaseTools;
import mcjty.incontrol.rules.support.GenericRuleEvaluator;
import mcjty.incontrol.rules.support.ICResult;
import mcjty.incontrol.rules.support.SpawnWhen;
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
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

import javax.annotation.Nonnull;
import java.util.Arrays;
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

    public static final IEventQuery<MobSpawnEvent.PositionCheck> EVENT_QUERY_POSITION = new IEventQuery<>() {
        @Override
        public LevelAccessor getWorld(MobSpawnEvent.PositionCheck o) {
            return o.getLevel();
        }

        @Override
        public BlockPos getPos(MobSpawnEvent.PositionCheck o) {
            return new BlockPos((int) o.getX(), (int) o.getY(), (int) o.getZ());
        }

        @Override
        public BlockPos getValidBlockPos(MobSpawnEvent.PositionCheck o) {
            return new BlockPos((int) o.getX(), (int) (o.getY() - 1), (int) o.getZ());
        }

        @Override
        public int getY(MobSpawnEvent.PositionCheck o) {
            return (int) o.getY();
        }

        @Override
        public Entity getEntity(MobSpawnEvent.PositionCheck o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(MobSpawnEvent.PositionCheck o) {
            return null;
        }

        @Override
        public Entity getAttacker(MobSpawnEvent.PositionCheck o) {
            return null;
        }

        @Override
        public Player getPlayer(MobSpawnEvent.PositionCheck o) {
            return getClosestPlayer(o.getLevel(), new BlockPos((int) o.getX(), (int) o.getY(), (int) o.getZ()));
        }

        @Override
        public ItemStack getItem(MobSpawnEvent.PositionCheck o) {
            return ItemStack.EMPTY;
        }
    };

    public static final IEventQuery<MobSpawnEvent.AllowDespawn> EVENT_QUERY_DESPAWN = new IEventQuery<>() {
        @Override
        public LevelAccessor getWorld(MobSpawnEvent.AllowDespawn o) {
            return o.getLevel();
        }

        @Override
        public BlockPos getPos(MobSpawnEvent.AllowDespawn o) {
            return new BlockPos((int) o.getX(), (int) o.getY(), (int) o.getZ());
        }

        @Override
        public BlockPos getValidBlockPos(MobSpawnEvent.AllowDespawn o) {
            return new BlockPos((int) o.getX(), (int) (o.getY() - 1), (int) o.getZ());
        }

        @Override
        public int getY(MobSpawnEvent.AllowDespawn o) {
            return (int) o.getY();
        }

        @Override
        public Entity getEntity(MobSpawnEvent.AllowDespawn o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(MobSpawnEvent.AllowDespawn o) {
            return null;
        }

        @Override
        public Entity getAttacker(MobSpawnEvent.AllowDespawn o) {
            return null;
        }

        @Override
        public Player getPlayer(MobSpawnEvent.AllowDespawn o) {
            return getClosestPlayer(o.getLevel(), new BlockPos((int) o.getX(), (int) o.getY(), (int) o.getZ()));
        }

        @Override
        public ItemStack getItem(MobSpawnEvent.AllowDespawn o) {
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
                .attribute(Attribute.create(WHEN))
                .attribute(Attribute.create(PHASE))
                .attribute(Attribute.create(NUMBER))

                .attribute(Attribute.create(TIME))
                .attribute(Attribute.create(MINTIME))
                .attribute(Attribute.create(MAXTIME))

                .attribute(Attribute.create(DAYCOUNT))
                .attribute(Attribute.create(MINDAYCOUNT))
                .attribute(Attribute.create(MAXDAYCOUNT))

                .attribute(Attribute.create(MINCOUNT))
                .attribute(Attribute.create(MAXCOUNT))

                .attribute(Attribute.create(LIGHT))
                .attribute(Attribute.create(MINLIGHT))
                .attribute(Attribute.create(MAXLIGHT))
                .attribute(Attribute.create(MINLIGHT_FULL))
                .attribute(Attribute.create(MAXLIGHT_FULL))

                .attribute(Attribute.create(HEIGHT))
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
                .attribute(Attribute.create(EVENTSPAWN))
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
                .attribute(Attribute.create(AREA))
                .attribute(Attribute.createMulti(BLOCK))
                .attribute(Attribute.create(BLOCKOFFSET))
                .attribute(Attribute.createMulti(BIOME))
                .attribute(Attribute.createMulti(BIOMETYPE))
                .attribute(Attribute.createMulti(DIMENSION))
                .attribute(Attribute.createMulti(DIMENSION_MOD))
                .attribute(Attribute.create(STATE))
                .attribute(Attribute.createMulti(SCOREBOARDTAGS_ALL))
                .attribute(Attribute.createMulti(SCOREBOARDTAGS_ANY))

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

                .attribute(Attribute.create(ACTION_CUSTOMEVENT))
                .attribute(Attribute.create(ACTION_CONTINUE))
                .attribute(Attribute.create(ACTION_RESULT))
                .attribute(Attribute.create(ACTION_MESSAGE))
                .attribute(Attribute.create(ACTION_ADDSCOREBOARDTAGS))
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
                .attribute(Attribute.create(ACTION_SETPHASE))
                .attribute(Attribute.create(ACTION_CLEARPHASE))
                .attribute(Attribute.create(ACTION_TOGGLEPHASE))
                .attribute(Attribute.create(ACTION_CHANGENUMBER))
                .attribute(Attribute.createMulti(ACTION_HELDITEM))
                .attribute(Attribute.createMulti(ACTION_ARMORBOOTS))
                .attribute(Attribute.createMulti(ACTION_ARMORLEGS))
                .attribute(Attribute.createMulti(ACTION_ARMORCHEST))
                .attribute(Attribute.createMulti(ACTION_ARMORHELMET))
                .attribute(Attribute.createMulti(ACTION_POTION))
                .attribute(Attribute.create(ACTION_NODESPAWN))
        ;
    }

    private final SpawnWhen when;
    private final GenericRuleEvaluator ruleEvaluator;
    private ICResult result = null;
    private boolean doContinue = false;

    private SpawnRule(AttributeMap map, SpawnWhen when, Set<String> phases) {
        super(phases);
        this.when = when;
        ruleEvaluator = new GenericRuleEvaluator(map);
        addActions(map, new ModRuleCompatibilityLayer());
        if (!map.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            map.getKeys().forEach(k -> buffer.append(k).append(' '));
            ErrorHandler.error("Invalid keywords in spawn rule: " + buffer);
        }
    }

    public static SpawnRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = FACTORY.parse(element, "spawn.json");
            String whenS = element.getAsJsonObject().has("when") ? element.getAsJsonObject().get("when").getAsString() : SpawnWhen.POSITION.name();
            SpawnWhen when = SpawnWhen.getByName(whenS);
            if (when == null) {
                ErrorHandler.error("Invalid spawn rule 'when' value '" + whenS + "'!. Should be one of " + Arrays.toString(SpawnWhen.values()) + "");
            }
            return new SpawnRule(map, when, PhaseTools.getPhases(element));
        }
    }

    @Override
    protected void addActions(AttributeMap map, IModRuleCompatibilityLayer layer) {
        super.addActions(map, layer);

        map.consumeOrElse(ACTION_RESULT, br -> {
            if ("default".equals(br) || br.startsWith("def")) {
                this.result = ICResult.DEFAULT;
            } else if ("allow".equals(br) || "true".equals(br)) {
                this.result = ICResult.ALLOW;
            } else if ("deny_with_actions".equals(br)) {
                this.result = ICResult.DENY_WITH_ACTIONS;
            } else {
                this.result = ICResult.DENY;
            }
        }, () -> {
            this.result = ICResult.DEFAULT;
        });
        map.consume(ACTION_CONTINUE, v -> this.doContinue = v);
    }

    public boolean match(MobSpawnEvent.FinalizeSpawn event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public boolean match(MobSpawnEvent.PositionCheck event) {
        return ruleEvaluator.match(event, EVENT_QUERY_POSITION);
    }

    public boolean match(MobSpawnEvent.AllowDespawn event) {
        return ruleEvaluator.match(event, EVENT_QUERY_DESPAWN);
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

    public void action(MobSpawnEvent.PositionCheck event) {
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

    public void action(MobSpawnEvent.AllowDespawn event) {
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

    @Nonnull
    public ICResult getResult() {
        return result;
    }

    public boolean isDoContinue() {
        return doContinue;
    }

    public SpawnWhen getWhen() {
        return when;
    }
}
