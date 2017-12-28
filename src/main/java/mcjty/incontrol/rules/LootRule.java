package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import mcjty.incontrol.InControl;
import mcjty.incontrol.rules.support.GenericRuleEvaluator;
import mcjty.incontrol.rules.support.IEventQuery;
import mcjty.incontrol.typed.Attribute;
import mcjty.incontrol.typed.AttributeMap;
import mcjty.incontrol.typed.GenericAttributeMapFactory;
import mcjty.incontrol.varia.Tools;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static mcjty.incontrol.rules.support.RuleKeys.*;

public class LootRule {

    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    public static final IEventQuery<LivingDropsEvent> EVENT_QUERY = new IEventQuery<LivingDropsEvent>() {
        @Override
        public World getWorld(LivingDropsEvent o) {
            return o.getEntity().getEntityWorld();
        }

        @Override
        public BlockPos getPos(LivingDropsEvent o) {
            LivingDropsEvent s = o;
            return s.getEntity().getPosition();
        }

        @Override
        public int getY(LivingDropsEvent o) {
            return o.getEntity().getPosition().getY();
        }

        @Override
        public Entity getEntity(LivingDropsEvent o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(LivingDropsEvent o) {
            return o.getSource();
        }
    };

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
                .attribute(Attribute.create(PROJECTILE))
                .attribute(Attribute.create(EXPLOSION))
                .attribute(Attribute.create(FIRE))
                .attribute(Attribute.create(MAGIC))
                .attribute(Attribute.createMulti(MOB))
                .attribute(Attribute.createMulti(MOD))
                .attribute(Attribute.createMulti(BLOCK))
                .attribute(Attribute.createMulti(BIOME))
                .attribute(Attribute.createMulti(DIMENSION))
                .attribute(Attribute.createMulti(SOURCE))
                .attribute(Attribute.createMulti(HELDITEM))

                .attribute(Attribute.createMulti(ACTION_ITEM))
                .attribute(Attribute.create(ACTION_LOOTTABLE))
                .attribute(Attribute.createMulti(ACTION_REMOVE))
                .attribute(Attribute.create(ACTION_REMOVEALL))
        ;
    }

    private final GenericRuleEvaluator ruleEvaluator;
    private List<ItemStack> toRemoveItems = new ArrayList<>();
    private List<ItemStack> toAddItems = new ArrayList<>();
    private boolean removeAll = false;
    private String lootTable;
    
    private LootRule(AttributeMap map) {
        ruleEvaluator = new GenericRuleEvaluator(map);
        addActions(map);
    }

    private void addActions(AttributeMap map) {
        if (map.has(ACTION_ITEM)) {
            addItem(map);
        }
        if (map.has(ACTION_LOOTTABLE)) {
            lootTable = map.get(ACTION_LOOTTABLE);
        }
        if (map.has(ACTION_REMOVE)) {
            removeItem(map);
        }
        if (map.has(ACTION_REMOVEALL)) {
            removeAll = map.get(ACTION_REMOVEALL);
        }
    }

    public List<ItemStack> getToRemoveItems() {
        return toRemoveItems;
    }

    public boolean isRemoveAll() {
        return removeAll;
    }

    public List<ItemStack> getToAddItems() {
        return toAddItems;
    }
    
    public String getLootTable() {
        return lootTable;
    }
    
    private List<ItemStack> getItems(List<String> itemNames) {
        List<ItemStack> items = new ArrayList<>();
        for (String name : itemNames) {
            ItemStack stack = Tools.parseStack(name);
            if (stack.isEmpty()) {
                InControl.logger.log(Level.ERROR, "Unknown item '" + name + "'!");
            } else {
                items.add(stack);
            }
        }
        return items;
    }

    private void addItem(AttributeMap map) {
        toAddItems.addAll(getItems(map.getList(ACTION_ITEM)));
    }

    private void removeItem(AttributeMap map) {
        toRemoveItems.addAll(getItems(map.getList(ACTION_REMOVE)));
    }

    private static Random rnd = new Random();

    public boolean match(LivingDropsEvent event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public static LootRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = FACTORY.parse(element);
            return new LootRule(map);
        }
    }}
