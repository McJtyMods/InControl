package mcjty.incontrol.rules.support;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import mcjty.incontrol.compat.ModRuleCompatibilityLayer;
import mcjty.incontrol.data.DataStorage;
import mcjty.incontrol.spawner.SpawnerSystem;
import mcjty.incontrol.tools.rules.CommonRuleEvaluator;
import mcjty.incontrol.tools.rules.IEventQuery;
import mcjty.incontrol.tools.typed.AttributeMap;
import mcjty.incontrol.tools.varia.Tools;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static mcjty.incontrol.rules.support.RuleKeys.*;


public class GenericRuleEvaluator extends CommonRuleEvaluator {

    public GenericRuleEvaluator(AttributeMap map) {
        super(map, InControl.setup.getLogger(), new ModRuleCompatibilityLayer());
    }

    @Override
    protected void addChecks(AttributeMap map) {
        super.addChecks(map);

        map.consume(ONJOIN, b -> {});
        map.consume(PHASE, b -> {});
        map.consume(HOSTILE, this::addHostileCheck);
        map.consume(PASSIVE, this::addPassiveCheck);
        map.consume(BABY, this::addBabyCheck);
        map.consume(CANSPAWNHERE, this::addCanSpawnHereCheck);
        map.consume(NOTCOLLIDING, this::addNotCollidingCheck);
        map.consume(SPAWNER, this::addSpawnerCheck);
        map.consume(INCONTROL, this::addInControlCheck);
        map.consumeAsList(MOB, this::addMobsCheck);
        map.consume(PLAYER, this::addPlayerCheck);
        map.consume(REALPLAYER, this::addRealPlayerCheck);
        map.consume(FAKEPLAYER, this::addFakePlayerCheck);
        map.consume(EXPLOSION, this::addExplosionCheck);
        map.consume(PROJECTILE, this::addProjectileCheck);
        map.consume(FIRE, this::addFireCheck);
        map.consume(MAGIC, this::addMagicCheck);
        map.consumeAsList(SOURCE, this::addSourceCheck);
        map.consumeAsList(MOD, this::addModsCheck);
        map.consume(MINCOUNT, this::addMinCountCheck);
        map.consume(MAXCOUNT, this::addMaxCountCheck);
        map.consume(DAYCOUNT, this::addDayCountCheck);
        map.consume(MINDAYCOUNT, this::addMinDayCountCheck);
        map.consume(MAXDAYCOUNT, this::addMaxDayCountCheck);
    }

    private void addCanSpawnHereCheck(boolean c) {
        if (c) {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof Mob) {
                    BlockPos pos = entity.blockPosition();
                    Level world = entity.getCommandSenderWorld();
                    LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                    if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                        return false;
                    }
                    return Mob.checkMobSpawnRules((EntityType<? extends Mob>) entity.getType(), world, MobSpawnType.NATURAL, pos, null);
                } else {
                    return false;
                }
            });
        } else {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof Mob) {
                    BlockPos pos = entity.blockPosition();
                    Level world = entity.getCommandSenderWorld();
                    LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                    if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                        return false;
                    }
                    return !Mob.checkMobSpawnRules((EntityType<? extends Mob>) entity.getType(), world, MobSpawnType.NATURAL, pos, null);
                } else {
                    return true;
                }
            });
        }
    }

    private void addNotCollidingCheck(boolean c) {
        if (c) {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof Mob) {
                    BlockPos pos = entity.blockPosition();
                    Level world = entity.getCommandSenderWorld();
                    LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                    if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                        return false;
                    }
                    return ((Mob) entity).checkSpawnObstruction(world);
                } else {
                    return false;
                }
            });
        } else {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof Mob) {
                    BlockPos pos = entity.blockPosition();
                    Level world = entity.getCommandSenderWorld();
                    LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                    if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                        return false;
                    }
                    return !((Mob) entity).checkSpawnObstruction(world);
                } else {
                    return true;
                }
            });
        }
    }

    private void addInControlCheck(boolean c) {
        checks.add((event, query) -> c == (SpawnerSystem.busySpawning != null));
    }

    private void addSpawnerCheck(boolean c) {
        if (c) {
            checks.add((event, query) -> {
                if (event instanceof LivingSpawnEvent.CheckSpawn checkSpawn) {
                    return checkSpawn.isSpawner();
                } else if (event instanceof LivingSpawnEvent.SpecialSpawn specialSpawn) {
                    return specialSpawn.getSpawnReason() == MobSpawnType.SPAWNER;
                } else {
                    return false;
                }
            });
        } else {
            checks.add((event, query) -> {
                if (event instanceof LivingSpawnEvent.CheckSpawn checkSpawn) {
                    return !checkSpawn.isSpawner();
                } else if (event instanceof LivingSpawnEvent.SpecialSpawn specialSpawn) {
                    return specialSpawn.getSpawnReason() != MobSpawnType.SPAWNER;
                } else {
                    return false;
                }
            });
        }
    }

    private void addBabyCheck(boolean baby) {
        checks.add((event, query) -> {
            Entity entity = query.getEntity(event);
            if (entity instanceof LivingEntity living) {
                return living.isBaby() == baby;
            }
            return false;
        });
    }

    private void addHostileCheck(boolean hostile) {
        if (hostile) {
            checks.add((event, query) -> query.getEntity(event) instanceof Enemy);
        } else {
            checks.add((event, query) -> !(query.getEntity(event) instanceof Enemy));
        }
    }

    private void addPassiveCheck(boolean passive) {
        if (passive) {
            checks.add((event, query) -> (query.getEntity(event) instanceof Animal && !(query.getEntity(event) instanceof Enemy)));
        } else {
            checks.add((event, query) -> !(query.getEntity(event) instanceof Animal && !(query.getEntity(event) instanceof Enemy)));
        }
    }

    private void addMobsCheck(List<String> mobs) {
        if (mobs.size() == 1) {
            String id = mobs.get(0);
            if (!ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(id))) {
                ErrorHandler.error("Unknown mob '" + id + "'!");
            }
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id));
            if (type != null) {
                checks.add((event, query) -> type.equals(query.getEntity(event).getType()));
            }
        } else {
            Set<EntityType> classes = new HashSet<>();
            for (String id : mobs) {
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id));
                if (type != null) {
                    classes.add(type);
                } else {
                    ErrorHandler.error("Unknown mob '" + id + "'!");
                }
            }
            if (!classes.isEmpty()) {
                checks.add((event, query) -> classes.contains(query.getEntity(event).getType()));
            }
        }
    }

    private void addModsCheck(List<String> mods) {
        if (mods.size() == 1) {
            String modid = mods.get(0);
            checks.add((event, query) -> {
                EntityType<?> type = query.getEntity(event).getType();
                String mod = ForgeRegistries.ENTITY_TYPES.getKey(type).getNamespace();
                return modid.equals(mod);
            });
        } else {
            Set<String> modids = new HashSet<>();
            for (String modid : mods) {
                modids.add(modid);
            }
            checks.add((event, query) -> {
                EntityType<?> type = query.getEntity(event).getType();
                String mod = ForgeRegistries.ENTITY_TYPES.getKey(type).getNamespace();
                return modids.contains(mod);
            });
        }
    }

    private static class CountInfo {
        private int amount;
        private Predicate<Integer> amountTester = null;
        private List<EntityType> entityTypes = new ArrayList<>();
        private boolean scaledPerPlayer = false;
        private boolean scaledPerChunk = false;
        private boolean passive = false;
        private boolean hostile = false;
        private boolean all = false;
        private String mod = null;

        public CountInfo() {
        }

        public CountInfo setAmount(int amount) {
            this.amount = amount;
            return this;
        }

        public CountInfo setAmountTester(Predicate<Integer> amountTester) {
            this.amountTester = amountTester;
            return this;
        }

        public CountInfo addEntityType(EntityType entityClass) {
            if (entityClass != null) {
                this.entityTypes.add(entityClass);
            }
            return this;
        }

        public CountInfo setScaledPerPlayer(boolean scaledPerPlayer) {
            this.scaledPerPlayer = scaledPerPlayer;
            return this;
        }

        public CountInfo setScaledPerChunk(boolean scaledPerChunk) {
            this.scaledPerChunk = scaledPerChunk;
            return this;
        }

        public CountInfo setAll(boolean all) {
            this.all = all;
            return this;
        }

        public CountInfo setPassive(boolean passive) {
            this.passive = passive;
            return this;
        }

        public CountInfo setHostile(boolean hostile) {
            this.hostile = hostile;
            return this;
        }

        public CountInfo setMod(String mod) {
            this.mod = mod;
            return this;
        }

        public String validate() {
            if (scaledPerPlayer && scaledPerChunk) {
                return "You cannot combine 'perchunk' and 'perplayer'!";
            }
            if (mod != null && !entityTypes.isEmpty()) {
                return "You cannot combine 'mod' with 'mob'!";
            }
            if ((passive && hostile) || (all && passive) || (all && hostile)) {
                return "Don't use all, passive, and hostile at the same time!";
            }
            if ((passive || hostile || all) && !entityTypes.isEmpty()) {
                return "You cannot combine 'all', 'passive', or 'hostile' with 'mob'!";
            }
            return null;
        }
    }

    @Nullable
    private CountInfo parseCountInfo(String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isString()) {
                String[] splitted = StringUtils.split(element.getAsString(), ',');
                int amount;
                try {
                    amount = Integer.parseInt(splitted[0]);
                } catch (NumberFormatException e) {
                    ErrorHandler.error("Bad amount for mincount '" + splitted[0] + "'!");
                    return null;
                }
                EntityType entityClass = null;
                if (splitted.length > 1) {
                    entityClass = findEntity(splitted[1]);
                    if (entityClass == null) {
                        ErrorHandler.error("Cannot find mob '" + splitted[1] + "'!");
                        return null;
                    }
                }
                return new CountInfo().setAmount(amount).addEntityType(entityClass);
            } else {
                int amount = element.getAsInt();
                return new CountInfo().setAmount(amount);
            }
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            int amount = obj.get("amount").getAsInt();
            CountInfo info = new CountInfo().setAmount(amount);
            if (obj.has("mob")) {
                if (obj.get("mob").isJsonPrimitive()) {
                    String entity = obj.get("mob").getAsString();
                    EntityType entityType = findEntity(entity);
                    if (entityType == null) return null;
                    info.addEntityType(entityType);
                } else if (obj.get("mob").isJsonArray()) {
                    JsonArray array = obj.get("mob").getAsJsonArray();
                    for (JsonElement el : array) {
                        String entity = el.getAsString();
                        EntityType entityType = findEntity(entity);
                        if (entityType == null) {
                            ErrorHandler.error("Cannot find mob '" + entity + "'!");
                            return null;
                        }
                        info.addEntityType(entityType);
                    }
                } else {
                    ErrorHandler.error("Bad entity tag in count description!");
                    return null;
                }
            }
            if (obj.has("mod")) {
                String mod = obj.get("mod").getAsString();
                info.setMod(mod);
            }
            if (obj.has("perplayer")) {
                info.setScaledPerPlayer(obj.get("perplayer").getAsBoolean());
            }
            if (obj.has("perchunk")) {
                info.setScaledPerChunk(obj.get("perchunk").getAsBoolean());
            }
            if (obj.has("passive")) {
                info.setPassive(obj.get("passive").getAsBoolean());
            }
            if (obj.has("all")) {
                info.setAll(obj.get("all").getAsBoolean());
            }
            if (obj.has("hostile")) {
                info.setHostile(obj.get("hostile").getAsBoolean());
            }
            String error = info.validate();
            if (error != null) {
                ErrorHandler.error(error);
                return null;
            }
            return info;
        } else {
            ErrorHandler.error("Count description '" + json + "' is not valid!");
            return null;
        }
    }

    private EntityType findEntity(String id) {
        EntityType<?> ee = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id));
        if (ee == null) {
            ErrorHandler.error("Unknown mob '" + id + "'!");
            return null;
        }
        return ee;
    }

    private void addMinCountCheck(String json) {
        CountInfo info = parseCountInfo(json);
        if (info == null) {
            return;
        }

        BiFunction<LevelAccessor, Entity, Integer> counter = getCounter(info);
        Function<LevelAccessor, Integer> amountAdjuster = getAmountAdjuster(info, info.amount);

        checks.add((event, query) -> {
            LevelAccessor world = query.getWorld(event);
            Entity entity = query.getEntity(event);
            int count = counter.apply(world, entity);
            int amount = amountAdjuster.apply(world);
            return count >= amount;
        });
    }

    private void addMaxCountCheck(String json) {
        CountInfo info = parseCountInfo(json);

        BiFunction<LevelAccessor, Entity, Integer> counter = getCounter(info);
        Function<LevelAccessor, Integer> amountAdjuster = getAmountAdjuster(info, info.amount);

        checks.add((event, query) -> {
            LevelAccessor world = query.getWorld(event);
            Entity entity = query.getEntity(event);
            int count = counter.apply(world, entity);
            int amount = amountAdjuster.apply(world);
            return count < amount;
        });
    }

    private void addDayCountCheck(Object count) {
        if (count == null) {
            return;
        }

        if (count instanceof Integer c) {
            checks.add((event, query) -> {
                LevelAccessor world = query.getWorld(event);
                DataStorage data = DataStorage.getData(Tools.getServerWorld(world));
                int amount = data.getDaycounter();
                return amount % c == 0;
            });
        } else if (count instanceof String input) {
            Predicate<Integer> expression = Tools.parseExpression(input);
            if (expression == null) {
                // Error already reported
                return;
            }
            checks.add((event, query) -> {
                LevelAccessor world = query.getWorld(event);
                DataStorage data = DataStorage.getData(Tools.getServerWorld(world));
                int amount = data.getDaycounter();
                return expression.test(amount);
            });
        }
    }

    private void addMinDayCountCheck(Integer count) {
        if (count == null) {
            return;
        }

        checks.add((event, query) -> {
            LevelAccessor world = query.getWorld(event);
            DataStorage data = DataStorage.getData(Tools.getServerWorld(world));
            int amount = data.getDaycounter();
            return amount >= count;
        });
    }

    private void addMaxDayCountCheck(Integer count) {
        if (count == null) {
            return;
        }

        checks.add((event, query) -> {
            LevelAccessor world = query.getWorld(event);
            DataStorage data = DataStorage.getData(Tools.getServerWorld(world));
            int amount = data.getDaycounter();
            return amount < count;
        });
    }

    private Function<LevelAccessor, Integer> getAmountAdjuster(CountInfo info, int infoAmount) {
        Function<LevelAccessor, Integer> amountAdjuster;
        if (info.scaledPerChunk) {
            amountAdjuster = world -> infoAmount * InControl.setup.cache.getValidSpawnChunks(world) / 289;
        } else if (info.scaledPerPlayer) {
            amountAdjuster = world -> infoAmount * InControl.setup.cache.getValidPlayers(world);
        } else {
            amountAdjuster = world -> infoAmount;
        }
        return amountAdjuster;
    }

    private BiFunction<LevelAccessor, Entity, Integer> getCounter(CountInfo info) {
        BiFunction<LevelAccessor, Entity, Integer> counter;
        if (info.mod != null) {
            if (info.hostile) {
                counter = (world, entity) -> InControl.setup.cache.getCountPerModHostile(world, info.mod);
            } else if (info.passive) {
                counter = (world, entity) -> InControl.setup.cache.getCountPerModPassive(world, info.mod);
            } else if (info.all) {
                counter = (world, entity) -> InControl.setup.cache.getCountPerModAll(world, info.mod);
            } else {
                counter = (world, entity) -> InControl.setup.cache.getCountPerMod(world, info.mod);
            }
        } else if (info.hostile) {
            counter = (world, entity) -> InControl.setup.cache.getCountHostile(world);
        } else if (info.passive) {
            counter = (world, entity) -> InControl.setup.cache.getCountPassive(world);
        } else if (info.all) {
            counter = (world, entity) -> InControl.setup.cache.getCountAll(world);
        } else {
            List<EntityType> infoEntityType = info.entityTypes;
            if (infoEntityType.isEmpty()) {
                counter = (world, entity) -> InControl.setup.cache.getCount(world, entity.getType());
            } else if (infoEntityType.size() == 1) {
                counter = (world, entity) -> {
                    EntityType entityType = infoEntityType.get(0);
                    return InControl.setup.cache.getCount(world, entityType);
                };
            } else {
                counter = (world, entity) -> {
                    int amount = 0;
                    for (EntityType cls : infoEntityType) {
                        amount += InControl.setup.cache.getCount(world, cls);
                    }
                    return amount;
                };
            }
        }
        return counter;
    }


    private void addPlayerCheck(boolean asPlayer) {
        if (asPlayer) {
            checks.add((event, query) -> query.getAttacker(event) instanceof Player);
        } else {
            checks.add((event, query) -> query.getAttacker(event) instanceof Player);
        }
    }


    private boolean isFakePlayer(Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }

        if (entity instanceof FakePlayer) {
            return true;
        }

        // If this returns false it is still possible we have a fake player. Try to find the player in the list of online players
        PlayerList playerList = entity.getCommandSenderWorld().getServer().getPlayerList();
        ServerPlayer playerByUUID = playerList.getPlayer(((Player) entity).getGameProfile().getId());
        if (playerByUUID == null) {
            // The player isn't online. Then it can't be real
            return true;
        }

        // The player is in the list. But is it this player?
        return entity != playerByUUID;
    }

    private boolean isRealPlayer(Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }
        return !isFakePlayer(entity);
    }

    private void addRealPlayerCheck(boolean asPlayer) {
        if (asPlayer) {
            checks.add((event, query) -> query.getAttacker(event) == null ? false : isRealPlayer(query.getAttacker(event)));
        } else {
            checks.add((event, query) -> query.getAttacker(event) == null ? true : !isRealPlayer(query.getAttacker(event)));
        }
    }

    private void addFakePlayerCheck(boolean asPlayer) {
        if (asPlayer) {
            checks.add((event, query) -> query.getAttacker(event) == null ? false : isFakePlayer(query.getAttacker(event)));
        } else {
            checks.add((event, query) -> query.getAttacker(event) == null ? true : !isFakePlayer(query.getAttacker(event)));
        }
    }

    private void addExplosionCheck(boolean explosion) {
        if (explosion) {
            checks.add((event, query) -> query.getSource(event) == null ? false : query.getSource(event).isExplosion());
        } else {
            checks.add((event, query) -> query.getSource(event) == null ? true : !query.getSource(event).isExplosion());
        }
    }

    private void addProjectileCheck(boolean projectile) {
        if (projectile) {
            checks.add((event, query) -> query.getSource(event) == null ? false : query.getSource(event).isProjectile());
        } else {
            checks.add((event, query) -> query.getSource(event) == null ? true : !query.getSource(event).isProjectile());
        }
    }

    private void addFireCheck(boolean fire) {
        if (fire) {
            checks.add((event, query) -> query.getSource(event) == null ? false : query.getSource(event).isFire());
        } else {
            checks.add((event, query) -> query.getSource(event) == null ? true : !query.getSource(event).isFire());
        }
    }

    private void addMagicCheck(boolean magic) {
        if (magic) {
            checks.add((event, query) -> query.getSource(event) == null ? false : query.getSource(event).isMagic());
        } else {
            checks.add((event, query) -> query.getSource(event) == null ? true : !query.getSource(event).isMagic());
        }
    }

    private void addSourceCheck(List<String> sources) {
        Set<String> sourceSet = new HashSet<>(sources);
        checks.add((event, query) -> {
            if (query.getSource(event) == null) {
                return false;
            }
            return sourceSet.contains(query.getSource(event).getMsgId());
        });
    }


    @Override
    public boolean match(Object event, IEventQuery query) {
        for (BiFunction<Object, IEventQuery, Boolean> rule : checks) {
            if (!rule.apply(event, query)) {
                return false;
            }
        }
        return true;
    }

}
