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
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static mcjty.incontrol.rules.support.RuleKeys.*;

public class ExperienceRule {

    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    public static final IEventQuery<LivingExperienceDropEvent> EVENT_QUERY = new IEventQuery<LivingExperienceDropEvent>() {
        @Override
        public World getWorld(LivingExperienceDropEvent o) {
            return o.getEntity().getEntityWorld();
        }

        @Override
        public BlockPos getPos(LivingExperienceDropEvent o) {
            return o.getEntity().getPosition();
        }

        @Override
        public int getY(LivingExperienceDropEvent o) {
            return o.getEntity().getPosition().getY();
        }

        @Override
        public Entity getEntity(LivingExperienceDropEvent o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(LivingExperienceDropEvent o) {
            return null;
        }

        @Override
        public Entity getAttacker(LivingExperienceDropEvent o) {
            return o.getAttackingPlayer();
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
                .attribute(Attribute.create(INSPHERE))
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
                .attribute(Attribute.createMulti(MOB))
                .attribute(Attribute.createMulti(MOD))
                .attribute(Attribute.createMulti(BLOCK))
                .attribute(Attribute.createMulti(BIOME))
                .attribute(Attribute.createMulti(DIMENSION))
                .attribute(Attribute.createMulti(HELDITEM))

                .attribute(Attribute.create(ACTION_RESULT))
                .attribute(Attribute.createMulti(ACTION_REMOVE))
                .attribute(Attribute.create(ACTION_REMOVEALL))
        ;
    }

    private final GenericRuleEvaluator ruleEvaluator;
    private List<ItemStack> toRemoveItems = new ArrayList<>();
    private List<ItemStack> toAddItems = new ArrayList<>();
    private boolean removeAll = false;

    private ExperienceRule(AttributeMap map) {
        ruleEvaluator = new GenericRuleEvaluator(map);
        addActions(map);
    }

    private void addActions(AttributeMap map) {
        if (map.has(ACTION_ITEM)) {
            addItem(map);
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

    private List<ItemStack> getItems(List<String> itemNames, @Nullable String nbtJson) {
        List<ItemStack> items = new ArrayList<>();
        for (String name : itemNames) {
            ItemStack stack = Tools.parseStack(name);
            if (stack.isEmpty()) {
                InControl.logger.log(Level.ERROR, "Unknown item '" + name + "'!");
            } else {
                if (nbtJson != null) {
                    try {
                        stack.setTagCompound(JsonToNBT.getTagFromJson(nbtJson));
                    } catch (NBTException e) {
                        InControl.logger.log(Level.ERROR, "Bad nbt for '" + name + "'!");
                    }
                }
                items.add(stack);
            }
        }
        return items;
    }

    private void addItem(AttributeMap map) {
        String nbt = map.get(ACTION_ITEMNBT);
        toAddItems.addAll(getItems(map.getList(ACTION_ITEM), nbt));
    }

    private void removeItem(AttributeMap map) {
        toRemoveItems.addAll(getItems(map.getList(ACTION_REMOVE), null));
    }

    private static Random rnd = new Random();

    public boolean match(LivingDropsEvent event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public static ExperienceRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = FACTORY.parse(element);
            return new ExperienceRule(map);
        }
    }}
