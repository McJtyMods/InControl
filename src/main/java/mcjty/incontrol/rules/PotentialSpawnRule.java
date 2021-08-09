package mcjty.incontrol.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.InControl;
import mcjty.incontrol.rules.support.GenericRuleEvaluator;
import mcjty.tools.rules.IEventQuery;
import mcjty.tools.rules.RuleBase;
import mcjty.tools.typed.Attribute;
import mcjty.tools.typed.AttributeMap;
import mcjty.tools.typed.GenericAttributeMapFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static mcjty.incontrol.rules.support.RuleKeys.*;

public class PotentialSpawnRule extends RuleBase<RuleBase.EventGetter> {

    public static final IEventQuery<WorldEvent.PotentialSpawns> EVENT_QUERY = new IEventQuery<WorldEvent.PotentialSpawns>() {
        @Override
        public IWorld getWorld(WorldEvent.PotentialSpawns o) {
            return o.getWorld();
        }

        @Override
        public BlockPos getPos(WorldEvent.PotentialSpawns o) {
            return o.getPos();
        }

        @Override
        public BlockPos getValidBlockPos(WorldEvent.PotentialSpawns o) {
            return o.getPos().below();
        }

        @Override
        public int getY(WorldEvent.PotentialSpawns o) {
            return o.getPos().getY();
        }

        @Override
        public Entity getEntity(WorldEvent.PotentialSpawns o) {
            return null;
        }

        @Override
        public DamageSource getSource(WorldEvent.PotentialSpawns o) {
            return null;
        }

        @Override
        public Entity getAttacker(WorldEvent.PotentialSpawns o) {
            return null;
        }

        @Override
        public PlayerEntity getPlayer(WorldEvent.PotentialSpawns o) {
            return null;
        }

        @Override
        public ItemStack getItem(WorldEvent.PotentialSpawns o) {
            return ItemStack.EMPTY;
        }
    };
    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    private static final GenericAttributeMapFactory MOB_FACTORY = new GenericAttributeMapFactory();

    static {
        FACTORY
                .attribute(Attribute.create(MINCOUNT))
                .attribute(Attribute.create(MAXCOUNT))
                .attribute(Attribute.create(MINDAYCOUNT))
                .attribute(Attribute.create(MAXDAYCOUNT))
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
                .attribute(Attribute.createMulti(CATEGORY))
                .attribute(Attribute.create(DIFFICULTY))
                .attribute(Attribute.create(STRUCTURE))
                .attribute(Attribute.create(WINTER))
                .attribute(Attribute.create(SUMMER))
                .attribute(Attribute.create(SPRING))
                .attribute(Attribute.create(AUTUMN))
                .attribute(Attribute.create(STATE))
                .attribute(Attribute.createMulti(BLOCK))
                .attribute(Attribute.create(BLOCKOFFSET))
                .attribute(Attribute.createMulti(BIOME))
                .attribute(Attribute.createMulti(BIOMETYPE))
                .attribute(Attribute.createMulti(DIMENSION))
                .attribute(Attribute.createMulti(DIMENSION_MOD))

                .attribute(Attribute.createMulti(ACTION_REMOVE_MOBS))
        ;

        MOB_FACTORY
                .attribute(Attribute.create(MOB_NAME))
                .attribute(Attribute.create(MOB_WEIGHT))
                .attribute(Attribute.create(MOB_GROUPCOUNTMIN))
                .attribute(Attribute.create(MOB_GROUPCOUNTMAX))
        ;
    }

    private final GenericRuleEvaluator ruleEvaluator;
    private List<MobSpawnInfo.Spawners> spawnEntries = new ArrayList<>();
    private Set<EntityType> toRemoveMobs = new HashSet<>();

    private PotentialSpawnRule(AttributeMap map) {
        super(InControl.setup.getLogger());

        ruleEvaluator = new GenericRuleEvaluator(map);

        if ((!map.has(ACTION_MOBS)) && (!map.has(ACTION_REMOVE_MOBS))) {
            InControl.setup.getLogger().log(Level.ERROR, "No 'mobs' or 'remove' specified!");
            return;
        }
        makeSpawnEntries(map);
        if (map.has(ACTION_REMOVE_MOBS)) {
            addToRemoveAction(map);
        }
    }

    public static PotentialSpawnRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            JsonObject jsonObject = element.getAsJsonObject();
            if ((!jsonObject.has("mobs")) && (!jsonObject.has("remove"))) {
                return null;
            }

            AttributeMap map = FACTORY.parse(element);

            if (jsonObject.has("mobs")) {
                JsonArray mobs = jsonObject.get("mobs").getAsJsonArray();
                for (JsonElement mob : mobs) {
                    AttributeMap mobMap = MOB_FACTORY.parse(mob);
                    map.addList(ACTION_MOBS, mobMap);
                }
            }
            return new PotentialSpawnRule(map);
        }
    }

    private void addToRemoveAction(AttributeMap map) {
        List<String> toremove = map.getList(ACTION_REMOVE_MOBS);
        for (String s : toremove) {
            EntityType type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(s));
            if (type == null) {
                InControl.setup.getLogger().log(Level.ERROR, "Cannot find mob '" + s + "'!");
                return;
            }
            toRemoveMobs.add(type);
        }
    }

    private void makeSpawnEntries(AttributeMap map) {
        for (AttributeMap mobMap : map.getList(ACTION_MOBS)) {
            String id = mobMap.get(MOB_NAME);
            EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
            if (type == null) {
                InControl.setup.getLogger().log(Level.ERROR, "Cannot find mob '" + mobMap.get(MOB_NAME) + "'!");
                return;
            }

            Integer weight = mobMap.get(MOB_WEIGHT);
            if (weight == null) {
                weight = 1;
            }
            Integer groupCountMin = mobMap.get(MOB_GROUPCOUNTMIN);
            if (groupCountMin == null) {
                groupCountMin = 1;
            }
            Integer groupCountMax = mobMap.get(MOB_GROUPCOUNTMAX);
            if (groupCountMax == null) {
                groupCountMax = Math.max(groupCountMin, 1);
            }
            MobSpawnInfo.Spawners entry = new MobSpawnInfo.Spawners(type, weight, groupCountMin, groupCountMax);
            spawnEntries.add(entry);
        }
    }

    public List<MobSpawnInfo.Spawners> getSpawnEntries() {
        return spawnEntries;
    }

    public boolean match(WorldEvent.PotentialSpawns event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public Set<EntityType> getToRemoveMobs() {
        return toRemoveMobs;
    }
}

