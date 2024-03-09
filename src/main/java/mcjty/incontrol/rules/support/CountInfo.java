package mcjty.incontrol.rules.support;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

class CountInfo {
    public int amount;
    private Predicate<Integer> amountTester = null;
    public List<EntityType> entityTypes = new ArrayList<>();
    public boolean scaledPerPlayer = false;
    public boolean scaledPerChunk = false;
    public boolean passive = false;
    public boolean hostile = false;
    public boolean all = false;
    public String mod = null;

    public CountInfo() {
    }

    public BiFunction<LevelAccessor, Entity, Integer> getCounter() {
        BiFunction<LevelAccessor, Entity, Integer> counter;
        if (mod != null) {
            if (hostile) {
                counter = (world, entity) -> InControl.setup.cache.getCountPerModHostile(world, mod);
            } else if (passive) {
                counter = (world, entity) -> InControl.setup.cache.getCountPerModPassive(world, mod);
            } else if (all) {
                counter = (world, entity) -> InControl.setup.cache.getCountPerModAll(world, mod);
            } else {
                counter = (world, entity) -> InControl.setup.cache.getCountPerMod(world, mod);
            }
        } else if (hostile) {
            counter = (world, entity) -> InControl.setup.cache.getCountHostile(world);
        } else if (passive) {
            counter = (world, entity) -> InControl.setup.cache.getCountPassive(world);
        } else if (all) {
            counter = (world, entity) -> InControl.setup.cache.getCountAll(world);
        } else {
            List<EntityType> infoEntityType = entityTypes;
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

    static Function<LevelAccessor, Integer> getAmountAdjuster(CountInfo info, int infoAmount) {
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

    @Nullable
    static CountInfo parseCountInfo(String json) {
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

    private static EntityType findEntity(String id) {
        EntityType<?> ee = BuiltInRegistries.ENTITY_TYPE.getValue(new ResourceLocation(id));
        if (ee == null) {
            ErrorHandler.error("Unknown mob '" + id + "'!");
            return null;
        }
        return ee;
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
