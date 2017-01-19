package mcjty.incontrol.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.InControl;
import mcjty.incontrol.typed.Attribute;
import mcjty.incontrol.typed.AttributeMap;
import mcjty.incontrol.typed.GenericAttributeMapFactory;
import mcjty.lib.tools.EntityTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;

import static mcjty.incontrol.rules.RuleKeys.*;

public class PotentialSpawnRule {

    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    private static final GenericAttributeMapFactory MOB_FACTORY = new GenericAttributeMapFactory();
    public static final IEventQuery EVENT_QUERY = new IEventQuery() {
        @Override
        public World getWorld(Object o) {
            return ((WorldEvent.PotentialSpawns) o).getWorld();
        }

        @Override
        public BlockPos getPos(Object o) {
            return ((WorldEvent.PotentialSpawns) o).getPos();
        }

        @Override
        public int getY(Object o) {
            return ((WorldEvent.PotentialSpawns) o).getPos().getY();
        }

        @Override
        public Entity getEntity(Object o) {
            return null;
        }
    };

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
                .attribute(Attribute.createMulti(BLOCK))
                .attribute(Attribute.createMulti(BIOME))
                .attribute(Attribute.createMulti(DIMENSION))

                .attribute(Attribute.createMulti(REMOVE))
        ;

        MOB_FACTORY
                .attribute(Attribute.create(MOB))
                .attribute(Attribute.create(WEIGHT))
                .attribute(Attribute.create(GROUPCOUNTMIN))
                .attribute(Attribute.create(GROUPCOUNTMAX))
        ;
    }

    private final GenericRuleEvaluator ruleEvaluator;
    private List<Biome.SpawnListEntry> spawnEntries = new ArrayList<>();
    private List<Class> toRemoveMobs = new ArrayList<>();

    private PotentialSpawnRule(AttributeMap map) {
        ruleEvaluator = new GenericRuleEvaluator(map);

        if ((!map.has(MOBS)) && (!map.has(REMOVE))) {
            InControl.logger.log(Level.ERROR, "No 'mobs' or 'remove' specified!");
            return;
        }
        makeSpawnEntries(map);
        if (map.has(REMOVE)) {
            addToRemoveAction(map);
        }
    }

    private void addToRemoveAction(AttributeMap map) {
        List<String> toremove = map.getList(REMOVE);
        for (String s : toremove) {
            String id = EntityTools.fixEntityId(s);
            Class<? extends Entity> clazz = EntityTools.findClassById(id);
            if (clazz == null) {
                InControl.logger.log(Level.ERROR, "Cannot find mob '" + s + "'!");
                return;
            }
            toRemoveMobs.add(clazz);
        }
    }

    private void makeSpawnEntries(AttributeMap map) {
        for (AttributeMap mobMap : map.getList(MOBS)) {
            String id = EntityTools.fixEntityId(mobMap.get(MOB));
            Class<? extends Entity> clazz = EntityTools.findClassById(id);
            if (clazz == null) {
                InControl.logger.log(Level.ERROR, "Cannot find mob '" + mobMap.get(MOB) + "'!");
                return;
            }

            Integer weight = mobMap.get(WEIGHT);
            if (weight == null) {
                weight = 1;
            }
            Integer groupCountMin = mobMap.get(GROUPCOUNTMIN);
            if (groupCountMin == null) {
                groupCountMin = 1;
            }
            Integer groupCountMax = mobMap.get(GROUPCOUNTMAX);
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
                    map.addList(MOBS, mobMap);
                }
            }
            return new PotentialSpawnRule(map);
        }
    }
}

