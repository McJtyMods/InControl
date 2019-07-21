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
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.fixes.EntityId;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;

import static mcjty.incontrol.rules.support.RuleKeys.*;

public class PotentialSpawnRule extends RuleBase<RuleBase.EventGetter> {

    public static final IEventQuery<WorldEvent.PotentialSpawns> EVENT_QUERY = new IEventQuery<WorldEvent.PotentialSpawns>() {
        @Override
        public World getWorld(WorldEvent.PotentialSpawns o) {
            return o.getWorld();
        }

        @Override
        public BlockPos getPos(WorldEvent.PotentialSpawns o) {
            return o.getPos();
        }

        @Override
        public BlockPos getValidBlockPos(WorldEvent.PotentialSpawns o) {
            return o.getPos().down();
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
        public EntityPlayer getPlayer(WorldEvent.PotentialSpawns o) {
            return null;
        }

        @Override
        public ItemStack getItem(WorldEvent.PotentialSpawns o) {
            return ItemStack.EMPTY;
        }
    };
    public static final EntityId FIXER = new EntityId();
    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    private static final GenericAttributeMapFactory MOB_FACTORY = new GenericAttributeMapFactory();

    static {
        FACTORY
                .attribute(Attribute.create(MINCOUNT))
                .attribute(Attribute.create(MAXCOUNT))
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
                .attribute(Attribute.create(TEMPCATEGORY))
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
    private List<Biome.SpawnListEntry> spawnEntries = new ArrayList<>();
    private List<Class> toRemoveMobs = new ArrayList<>();

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

    public static String fixEntityId(String id) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("id", id);
        nbt = FIXER.fixTagCompound(nbt);
        return nbt.getString("id");
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
            String id = fixEntityId(s);
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
            Class<? extends Entity> clazz = entry == null ? null : entry.getEntityClass();
            if (clazz == null) {
                InControl.setup.getLogger().log(Level.ERROR, "Cannot find mob '" + s + "'!");
                return;
            }
            toRemoveMobs.add(clazz);
        }
    }

    private void makeSpawnEntries(AttributeMap map) {
        for (AttributeMap mobMap : map.getList(ACTION_MOBS)) {
            String id = fixEntityId(mobMap.get(MOB_NAME));
            EntityEntry ee = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
            Class<? extends Entity> clazz = ee == null ? null : ee.getEntityClass();
            if (clazz == null) {
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
            Biome.SpawnListEntry entry = new Biome.SpawnListEntry((Class<? extends EntityLiving>) clazz,
                    weight, groupCountMin, groupCountMax);
            spawnEntries.add(entry);
        }
    }

    public List<Biome.SpawnListEntry> getSpawnEntries() {
        return spawnEntries;
    }

    public boolean match(WorldEvent.PotentialSpawns event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public List<Class> getToRemoveMobs() {
        return toRemoveMobs;
    }
}

