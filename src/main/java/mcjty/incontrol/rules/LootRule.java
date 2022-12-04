package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import mcjty.incontrol.compat.ModRuleCompatibilityLayer;
import mcjty.incontrol.data.PhaseTools;
import mcjty.incontrol.rules.support.GenericRuleEvaluator;
import mcjty.incontrol.tools.rules.CommonRuleEvaluator;
import mcjty.incontrol.tools.rules.IEventQuery;
import mcjty.incontrol.tools.rules.IModRuleCompatibilityLayer;
import mcjty.incontrol.tools.rules.RuleBase;
import mcjty.incontrol.tools.typed.Attribute;
import mcjty.incontrol.tools.typed.AttributeMap;
import mcjty.incontrol.tools.typed.GenericAttributeMapFactory;
import mcjty.incontrol.tools.varia.Tools;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static mcjty.incontrol.rules.support.RuleKeys.*;

public class LootRule extends RuleBase<RuleBase.EventGetter> {

    public static final IEventQuery<LivingDropsEvent> EVENT_QUERY = new IEventQuery<>() {
        @Override
        public Level getWorld(LivingDropsEvent o) {
            return o.getEntity().getCommandSenderWorld();
        }

        @Override
        public BlockPos getPos(LivingDropsEvent o) {
            return o.getEntity().blockPosition();
        }

        @Override
        public BlockPos getValidBlockPos(LivingDropsEvent o) {
            return o.getEntity().blockPosition().below();
        }

        @Override
        public int getY(LivingDropsEvent o) {
            return o.getEntity().blockPosition().getY();
        }

        @Override
        public Entity getEntity(LivingDropsEvent o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(LivingDropsEvent o) {
            return o.getSource();
        }

        @Override
        public Entity getAttacker(LivingDropsEvent o) {
            return o.getSource().getEntity();
        }

        @Override
        public Player getPlayer(LivingDropsEvent o) {
            Entity entity = o.getSource().getEntity();
            return entity instanceof Player ? (Player) entity : null;
        }

        @Override
        public ItemStack getItem(LivingDropsEvent o) {
            return ItemStack.EMPTY;
        }
    };
    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    private static final Random rnd = new Random();

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
                .attribute(Attribute.create(GAMESTAGE))
                .attribute(Attribute.create(WINTER))
                .attribute(Attribute.create(SUMMER))
                .attribute(Attribute.create(SPRING))
                .attribute(Attribute.create(AUTUMN))
                .attribute(Attribute.create(INSTREET))
                .attribute(Attribute.create(INSPHERE))
                .attribute(Attribute.create(PASSIVE))
                .attribute(Attribute.create(HOSTILE))
                .attribute(Attribute.create(BABY))
                .attribute(Attribute.create(SEESKY))
                .attribute(Attribute.create(SLIME))
                .attribute(Attribute.create(WEATHER))
                .attribute(Attribute.createMulti(BIOMETAGS))
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
                .attribute(Attribute.create(BLOCKOFFSET))
                .attribute(Attribute.createMulti(BIOME))
                .attribute(Attribute.createMulti(BIOMETYPE))
                .attribute(Attribute.createMulti(DIMENSION))
                .attribute(Attribute.createMulti(DIMENSION_MOD))
                .attribute(Attribute.createMulti(SOURCE))
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

                .attribute(Attribute.create(STATE))
                .attribute(Attribute.create(PSTATE))

                .attribute(Attribute.createMulti(HELDITEM))
                .attribute(Attribute.createMulti(PLAYER_HELDITEM))
                .attribute(Attribute.createMulti(OFFHANDITEM))
                .attribute(Attribute.createMulti(BOTHHANDSITEM))

                .attribute(Attribute.create(ACTION_ITEMNBT))
                .attribute(Attribute.create(ACTION_ITEMCOUNT))
                .attribute(Attribute.createMulti(ACTION_ITEM))
                .attribute(Attribute.createMulti(ACTION_REMOVE))
                .attribute(Attribute.create(ACTION_REMOVEALL))
        ;
    }

    private final GenericRuleEvaluator ruleEvaluator;
    private final Set<String> phases;
    private final List<Predicate<ItemStack>> toRemoveItems = new ArrayList<>();
    private final List<Pair<ItemStack, Function<Integer, Integer>>> toAddItems = new ArrayList<>();
    private boolean removeAll = false;

    private LootRule(AttributeMap map, Set<String> phases) {
        super(InControl.setup.getLogger());
        this.phases = phases;
        ruleEvaluator = new GenericRuleEvaluator(map);
        addActions(map, new ModRuleCompatibilityLayer());
    }

    public Set<String> getPhases() {
        return phases;
    }

    public static LootRule parse(JsonElement element) {
        if (element == null) {
            return null;
        } else {
            AttributeMap map = FACTORY.parse(element, "loot.json");
            return new LootRule(map, PhaseTools.getPhases(element));
        }
    }

    @Override
    protected void addActions(AttributeMap map, IModRuleCompatibilityLayer layer) {
        super.addActions(map, layer);

        map.consumeAsList(ACTION_ITEM, itemList -> addItem(map, itemList));
        map.consumeAsList(ACTION_REMOVE, this::removeItem);
        map.consume(ACTION_REMOVEALL, v -> removeAll = v);

        if (!map.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            map.getKeys().forEach(k -> buffer.append(k).append(' '));
            ErrorHandler.error("Invalid keywords in loot rule: " + buffer);
        }
    }

    public List<Predicate<ItemStack>> getToRemoveItems() {
        return toRemoveItems;
    }

    public boolean isRemoveAll() {
        return removeAll;
    }

    public List<Pair<ItemStack, Function<Integer, Integer>>> getToAddItems() {
        return toAddItems;
    }

    private Function<Integer, Integer> getCountFunction(@Nullable String itemcount) {
        if (itemcount == null) {
            return looting -> 1;
        }
        String[] loottable = StringUtils.split(itemcount, '/');
        int[] min = new int[loottable.length];
        int[] max = new int[loottable.length];
        for (int i = 0; i < loottable.length; i++) {
            String[] minmax = StringUtils.split(loottable[i], '-');
            if (minmax.length == 1) {
                try {
                    min[i] = max[i] = Integer.parseInt(minmax[0]);
                } catch (NumberFormatException e) {
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Bad amount specified in loot rule: " + minmax);
                    min[i] = max[i] = 1;
                }
            } else if (minmax.length == 2) {
                try {
                    min[i] = Integer.parseInt(minmax[0]);
                    max[i] = Integer.parseInt(minmax[1]);
                } catch (NumberFormatException e) {
                    InControl.setup.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Bad amounts specified in loot rule: " + minmax);
                    min[i] = max[i] = 1;
                }
            } else {
                InControl.setup.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Bad amount range specified in loot rule: " + minmax);
                min[i] = max[i] = 1;
            }
        }

        if (loottable.length == 1) {
            // Easy case
            if (min[0] == max[0]) {
                return looting -> min[0];
            } else {
                return looting -> rnd.nextInt(max[0] - min[0] + 1) + min[0];
            }
        } else {
            return looting -> {
                if (looting >= min.length) {
                    return rnd.nextInt(max[min.length - 1] - min[min.length - 1] + 1) + min[min.length - 1];
                } else if (looting >= 0) {
                    return rnd.nextInt(max[looting] - min[looting] + 1) + min[looting];
                } else {
                    return rnd.nextInt(max[0] - min[0] + 1) + min[0];
                }
            };
        }
    }

    private List<Pair<ItemStack, Function<Integer, Integer>>> getItems(List<String> itemNames, @Nullable String nbtJson,
                                                                       @Nullable String itemcount) {
        Function<Integer, Integer> countFunction = getCountFunction(itemcount);

        List<Pair<ItemStack, Function<Integer, Integer>>> items = new ArrayList<>();
        for (String name : itemNames) {
            ItemStack stack = Tools.parseStack(name, InControl.setup.getLogger());
            if (stack.isEmpty()) {
                InControl.setup.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Unknown item '" + name + "'!");
            } else {
                if (nbtJson != null) {
                    try {
                        stack.setTag(TagParser.parseTag(nbtJson));
                    } catch (CommandSyntaxException e) {
                        InControl.setup.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Bad nbt for '" + name + "'!");
                    }
                }
                items.add(Pair.of(stack, countFunction));
            }
        }
        return items;
    }

    private void addItem(AttributeMap map, List<String> itemList) {
        String nbt = map.consumeAndFetch(ACTION_ITEMNBT);
        String itemcount = map.consumeAndFetch(ACTION_ITEMCOUNT);
        toAddItems.addAll(getItems(itemList, nbt, itemcount));
    }

    private void removeItem(List<String> itemList) {
        toRemoveItems.addAll(CommonRuleEvaluator.getItems(itemList, logger));
    }

    public boolean match(LivingDropsEvent event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }
}
