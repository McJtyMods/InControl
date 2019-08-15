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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static mcjty.incontrol.rules.support.RuleKeys.*;


public class SpawnRule extends RuleBase<RuleBase.EventGetter> {

    public static final IEventQuery<LivingSpawnEvent.CheckSpawn> EVENT_QUERY = new IEventQuery<LivingSpawnEvent.CheckSpawn>() {
        @Override
        public World getWorld(LivingSpawnEvent.CheckSpawn o) {
            return o.getWorld();
        }

        @Override
        public BlockPos getPos(LivingSpawnEvent.CheckSpawn o) {
            return new BlockPos(o.getX(), o.getY(), o.getZ());
        }

        @Override
        public BlockPos getValidBlockPos(LivingSpawnEvent.CheckSpawn o) {
            return new BlockPos(o.getX(), o.getY() - 1, o.getZ());
        }

        @Override
        public int getY(LivingSpawnEvent.CheckSpawn o) {
            return (int) o.getY();
        }

        @Override
        public Entity getEntity(LivingSpawnEvent.CheckSpawn o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(LivingSpawnEvent.CheckSpawn o) {
            return null;
        }

        @Override
        public Entity getAttacker(LivingSpawnEvent.CheckSpawn o) {
            return null;
        }

        @Override
        public EntityPlayer getPlayer(LivingSpawnEvent.CheckSpawn o) {
            return getClosestPlayer(o.getWorld(), new BlockPos(o.getX(), o.getY(), o.getZ()));
        }

        @Override
        public ItemStack getItem(LivingSpawnEvent.CheckSpawn o) {
            return ItemStack.EMPTY;
        }
    };
    public static final IEventQuery<EntityJoinWorldEvent> EVENT_QUERY_JOIN = new IEventQuery<EntityJoinWorldEvent>() {
        @Override
        public World getWorld(EntityJoinWorldEvent o) {
            return o.getWorld();
        }

        @Override
        public BlockPos getPos(EntityJoinWorldEvent o) {
            return o.getEntity().getPosition();
        }

        @Override
        public BlockPos getValidBlockPos(EntityJoinWorldEvent o) {
            return o.getEntity().getPosition().down();
        }

        @Override
        public int getY(EntityJoinWorldEvent o) {
            return o.getEntity().getPosition().getY();
        }

        @Override
        public Entity getEntity(EntityJoinWorldEvent o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(EntityJoinWorldEvent o) {
            return null;
        }

        @Override
        public Entity getAttacker(EntityJoinWorldEvent o) {
            return null;
        }

        @Override
        public EntityPlayer getPlayer(EntityJoinWorldEvent o) {
            return getClosestPlayer(o.getWorld(), o.getEntity().getPosition());
        }

        @Override
        public ItemStack getItem(EntityJoinWorldEvent o) {
            return ItemStack.EMPTY;
        }
    };
    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();

    private static EntityPlayer getClosestPlayer(World world, BlockPos pos) {
        return world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 100, false);
    }

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
                .attribute(Attribute.create(CANSPAWNHERE))
                .attribute(Attribute.create(NOTCOLLIDING))
                .attribute(Attribute.create(SPAWNER))
                .attribute(Attribute.create(INBUILDING))
                .attribute(Attribute.create(INCITY))
                .attribute(Attribute.create(INSTREET))
                .attribute(Attribute.create(INSPHERE))
                .attribute(Attribute.create(GAMESTAGE))
                .attribute(Attribute.create(PASSIVE))
                .attribute(Attribute.create(HOSTILE))
                .attribute(Attribute.create(SEESKY))
                .attribute(Attribute.create(WEATHER))
                .attribute(Attribute.create(TEMPCATEGORY))
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
    private Event.Result result = null;

    private SpawnRule(AttributeMap map, boolean onJoin) {
        super(InControl.setup.getLogger());
        this.onJoin = onJoin;
        ruleEvaluator = new GenericRuleEvaluator(map);
        addActions(map, new ModRuleCompatibilityLayer());
    }

    public static SpawnRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = FACTORY.parse(element);
            boolean onJoin = element.getAsJsonObject().has("onjoin") && element.getAsJsonObject().get("onjoin").getAsBoolean();
            return new SpawnRule(map, onJoin);
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
            this.result = null;
        }
    }

    public boolean match(LivingSpawnEvent.CheckSpawn event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public boolean match(EntityJoinWorldEvent event) {
        return ruleEvaluator.match(event, EVENT_QUERY_JOIN);
    }

    public void action(LivingSpawnEvent.CheckSpawn event) {
        EventGetter getter = new EventGetter() {
            @Override
            public EntityLivingBase getEntityLiving() {
                return event.getEntityLiving();
            }

            @Override
            public EntityPlayer getPlayer() {
                return null;
            }

            @Override
            public World getWorld() {
                return event.getWorld();
            }

            @Override
            public BlockPos getPosition() {
                return event.getEntityLiving().getPosition();
            }
        };
        for (Consumer<EventGetter> action : actions) {
            action.accept(getter);
        }
    }

    public void action(EntityJoinWorldEvent event) {
        EventGetter getter = new EventGetter() {
            @Override
            public EntityLivingBase getEntityLiving() {
                return event.getEntity() instanceof EntityLivingBase ? (EntityLivingBase) event.getEntity() : null;
            }

            @Override
            public EntityPlayer getPlayer() {
                return null;
            }

            @Override
            public World getWorld() {
                return event.getWorld();
            }

            @Override
            public BlockPos getPosition() {
                return event.getEntity() != null ? event.getEntity().getPosition() : null;
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

    public boolean isOnJoin() {
        return onJoin;
    }
}
