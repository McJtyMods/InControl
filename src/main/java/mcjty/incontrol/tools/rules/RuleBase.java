package mcjty.incontrol.tools.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.tools.typed.AttributeMap;
import mcjty.incontrol.tools.varia.LookAtTools;
import mcjty.incontrol.tools.varia.Tools;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static mcjty.incontrol.tools.rules.CommonRuleKeys.*;

public class RuleBase<T extends RuleBase.EventGetter> {

    protected final Logger logger;
    protected final List<Consumer<T>> actions = new ArrayList<>();

    public RuleBase(Logger logger) {
        this.logger = logger;
    }

    private static final Random rnd = new Random();

    protected List<Pair<Float, ItemStack>> getItemsWeighted(List<String> itemNames) {
        List<Pair<Float, ItemStack>> items = new ArrayList<>();
        for (String json : itemNames) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(json);
            if (element.isJsonPrimitive()) {
                String name = element.getAsString();
                Pair<Float, ItemStack> pair = Tools.parseStackWithFactor(name, logger);
                if (pair.getValue().isEmpty()) {
                    ErrorHandler.error("Unknown item '" + name + "'!");
                } else {
                    items.add(pair);
                }
            } else if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                Pair<Float, ItemStack> pair = Tools.parseStackWithFactor(obj, logger);
                if (pair != null) {
                    items.add(pair);
                }
            } else {
                ErrorHandler.error("Item description '" + json + "' is not valid!");
            }
        }
        return items;
    }

    protected ItemStack getRandomItem(List<Pair<Float, ItemStack>> items, float total) {
        float r = rnd.nextFloat() * total;
        for (Pair<Float, ItemStack> pair : items) {
            if (r <= pair.getLeft()) {
                return pair.getRight().copy();
            }
            r -= pair.getLeft();
        }
        return ItemStack.EMPTY;
    }

    protected float getTotal(List<Pair<Float, ItemStack>> items) {
        float total = 0.0f;
        for (Pair<Float, ItemStack> pair : items) {
            total += pair.getLeft();
        }
        return total;
    }

    public interface EventGetter {
        LivingEntity getEntityLiving();

        Player getPlayer();

        LevelAccessor getWorld();

        BlockPos getPosition();
    }

    protected void addActions(AttributeMap map, IModRuleCompatibilityLayer layer) {
        map.consume(ACTION_COMMAND, this::addCommandAction);
        map.consume(ACTION_ADDSTAGE, stage -> addAddStage(stage, layer));
        map.consume(ACTION_REMOVESTAGE, stage -> addRemoveStage(stage, layer));
        map.consume(ACTION_HEALTHSET, this::addHealthSetAction);
        map.consume2(ACTION_HEALTHMULTIPLY, ACTION_HEALTHADD, this::addHealthAction);
        map.consume(ACTION_SPEEDSET, this::addSpeedSetAction);
        map.consume2(ACTION_SPEEDMULTIPLY, ACTION_SPEEDADD, this::addSpeedAction);
        map.consume(ACTION_DAMAGESET, this::addDamageSetAction);
        map.consume2(ACTION_DAMAGEMULTIPLY, ACTION_DAMAGEADD, this::addDamageAction);
        map.consume2(ACTION_SIZEMULTIPLY, ACTION_SIZEADD, this::addSizeActions);
        map.consumeAsList(ACTION_POTION, this::addPotionsAction);
        map.consume(ACTION_ANGRY, this::addAngryAction);
        map.consume(ACTION_CUSTOMNAME, this::addCustomName);
        map.consume(ACTION_MOBNBT, this::addMobNBT);
        map.consumeAsList(ACTION_HELDITEM, this::addHeldItem);
        map.consumeAsList(ACTION_ARMORBOOTS, items -> addArmorItem(items, EquipmentSlot.FEET));
        map.consumeAsList(ACTION_ARMORLEGS, items -> addArmorItem(items, EquipmentSlot.LEGS));
        map.consumeAsList(ACTION_ARMORHELMET, items -> addArmorItem(items, EquipmentSlot.HEAD));
        map.consumeAsList(ACTION_ARMORCHEST, items -> addArmorItem(items, EquipmentSlot.CHEST));
        map.consume(ACTION_FIRE, this::addFireAction);
        map.consume(ACTION_EXPLOSION, this::addExplosionAction);
        map.consume(ACTION_CLEAR, this::addClearAction);
        map.consume(ACTION_DAMAGE, this::addDoDamageAction);
        map.consume(ACTION_MESSAGE, this::addDoMessageAction);
        map.consumeAsList(ACTION_GIVE, this::addGiveAction);
        map.consumeAsList(ACTION_DROP, this::addDropAction);
        map.consume2(ACTION_SETBLOCK, BLOCKOFFSET, this::addSetBlockAction);
        map.consume(ACTION_SETHELDITEM, this::addSetHeldItemAction);
        map.consume(ACTION_SETHELDAMOUNT, this::addSetHeldAmountAction);
        map.consume(ACTION_SETSTATE, state -> {
            if (layer.hasEnigmaScript()) {
                addStateAction(state, layer);
            } else {
                logger.warn("EnigmaScript is missing: this action cannot work!");
            }
        });
        map.consume(ACTION_SETPSTATE, state -> {
            if (layer.hasEnigmaScript()) {
                addPStateAction(state, layer);
            } else {
                logger.warn("EnigmaScript is missing: this action cannot work!");
            }
        });
    }

    private static Map<String, DamageSource> damageMap = null;

    private static void addSource(DamageSource source) {
        damageMap.put(source.getMsgId(), source);
    }

    private void createDamageMap() {
        if (damageMap == null) {
            damageMap = new HashMap<>();
            addSource(DamageSource.IN_FIRE);
            addSource(DamageSource.LIGHTNING_BOLT);
            addSource(DamageSource.ON_FIRE);
            addSource(DamageSource.LAVA);
            addSource(DamageSource.HOT_FLOOR);
            addSource(DamageSource.IN_WALL);
            addSource(DamageSource.CRAMMING);
            addSource(DamageSource.DROWN);
            addSource(DamageSource.STARVE);
            addSource(DamageSource.CACTUS);
            addSource(DamageSource.FALL);
            addSource(DamageSource.FLY_INTO_WALL);
            addSource(DamageSource.OUT_OF_WORLD);
            addSource(DamageSource.GENERIC);
            addSource(DamageSource.MAGIC);
            addSource(DamageSource.WITHER);
            addSource(DamageSource.ANVIL);
            addSource(DamageSource.FALLING_BLOCK);
            addSource(DamageSource.DRAGON_BREATH);
//            addSource(DamageSource.FIREWORKS);    // @todo 1.16
        }
    }

    private static final TextComponent DEFAULT_NAME = new TextComponent("@");
    private static final CommandSource EMPTY = new CommandSource() {
        @Override
        public void sendMessage(Component pComponent, UUID pSenderUUID) {
        }

        @Override
        public boolean acceptsSuccess() {
            return false;
        }

        @Override
        public boolean acceptsFailure() {
            return false;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }
    };

    private void addCommandAction(String command) {
        actions.add(event -> {
            MinecraftServer server = event.getWorld().getServer();
            Player player = event.getPlayer();
            CommandSourceStack stack = new CommandSourceStack(EMPTY, Vec3.atCenterOf(event.getPosition()), Vec2.ZERO, (ServerLevel) event.getWorld(), 2,
                    DEFAULT_NAME.getString(), DEFAULT_NAME, server, player);
            server.getCommands().performCommand(stack, command);
        });
    }

    private void addAddStage(String stage, IModRuleCompatibilityLayer layer) {
        actions.add(event -> {
            Player player = event.getPlayer();
            if (player != null) {
                layer.addGameStage(player, stage);
            }
        });
    }

    private void addRemoveStage(String stage, IModRuleCompatibilityLayer layer) {
        actions.add(event -> {
            Player player = event.getPlayer();
            if (player != null) {
                layer.removeGameStage(player, stage);
            }
        });
    }

    private void addDoDamageAction(String damage) {
        createDamageMap();
        String[] split = StringUtils.split(damage, "=");
        DamageSource source = damageMap.get(split[0]);
        if (source == null) {
            ErrorHandler.error("Can't find damage source '" + split[0] + "'!");
            return;
        }
        float amount = 1.0f;
        if (split.length > 1) {
            amount = Float.parseFloat(split[1]);
        }

        float finalAmount = amount;
        actions.add(event -> {
            LivingEntity living = event.getEntityLiving();
            if (living != null) {
                living.hurt(source, finalAmount);
            }
        });
    }

    private void addDoMessageAction(String message) {
        actions.add(event -> {
            Player player = event.getPlayer();
            if (player == null) {
                player = event.getWorld().getNearestPlayer(event.getEntityLiving(), 100);
            }
            if (player != null) {
                player.displayClientMessage(new TextComponent(message), false);
            }
        });
    }


    private void addGiveAction(List<String> itemList) {
        final List<Pair<Float, ItemStack>> items = getItemsWeighted(itemList);
        if (items.isEmpty()) {
            return;
        }
        if (items.size() == 1) {
            ItemStack item = items.get(0).getRight();
            actions.add(event -> {
                Player player = event.getPlayer();
                if (player != null) {
                    if (!player.getInventory().add(item.copy())) {
                        player.spawnAtLocation(item.copy(), 1.05f);
                    }
                }
            });
        } else {
            final float total = getTotal(items);
            actions.add(event -> {
                Player player = event.getPlayer();
                if (player != null) {
                    ItemStack item = getRandomItem(items, total);
                    if (!player.getInventory().add(item.copy())) {
                        player.spawnAtLocation(item.copy(), 1.05f);
                    }
                }
            });
        }
    }

    private void addStateAction(String s, IModRuleCompatibilityLayer layer) {
        String[] split = StringUtils.split(s, '=');
        String state;
        String value;
        try {
            state = split[0];
            value = split[1];
        } catch (Exception e) {
            ErrorHandler.error("Bad state=value specifier '" + s + "'!");
            return;
        }
        String finalState = state;
        String finalValue = value;
        actions.add(event -> layer.setState(event.getWorld(), finalState, finalValue));
    }

    private void addPStateAction(String s, IModRuleCompatibilityLayer layer) {
        String[] split = StringUtils.split(s, '=');
        String state;
        String value;
        try {
            state = split[0];
            value = split[1];
        } catch (Exception e) {
            ErrorHandler.error("Bad state=value specifier '" + s + "'!");
            return;
        }
        String finalState = state;
        String finalValue = value;
        actions.add(event -> layer.setPlayerState(event.getPlayer(), finalState, finalValue));
    }

    @Nonnull
    private Function<EventGetter, BlockPos> parseOffset(String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        JsonObject obj = element.getAsJsonObject();

        int offsetX;
        int offsetY;
        int offsetZ;

        if (obj.has("offset")) {
            JsonObject offset = obj.getAsJsonObject("offset");
            offsetX = offset.has("x") ? offset.get("x").getAsInt() : 0;
            offsetY = offset.has("y") ? offset.get("y").getAsInt() : 0;
            offsetZ = offset.has("z") ? offset.get("z").getAsInt() : 0;
        } else {
            offsetX = 0;
            offsetY = 0;
            offsetZ = 0;
        }

        if (obj.has("look")) {
            return event -> {
                HitResult result = LookAtTools.getMovingObjectPositionFromPlayer(event.getWorld(), event.getPlayer(), false);
                if (result instanceof BlockHitResult) {
                    return ((BlockHitResult) result).getBlockPos().offset(offsetX, offsetY, offsetZ);
                } else {
                    return event.getPosition().offset(offsetX, offsetY, offsetZ);
                }
            };

        }
        return event -> event.getPosition().offset(offsetX, offsetY, offsetZ);
    }

    private void addSetHeldItemAction(String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        ItemStack stack;
        if (element.isJsonPrimitive()) {
            String name = element.getAsString();
            stack = Tools.parseStack(name, logger);
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            stack = Tools.parseStack(obj, logger);
            if (stack == null) {
                return;
            }
        } else {
            ErrorHandler.error("Item description '" + json + "' is not valid!");
            return;
        }
        actions.add(event -> event.getPlayer().setItemInHand(InteractionHand.MAIN_HAND, stack.copy()));
    }

    private void addSetHeldAmountAction(String amount) {
        int add = 0;
        int set = -1;
        if (amount.startsWith("+")) {
            add = Integer.parseInt(amount.substring(1));
        } else if (amount.startsWith("-")) {
            add = -Integer.parseInt(amount.substring(1));
        } else if (amount.startsWith("=")) {
            set = Integer.parseInt(amount.substring(1));
        } else {
            set = Integer.parseInt(amount);
        }

        int finalSet = set;
        if (finalSet >= 0) {
            actions.add(event -> {
                ItemStack item = event.getPlayer().getMainHandItem();
                item.setCount(finalSet);
                event.getPlayer().setItemInHand(InteractionHand.MAIN_HAND, item.copy());
            });
        } else {
            int finalAdd = add;
            actions.add(event -> {
                ItemStack item = event.getPlayer().getMainHandItem();
                int newCount = item.getCount() + finalAdd;
                if (newCount < 0) {
                    newCount = 0;
                } else if (newCount >= item.getMaxStackSize()) {
                    newCount = item.getMaxStackSize() - 1;
                }
                item.setCount(newCount);
                event.getPlayer().setItemInHand(InteractionHand.MAIN_HAND, item.copy());
            });
        }
    }

    private void addSetBlockAction(String json, String bo) {
        Function<EventGetter, BlockPos> posFunction;
        if (bo != null) {
            posFunction = parseOffset(bo);
        } else {
            posFunction = EventGetter::getPosition;
        }

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        if (element.isJsonPrimitive()) {
            String blockname = element.getAsString();
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockname));
            if (block == null) {
                ErrorHandler.error("Block '" + blockname + "' is not valid!");
                return;
            }
            BlockState state = block.defaultBlockState();
            actions.add(event -> {
                BlockPos pos = posFunction.apply(event);
                if (pos != null) {
                    event.getWorld().setBlock(pos, state, 3);
                }
            });
        } else {
            JsonObject obj = element.getAsJsonObject();
            if (!obj.has("block")) {
                ErrorHandler.error("Block is not valid!");
                return;
            }

            String blockname = obj.get("block").getAsString();
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockname));
            if (block == null) {
                ErrorHandler.error("Block '" + blockname + "' is not valid!");
                return;
            }
            BlockState state = block.defaultBlockState();
            if (obj.has("properties")) {
                JsonArray propArray = obj.get("properties").getAsJsonArray();
                for (JsonElement el : propArray) {
                    JsonObject propObj = el.getAsJsonObject();
                    String name = propObj.get("name").getAsString();
                    String value = propObj.get("value").getAsString();
                    for (Property<?> key : state.getProperties()) {
                        if (name.equals(key.getName())) {
                            state = CommonRuleEvaluator.set(state, key, value);
                        }
                    }
                }
            }
            BlockState finalState = state;
            actions.add(event -> {
                BlockPos pos = posFunction.apply(event);
                if (pos != null) {
                    event.getWorld().setBlock(pos, finalState, 3);
                }
            });
        }
    }

    private void addDropAction(List<String> itemList) {
        final List<Pair<Float, ItemStack>> items = getItemsWeighted(itemList);
        if (items.isEmpty()) {
            return;
        }
        if (items.size() == 1) {
            ItemStack item = items.get(0).getRight();
            actions.add(event -> {
                if (event.getWorld() instanceof Level) {
                    BlockPos pos = event.getPosition();
                    ItemEntity entityItem = new ItemEntity((Level) event.getWorld(), pos.getX(), pos.getY(), pos.getZ(), item.copy());
                    event.getWorld().addFreshEntity(entityItem);
                }
            });
        } else {
            final float total = getTotal(items);
            actions.add(event -> {
                if (event.getWorld() instanceof Level) {
                    BlockPos pos = event.getPosition();
                    ItemStack item = getRandomItem(items, total);
                    ItemEntity entityItem = new ItemEntity((Level) event.getWorld(), pos.getX(), pos.getY(), pos.getZ(), item.copy());
                    event.getWorld().addFreshEntity(entityItem);
                }
            });
        }
    }


    private void addClearAction(boolean clear) {
        if (clear) {
            actions.add(event -> {
                LivingEntity living = event.getEntityLiving();
                if (living != null) {
                    living.removeAllEffects();
                }
            });
        }
    }

    private void addFireAction(int fireAction) {
        actions.add(event -> {
            LivingEntity living = event.getEntityLiving();
            if (living != null) {
                living.hurt(DamageSource.ON_FIRE, 0.1f);
                living.setSecondsOnFire(fireAction);
            }
        });
    }

    private void addExplosionAction(String fireAction) {
        String[] split = StringUtils.split(fireAction, ",");
        float strength = 1.0f;
        boolean flaming = false;
        boolean smoking = false;
        try {
            strength = Float.parseFloat(split[0]);
            flaming = "1".equalsIgnoreCase(split[1]) || "true".equals(split[1].toLowerCase()) || "yes".equals(split[1].toLowerCase());
            smoking = "1".equalsIgnoreCase(split[2]) || "true".equals(split[2].toLowerCase()) || "yes".equals(split[2].toLowerCase());
        } catch (Exception ignore) {
        }

        float finalStrength = strength;
        boolean finalFlaming = flaming;
        boolean finalSmoking = smoking;
        actions.add(event -> {
            BlockPos pos = event.getPosition();
            if (pos != null) {
                // @todo 1.15 check if this is right and what to do about finalSmoking
//                event.getWorld().createExplosion(null, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, finalStrength, finalFlaming, finalSmoking);
                if (event.getWorld() instanceof Level) {
                    ((Level) event.getWorld()).explode(null, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, finalStrength, finalFlaming, Explosion.BlockInteraction.DESTROY);
                }
            }
        });
    }


    protected void addPotionsAction(List<String> potions) {
        List<MobEffectInstance> effects = new ArrayList<>();
        for (String p : potions) {
            String[] splitted = StringUtils.split(p, ',');
            if (splitted == null || splitted.length != 3) {
                ErrorHandler.error("Bad potion specifier '" + p + "'! Use <potion>,<duration>,<amplifier>");
                continue;
            }
            MobEffect potion = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(splitted[0]));
            if (potion == null) {
                ErrorHandler.error("Can't find potion '" + p + "'!");
                continue;
            }
            int duration = 0;
            int amplifier = 0;
            try {
                duration = Integer.parseInt(splitted[1]);
                amplifier = Integer.parseInt(splitted[2]);
            } catch (NumberFormatException e) {
                ErrorHandler.error("Bad duration or amplifier integer for '" + p + "'!");
                continue;
            }
            effects.add(new MobEffectInstance(potion, duration, amplifier));
        }
        if (!effects.isEmpty()) {
            actions.add(event -> {
                LivingEntity living = event.getEntityLiving();
                if (living != null) {
                    for (MobEffectInstance effect : effects) {
                        MobEffectInstance neweffect = new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier());
                        living.addEffect(neweffect);
                    }
                }
            });
        }
    }

    private void addHealthSetAction(float s) {
        actions.add(event -> {
            LivingEntity entityLiving = event.getEntityLiving();
            if (entityLiving != null) {
                if (!entityLiving.getTags().contains("ctrlHealth")) {
                    AttributeInstance entityAttribute = entityLiving.getAttribute(Attributes.MAX_HEALTH);
                    if (entityAttribute != null) {
                        entityAttribute.setBaseValue(s);
                        entityLiving.setHealth((float) (double) s);
                        entityLiving.addTag("ctrlHealth");
                    }
                }
            }
        });
    }

    private void addHealthAction(Float m, Float a) {
        float finalM = m == null ? 1 : m;
        float finalA = a == null ? 0 : a;
        actions.add(event -> {
            LivingEntity entityLiving = event.getEntityLiving();
            if (entityLiving != null) {
                if (!entityLiving.getTags().contains("ctrlHealth")) {
                    AttributeInstance entityAttribute = entityLiving.getAttribute(Attributes.MAX_HEALTH);
                    if (entityAttribute != null) {
                        double newMax = entityAttribute.getBaseValue() * finalM + finalA;
                        entityAttribute.setBaseValue(newMax);
                        entityLiving.setHealth((float) newMax);
                        entityLiving.addTag("ctrlHealth");
                    }
                }
            }
        });
    }

    private void addSpeedSetAction(float s) {
        actions.add(event -> {
            LivingEntity entityLiving = event.getEntityLiving();
            if (entityLiving != null) {
                if (!entityLiving.getTags().contains("ctrlSpeed")) {
                    AttributeInstance entityAttribute = entityLiving.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (entityAttribute != null) {
                        entityAttribute.setBaseValue(s);
                        entityLiving.addTag("ctrlSpeed");
                    }
                }
            }
        });
    }

    private void addSpeedAction(Float m, Float a) {
        float finalM = m == null ? 1 : m;
        float finalA = a == null ? 0 : a;
        actions.add(event -> {
            LivingEntity entityLiving = event.getEntityLiving();
            if (entityLiving != null) {
                if (!entityLiving.getTags().contains("ctrlSpeed")) {
                    AttributeInstance entityAttribute = entityLiving.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (entityAttribute != null) {
                        double newMax = entityAttribute.getBaseValue() * finalM + finalA;
                        entityAttribute.setBaseValue(newMax);
                        entityLiving.addTag("ctrlSpeed");
                    }
                }
            }
        });
    }

    private void addSizeActions(Float m, Float a) {
        m = m == null ? 1 : m;
        a = a == null ? 0 : a;
        ErrorHandler.error("Mob resizing not implemented yet!");
        actions.add(event -> {
            LivingEntity entityLiving = event.getEntityLiving();
            if (entityLiving != null) {
                // Not implemented yet
//                entityLiving.setSize(entityLiving.width * m + a, entityLiving.height * m + a);
            }
        });
    }

    private void addDamageSetAction(float s) {
        actions.add(event -> {
            LivingEntity entityLiving = event.getEntityLiving();
            if (entityLiving != null) {
                if (!entityLiving.getTags().contains("ctrlDamage")) {
                    AttributeInstance entityAttribute = entityLiving.getAttribute(Attributes.ATTACK_DAMAGE);
                    if (entityAttribute != null) {
                        entityAttribute.setBaseValue(s);
                        entityLiving.addTag("ctrlDamage");
                    }
                }
            }
        });
    }

    private void addDamageAction(Float m, Float a) {
        float finalM = m == null ? 1 : m;
        float finalA = a == null ? 0 : a;
        actions.add(event -> {
            LivingEntity entityLiving = event.getEntityLiving();
            if (entityLiving != null) {
                if (!entityLiving.getTags().contains("ctrlDamage")) {
                    AttributeInstance entityAttribute = entityLiving.getAttribute(Attributes.ATTACK_DAMAGE);
                    if (entityAttribute != null) {
                        double newMax = entityAttribute.getBaseValue() * finalM + finalA;
                        entityAttribute.setBaseValue(newMax);
                        entityLiving.addTag("ctrlDamage");
                    }
                }
            }
        });
    }

    protected void addArmorItem(List<String> itemList, EquipmentSlot slot) {
        final List<Pair<Float, ItemStack>> items = getItemsWeighted(itemList);
        if (items.isEmpty()) {
            return;
        }
        if (items.size() == 1) {
            ItemStack item = items.get(0).getRight();
            actions.add(event -> {
                LivingEntity entityLiving = event.getEntityLiving();
                if (entityLiving != null) {
                    entityLiving.setItemSlot(slot, item.copy());
                }
            });
        } else {
            final float total = getTotal(items);
            actions.add(event -> {
                LivingEntity entityLiving = event.getEntityLiving();
                if (entityLiving != null) {
                    entityLiving.setItemSlot(slot, getRandomItem(items, total));
                }
            });
        }
    }

    protected void addHeldItem(List<String> heldItems) {
        final List<Pair<Float, ItemStack>> items = getItemsWeighted(heldItems);
        if (items.isEmpty()) {
            return;
        }
        if (items.size() == 1) {
            ItemStack item = items.get(0).getRight();
            actions.add(event -> {
                LivingEntity entityLiving = event.getEntityLiving();
                if (entityLiving != null) {
                    if (entityLiving instanceof EnderMan) {
                        if (item.getItem() instanceof BlockItem) {
                            BlockItem b = (BlockItem) item.getItem();
                            ((EnderMan) entityLiving).setCarriedBlock(b.getBlock().defaultBlockState());
                        }
                    } else {
                        entityLiving.setItemInHand(InteractionHand.MAIN_HAND, item.copy());
                    }
                }
            });
        } else {
            final float total = getTotal(items);
            actions.add(event -> {
                LivingEntity entityLiving = event.getEntityLiving();
                if (entityLiving != null) {
                    ItemStack item = getRandomItem(items, total);
                    if (entityLiving instanceof EnderMan) {
                        if (item.getItem() instanceof BlockItem) {
                            BlockItem b = (BlockItem) item.getItem();
                            ((EnderMan) entityLiving).setCarriedBlock(b.getBlock().defaultBlockState());
                        }
                    } else {
                        entityLiving.setItemInHand(InteractionHand.MAIN_HAND, item.copy());
                    }
                }
            });
        }
    }

    private void addMobNBT(String mobnbt) {
        if (mobnbt != null) {
            CompoundTag tagCompound;
            try {
                tagCompound = TagParser.parseTag(mobnbt);
            } catch (CommandSyntaxException e) {
                ErrorHandler.error("Bad NBT for mob!");
                return;
            }
            actions.add(event -> {
                LivingEntity entityLiving = event.getEntityLiving();
                entityLiving.readAdditionalSaveData(tagCompound);   // @todo 1.15 right?
            });
        }
    }

    private void addCustomName(String customName) {
        if (customName != null) {
            actions.add(event -> {
                LivingEntity entityLiving = event.getEntityLiving();
                entityLiving.setCustomName(new TextComponent(customName));
            });
        }
    }

    protected void addAngryAction(boolean angry) {
        if (angry) {
            actions.add(event -> {
                LivingEntity entityLiving = event.getEntityLiving();
                if (entityLiving != null) {
                    Player player = event.getWorld().getNearestPlayer(entityLiving, 50);
                    if (player != null) {
                        entityLiving.setLastHurtByMob(player);
                        entityLiving.setLastHurtMob(player);
                        if (entityLiving instanceof NeutralMob) {
                            ((NeutralMob) entityLiving).setTarget(player);
                        }
                    }
                }
            });
        }
    }


}
