package mcjty.incontrol.rules.support;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.areas.Area;
import mcjty.incontrol.areas.AreaSystem;
import mcjty.incontrol.compat.ModRuleCompatibilityLayer;
import mcjty.incontrol.data.DataStorage;
import mcjty.incontrol.events.EventsSystem;
import mcjty.incontrol.spawner.SpawnerSystem;
import mcjty.incontrol.tools.cache.StructureCache;
import mcjty.incontrol.tools.rules.TestingBlockTools;
import mcjty.incontrol.tools.rules.IEventQuery;
import mcjty.incontrol.tools.rules.IModRuleCompatibilityLayer;
import mcjty.incontrol.tools.rules.TestingTools;
import mcjty.incontrol.tools.typed.AttributeMap;
import mcjty.incontrol.tools.varia.Tools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.BiomeManager;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static mcjty.incontrol.rules.support.RuleKeys.*;


public class GenericRuleEvaluator {

    private static final Random rnd = new Random();
    private final List<BiFunction<Object, IEventQuery, Boolean>> checks = new ArrayList<>();
    private final IModRuleCompatibilityLayer compatibility;

    public GenericRuleEvaluator(AttributeMap map) {
        this.compatibility = new ModRuleCompatibilityLayer();
        addChecks(map);
    }

    private void addChecks(AttributeMap map) {
        map.consume(RANDOM, this::addRandomCheck);
        map.consumeAsList(DIMENSION, this::addDimensionCheck);
        map.consumeAsList(DIMENSION_MOD, this::addDimensionModCheck);

        map.consume(TIME, this::addTimeCheck);
        map.consume(MINTIME, this::addMinTimeCheck);
        map.consume(MAXTIME, this::addMaxTimeCheck);

        map.consume(HEIGHT, this::addHeightCheck);
        map.consume(MINHEIGHT, this::addMinHeightCheck);
        map.consume(MAXHEIGHT, this::addMaxHeightCheck);

        map.consume(WEATHER, this::addWeatherCheck);
        map.consumeAsList(BIOMETAGS, this::addBiomeTagCheck);
        map.consume(DIFFICULTY, this::addDifficultyCheck);
        map.consume(MINSPAWNDIST, this::addMinSpawnDistCheck);
        map.consume(MAXSPAWNDIST, this::addMaxSpawnDistCheck);

        map.consume(LIGHT, this::addLightCheck);
        map.consume(MINLIGHT, this::addMinLightCheck);
        map.consume(MAXLIGHT, this::addMaxLightCheck);
        map.consume(MINLIGHT_FULL, this::addMinLightCheckCorrect);
        map.consume(MAXLIGHT_FULL, this::addMaxLightCheckCorrect);

        map.consume(MINDIFFICULTY, this::addMinAdditionalDifficultyCheck);
        map.consume(MAXDIFFICULTY, this::addMaxAdditionalDifficultyCheck);
        map.consume(SEESKY, this::addSeeSkyCheck);
        map.consume(SLIME, this::addSlimeChunkCheck);
        map.consume(AREA, this::addAreaCheck);

        map.consumeAsList(BLOCK, b1 -> addBlocksCheck(map, b1));
        map.consumeAsList(BIOME, this::addBiomesCheck);
        map.consumeAsList(BIOMETYPE, this::addBiomeTypesCheck);

        map.consumeAsList(HELMET, this::addHelmetCheck);
        map.consumeAsList(CHESTPLATE, this::addChestplateCheck);
        map.consumeAsList(LEGGINGS, this::addLeggingsCheck);
        map.consumeAsList(BOOTS, this::addBootsCheck);
        map.consumeAsList(PLAYER_HELDITEM, items -> addHeldItemCheck(items, false));
        map.consumeAsList(HELDITEM, items -> addHeldItemCheck(items, false));
        map.consumeAsList(OFFHANDITEM, items -> addOffHandItemCheck(items, false));
        map.consumeAsList(BOTHHANDSITEM, this::addBothHandsItemCheck);

        map.consumeAsList(LACKHELMET, this::addHelmetCheckLacking);
        map.consumeAsList(LACKCHESTPLATE, this::addChestplateCheckLacking);
        map.consumeAsList(LACKLEGGINGS, this::addLeggingsCheckLacking);
        map.consumeAsList(LACKBOOTS, this::addBootsCheckLacking);
        map.consumeAsList(LACKHELDITEM, items -> addHeldItemCheck(items, true));
        map.consumeAsList(LACKOFFHANDITEM, items -> addOffHandItemCheck(items, true));

        map.consume(STRUCTURE, this::addStructureCheck);
        map.consumeAsList(SCOREBOARDTAGS_ALL, this::addAllScoreboardTagsCheck);
        map.consumeAsList(SCOREBOARDTAGS_ANY, this::addAnyScoreboardTagsCheck);

        map.consume(STATE, this::addStateCheck);
        map.consume(PSTATE, this::addPStateCheck);

        map.consume(SUMMER, this::addSummerCheck);
        map.consume(WINTER, this::addWinterCheck);
        map.consume(SPRING, this::addSpringCheck);
        map.consume(AUTUMN, this::addAutumnCheck);

        map.consume(GAMESTAGE, this::addGameStageCheck);

        map.consume(INCITY, this::addInCityCheck);
        map.consume(INSTREET, this::addInStreetCheck);
        map.consume(INSPHERE, this::addInSphereCheck);
        map.consume(INBUILDING, this::addInBuildingCheck);
        map.consumeAsList(BUILDING, this::addBuildingCheck);

        map.consumeAsList(AMULET, v -> addBaubleCheck(v, compatibility::getAmuletSlots));
        map.consumeAsList(RING, v -> addBaubleCheck(v, compatibility::getRingSlots));
        map.consumeAsList(BELT, v -> addBaubleCheck(v, compatibility::getBeltSlots));
        map.consumeAsList(TRINKET, v -> addBaubleCheck(v, compatibility::getTrinketSlots));
        map.consumeAsList(HEAD, v -> addBaubleCheck(v, compatibility::getHeadSlots));
        map.consumeAsList(BODY, v -> addBaubleCheck(v, compatibility::getBodySlots));
        map.consumeAsList(CHARM, v -> addBaubleCheck(v, compatibility::getCharmSlots));

        map.consume(WHEN, b -> {});
        map.consume(PHASE, b -> {});
        map.consume(NUMBER, this::addNumberCheck);
        map.consume(HOSTILE, this::addHostileCheck);
        map.consume(PASSIVE, this::addPassiveCheck);
        map.consume(BABY, this::addBabyCheck);
        map.consume(CANSPAWNHERE, this::addCanSpawnHereCheck);
        map.consume(NOTCOLLIDING, this::addNotCollidingCheck);
        map.consume(SPAWNER, this::addSpawnerCheck);
        map.consume(INCONTROL, this::addInControlCheck);
        map.consume(EVENTSPAWN, this::addEventSpawnCheck);
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

    private void addNumberCheck(String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        TestingTools.NumberResult result = TestingTools.parseNumberCheck(element);
        if (result != null) {
            checks.add((event,query) -> {
                DataStorage data = DataStorage.getData(Tools.getServerWorld(query.getWorld(event)));
                return result.test().test(data.getNumber(result.number()));
            });
        }
    }

    private void addCanSpawnHereCheck(boolean c) {
        if (c) {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof Mob) {
                    BlockPos pos = entity.blockPosition();
                    Level world = entity.getCommandSenderWorld();
                    if (TestingTools.isChunkInvalid(world, pos)) return false;
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
                    if (TestingTools.isChunkInvalid(world, pos)) return false;
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
                    if (TestingTools.isChunkInvalid(world, pos)) return false;
                    return ((Mob) entity).checkSpawnObstruction(world);
                } else {
                    return false;
                }
            });
        } else {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof Mob mob) {
                    BlockPos pos = entity.blockPosition();
                    Level world = entity.getCommandSenderWorld();
                    if (TestingTools.isChunkInvalid(world, pos)) return false;
                    return !mob.checkSpawnObstruction(world);
                } else {
                    return true;
                }
            });
        }
    }

    private void addInControlCheck(boolean c) {
        checks.add((event, query) -> c == (SpawnerSystem.busySpawning != null));
    }

    private void addEventSpawnCheck(boolean c) {
        checks.add((event, query) -> c == (EventsSystem.busySpawning != null));
    }

    private void addSpawnerCheck(boolean c) {
        if (c) {
            checks.add((event, query) -> {
                if (event instanceof MobSpawnEvent.FinalizeSpawn checkSpawn) {
                    return checkSpawn.getSpawnType().equals(MobSpawnType.SPAWNER);
                } else if (event instanceof MobSpawnEvent.PositionCheck checkSpawn) {
                    return checkSpawn.getSpawnType().equals(MobSpawnType.SPAWNER);
                } else {
                    return query.getEntity(event) instanceof Mob mob && Objects.equals(mob.getSpawnType(), MobSpawnType.SPAWNER);
                }
            });
        } else {
            checks.add((event, query) -> {
                if (event instanceof MobSpawnEvent.FinalizeSpawn checkSpawn) {
                    return !checkSpawn.getSpawnType().equals(MobSpawnType.SPAWNER);
                } else if (event instanceof MobSpawnEvent.PositionCheck checkSpawn) {
                    return !checkSpawn.getSpawnType().equals(MobSpawnType.SPAWNER);
                } else {
                    return !(query.getEntity(event) instanceof Mob mob && Objects.equals(mob.getSpawnType(), MobSpawnType.SPAWNER));
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
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(new ResourceLocation(id))) {
                ErrorHandler.error("Unknown mob '" + id + "'!");
            }
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(new ResourceLocation(id));
            if (type != null) {
                checks.add((event, query) -> type.equals(query.getEntity(event).getType()));
            }
        } else {
            Set<EntityType> classes = new HashSet<>();
            for (String id : mobs) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(new ResourceLocation(id));
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
                String mod = BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace();
                return modid.equals(mod);
            });
        } else {
            Set<String> modids = new HashSet<>();
            modids.addAll(mods);
            checks.add((event, query) -> {
                EntityType<?> type = query.getEntity(event).getType();
                String mod = BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace();
                return modids.contains(mod);
            });
        }
    }

    private void addRandomCheck(float r) {
        checks.add((event,query) -> rnd.nextFloat() < r);
    }

    private void addSeeSkyCheck(boolean seesky) {
        if (seesky) {
            checks.add((event,query) -> {
                LevelAccessor world = query.getWorld(event);
                BlockPos pos = query.getPos(event);
                if (TestingTools.isChunkInvalid(world, pos)) return false;
                return world.canSeeSkyFromBelowWater(pos);
            });
        } else {
            checks.add((event,query) -> {
                LevelAccessor world = query.getWorld(event);
                BlockPos pos = query.getPos(event);
                if (TestingTools.isChunkInvalid(world, pos)) return false;
                return !world.canSeeSkyFromBelowWater(pos);
            });
        }
    }

    private void addSlimeChunkCheck(boolean slime) {
        if (slime) {
            checks.add((event,query) -> TestingTools.isSlimeChunk(new ChunkPos(query.getPos(event)), query.getWorld(event)));
        } else {
            checks.add((event,query) -> !TestingTools.isSlimeChunk(new ChunkPos(query.getPos(event)), query.getWorld(event)));
        }
    }

    private void addDimensionCheck(List<ResourceKey<Level>> dimensions) {
        if (dimensions.size() == 1) {
            ResourceKey<Level> dim = dimensions.get(0);
            checks.add((event,query) -> Tools.getDimensionKey(query.getWorld(event)).equals(dim));
        } else {
            Set<ResourceKey<Level>> dims = new HashSet<>(dimensions);
            checks.add((event,query) -> dims.contains(Tools.getDimensionKey(query.getWorld(event))));
        }
    }

    private void addDimensionModCheck(List<String> dimensions) {
        if (dimensions.size() == 1) {
            String dimmod = dimensions.get(0);
            checks.add((event,query) -> Tools.getDimensionKey(query.getWorld(event)).location().getNamespace().equals(dimmod));
        } else {
            Set<String> dims = new HashSet<>(dimensions);
            checks.add((event,query) -> dims.contains(Tools.getDimensionKey(query.getWorld(event)).location().getNamespace()));
        }
    }

    private void addDifficultyCheck(String difficulty) {
        difficulty = difficulty.toLowerCase();
        Difficulty diff = Difficulty.byName(difficulty);
        if (diff != null) {
            Difficulty finalDiff = diff;
            checks.add((event,query) -> query.getWorld(event).getDifficulty() == finalDiff);
        } else {
            ErrorHandler.error("Unknown difficulty '" + difficulty + "'! Use one of 'easy', 'normal', 'hard',  or 'peaceful'");
        }
    }

    private void addWeatherCheck(String weather) {
        boolean raining = weather.toLowerCase().startsWith("rain");
        boolean thunder = weather.toLowerCase().startsWith("thunder");
        if (raining) {
            checks.add((event,query) -> {
                LevelAccessor world = query.getWorld(event);
                if (world instanceof Level level) {
                    return level.isRaining();
                } else {
                    return false;
                }
            });
        } else if (thunder) {
            checks.add((event, query) -> {
                LevelAccessor world = query.getWorld(event);
                if (world instanceof Level level) {
                    return level.isThundering();
                } else {
                    return false;
                }
            });
        } else {
            ErrorHandler.error("Unknown weather '" + weather + "'! Use 'rain' or 'thunder'");
        }
    }

    private void addBiomeTagCheck(List<String> list) {
        Set<TagKey<Biome>> tags = list.stream().map(s -> TagKey.create(Registries.BIOME, new ResourceLocation(s))).collect(Collectors.toSet());
        if (tags.size() == 1) {
            TagKey<Biome> key = tags.iterator().next();
            checks.add((event,query) -> {
                Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
                return biome.is(key);
            });
        } else {
            checks.add((event, query) -> {
                Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
                for (TagKey<Biome> tag : tags) {
                    if (biome.is(tag)) {
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private void addAllScoreboardTagsCheck(List<String> list) {
        Set<String> tags = new HashSet<>(list);
        checks.add((event,query) -> {
            Entity entity = query.getEntity(event);
            if (entity instanceof LivingEntity living) {
                return living.getTags().containsAll(tags);
            }
            return false;
        });
    }

    private void addAnyScoreboardTagsCheck(List<String> list) {
        Set<String> tags = new HashSet<>(list);
        checks.add((event,query) -> {
            Entity entity = query.getEntity(event);
            if (entity instanceof LivingEntity living) {
                // Return true if entity.getTags() contains any key from tags
                return living.getTags().stream().anyMatch(tags::contains);
            }
            return false;
        });
    }

    private void addStructureCheck(String structure) {
        checks.add((event,query) -> StructureCache.CACHE.isInStructure(query.getWorld(event), structure, query.getPos(event)));
    }

    private void addBiomesCheck(List<String> biomes) {
        if (biomes.size() == 1) {
            String biomename = biomes.get(0);
            checks.add((event,query) -> {
                Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
                return Tools.getBiomeId(biome).equals(biomename);
            });
        } else {
            Set<String> biomenames = new HashSet<>(biomes);
            checks.add((event,query) -> {
                Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
                String biomeId = Tools.getBiomeId(biome);
                return biomenames.contains(biomeId);
            });
        }
    }

    private void addBiomeTypesCheck(List<String> biomeTypes) {
        Set<Biome> biomes = new HashSet<>();
        biomeTypes.stream().map(s -> BiomeManager.BiomeType.valueOf(s.toUpperCase())).
                forEach(type -> BiomeManager.getBiomes(type).forEach(t -> biomes.add(ForgeRegistries.BIOMES.getValue(t.getKey().registry()))));

        checks.add((event,query) -> {
            Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
            return biomes.contains(biome.value());
        });
    }

    private void addBlocksCheck(AttributeMap map, List<String> blocks) {
        BiFunction<Object, IEventQuery, BlockPos> posFunction;
        String bo = map.consumeAndFetch(BLOCKOFFSET);
        if (bo != null) {
            posFunction = TestingBlockTools.parseOffset(bo);
        } else {
            posFunction = (event, query) -> query.getValidBlockPos(event);
        }

        if (blocks.size() == 1) {
            String json = blocks.get(0);
            BiPredicate<LevelAccessor, BlockPos> blockMatcher = TestingBlockTools.parseBlock(json);
            if (blockMatcher != null) {
                checks.add((event, query) -> {
                    BlockPos pos = posFunction.apply(event, query);
                    return pos != null && blockMatcher.test(query.getWorld(event), pos);
                });
            }
        } else {
            List<BiPredicate<LevelAccessor, BlockPos>> blockMatchers = new ArrayList<>();
            for (String block : blocks) {
                BiPredicate<LevelAccessor, BlockPos> blockMatcher = TestingBlockTools.parseBlock(block);
                if (blockMatcher == null) {
                    return;
                }
                blockMatchers.add(blockMatcher);
            }

            checks.add((event,query) -> {
                BlockPos pos = posFunction.apply(event, query);
                if (pos != null) {
                    LevelAccessor world = query.getWorld(event);
                    for (BiPredicate<LevelAccessor, BlockPos> matcher : blockMatchers) {
                        if (matcher.test(world, pos)) {
                            return true;
                        }
                    }
                }
                return false;
            });
        }
    }

    private void addTimeCheck(String time) {
        Predicate<Integer> expression = Tools.parseExpression(time);
        if (expression != null) {
            checks.add((event,query) -> {
                LevelAccessor world = query.getWorld(event);
                if (world instanceof Level) {
                    long t = ((Level)world).getDayTime();
                    return expression.test((int) (t % 24000));
                } else {
                    return false;
                }
            });
        }
    }

    private void addMinTimeCheck(int mintime) {
        checks.add((event,query) -> {
            LevelAccessor world = query.getWorld(event);
            if (world instanceof Level) {
                long time = ((Level)world).getDayTime();
                return (time % 24000) >= mintime;
            } else {
                return false;
            }
        });
    }

    private void addMaxTimeCheck(int maxtime) {
        checks.add((event,query) -> {
            LevelAccessor world = query.getWorld(event);
            if (world instanceof Level) {
                long time = ((Level)world).getDayTime();
                return (time % 24000) <= maxtime;
            } else {
                return false;
            }
        });
    }

    private void addMinSpawnDistCheck(float v) {
        final float d = v * v;
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            ServerLevel sw = Tools.getServerWorld(query.getWorld(event));
            double sqdist = pos.distSqr(sw.getSharedSpawnPos());
            return sqdist >= d;
        });
    }

    private void addMaxSpawnDistCheck(float v) {
        final float d = v * v;
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            ServerLevel sw = Tools.getServerWorld(query.getWorld(event));
            double sqdist = pos.distSqr(sw.getSharedSpawnPos());
            return sqdist <= d;
        });
    }

    private void addLightCheck(String expression) {
        Predicate<Integer> exp = Tools.parseExpression(expression);
        if (exp != null) {
            checks.add((event, query) -> {
                BlockPos pos = query.getPos(event);
                LevelAccessor world = query.getWorld(event);
                if (TestingTools.isChunkInvalid(world, pos)) return false;
                return exp.test(world.getBrightness(LightLayer.BLOCK, pos));
            });
        }
    }

    private void addMinLightCheck(int minlight) {
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            LevelAccessor world = query.getWorld(event);
            if (TestingTools.isChunkInvalid(world, pos)) return false;
            return world.getBrightness(LightLayer.BLOCK, pos) >= minlight;
        });
    }

    private void addMinLightCheckCorrect(int minlight) {
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            LevelAccessor world = query.getWorld(event);
            if (TestingTools.isChunkInvalid(world, pos)) return false;
            return world.getMaxLocalRawBrightness(pos) >= minlight;
        });
    }

    private void addMaxLightCheck(int maxlight) {
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            LevelAccessor world = query.getWorld(event);
            if (TestingTools.isChunkInvalid(world, pos)) return false;
            return world.getBrightness(LightLayer.BLOCK, pos) <= maxlight;
        });
    }

    private void addMaxLightCheckCorrect(int maxlight) {
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            LevelAccessor world = query.getWorld(event);
            if (TestingTools.isChunkInvalid(world, pos)) return false;
            return world.getMaxLocalRawBrightness(pos) <= maxlight;
        });
    }

    private void addMinAdditionalDifficultyCheck(Float mindifficulty) {
        checks.add((event,query) -> query.getWorld(event).getCurrentDifficultyAt(query.getPos(event)).getEffectiveDifficulty() >= mindifficulty);
    }

    private void addMaxAdditionalDifficultyCheck(Float maxdifficulty) {
        checks.add((event,query) -> query.getWorld(event).getCurrentDifficultyAt(query.getPos(event)).getEffectiveDifficulty() <= maxdifficulty);
    }

    private void addHeightCheck(String input) {
        Predicate<Integer> expression = Tools.parseExpression(input);
        if (expression != null) {
            checks.add((event, query) -> expression.test(query.getY(event)));
        }
    }

    private void addMaxHeightCheck(int maxheight) {
        checks.add((event,query) -> query.getY(event) <= maxheight);
    }

    private void addMinHeightCheck(int minheight) {
        checks.add((event,query) -> query.getY(event) >= minheight);
    }

    private void addHelmetCheck(List<String> itemList) {
        List<Predicate<ItemStack>> items = TestingTools.getItems(itemList);
        addArmorCheck(items, EquipmentSlot.HEAD, false);
    }

    private void addChestplateCheck(List<String> itemList) {
        List<Predicate<ItemStack>> items = TestingTools.getItems(itemList);
        addArmorCheck(items, EquipmentSlot.CHEST, false);
    }

    private void addLeggingsCheck(List<String> itemList) {
        List<Predicate<ItemStack>> items = TestingTools.getItems(itemList);
        addArmorCheck(items, EquipmentSlot.LEGS, false);
    }

    private void addBootsCheck(List<String> itemList) {
        List<Predicate<ItemStack>> items = TestingTools.getItems(itemList);
        addArmorCheck(items, EquipmentSlot.FEET, false);
    }

    private void addHelmetCheckLacking(List<String> itemList) {
        List<Predicate<ItemStack>> items = TestingTools.getItems(itemList);
        addArmorCheck(items, EquipmentSlot.HEAD, true);
    }

    private void addChestplateCheckLacking(List<String> itemList) {
        List<Predicate<ItemStack>> items = TestingTools.getItems(itemList);
        addArmorCheck(items, EquipmentSlot.CHEST, true);
    }

    private void addLeggingsCheckLacking(List<String> itemList) {
        List<Predicate<ItemStack>> items = TestingTools.getItems(itemList);
        addArmorCheck(items, EquipmentSlot.LEGS, true);
    }

    private void addBootsCheckLacking(List<String> itemList) {
        List<Predicate<ItemStack>> items = TestingTools.getItems(itemList);
        addArmorCheck(items, EquipmentSlot.FEET, true);
    }

    private void addArmorCheck(List<Predicate<ItemStack>> items, EquipmentSlot slot, boolean lacking) {
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                ItemStack armorItem = player.getItemBySlot(slot);
                if (!armorItem.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(armorItem)) {
                            return !lacking;
                        }
                    }
                }
            }
            return lacking;
        });
    }

    private void addHeldItemCheck(List<String> itemList, boolean lacking) {
        List<Predicate<ItemStack>> items = TestingTools.getItems(itemList);
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                ItemStack mainhand = player.getMainHandItem();
                if (!mainhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(mainhand)) {
                            return !lacking;
                        }
                    }
                }
            }
            return lacking;
        });
    }

    private void addOffHandItemCheck(List<String> itemList, boolean lacking) {
        List<Predicate<ItemStack>> items = TestingTools.getItems(itemList);
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                ItemStack offhand = player.getOffhandItem();
                if (!offhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(offhand)) {
                            return !lacking;
                        }
                    }
                }
            }
            return lacking;
        });
    }

    private void addBothHandsItemCheck(List<String> itemList) {
        List<Predicate<ItemStack>> items = TestingTools.getItems(itemList);
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                ItemStack offhand = player.getOffhandItem();
                if (!offhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(offhand)) {
                            return true;
                        }
                    }
                }
                ItemStack mainhand = player.getMainHandItem();
                if (!mainhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(mainhand)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    private void addStateCheck(String s) {
        if (!compatibility.hasEnigmaScript()) {
            TestingTools.warn("EnigmaScript is missing: this test cannot work!");
            return;
        }
        try {
            String[] split = StringUtils.split(s, '=');
            String state = split[0];
            String value = split[1];
            checks.add((event, query) -> value.equals(compatibility.getState(query.getWorld(event), state)));
        } catch (Exception e) {
            ErrorHandler.error("Bad state=value specifier '" + s + "'!");
        }
    }

    private void addPStateCheck(String s) {
        if (!compatibility.hasEnigmaScript()) {
            TestingTools.warn("EnigmaScript is missing: this test cannot work!");
            return;
        }
        try {
            String[] split = StringUtils.split(s, '=');
            String state = split[0];
            String value = split[1];
            checks.add((event, query) -> value.equals(compatibility.getPlayerState(query.getPlayer(event), state)));
        } catch (Exception e) {
            ErrorHandler.error("Bad state=value specifier '" + s + "'!");
        }
    }

    private void addSummerCheck(Boolean s) {
        if (!compatibility.hasSereneSeasons()) {
            TestingTools.warn("Serene Seasons is missing: this test cannot work!");
            return;
        }
        checks.add((event, query) -> s == compatibility.isSummer(Tools.getServerWorld(query.getWorld(event))));
    }

    private void addWinterCheck(Boolean s) {
        if (!compatibility.hasSereneSeasons()) {
            TestingTools.warn("Serene Seasons is missing: this test cannot work!");
            return;
        }
        checks.add((event, query) -> s == compatibility.isWinter(Tools.getServerWorld(query.getWorld(event))));
    }

    private void addSpringCheck(Boolean s) {
        if (!compatibility.hasSereneSeasons()) {
            TestingTools.warn("Serene Seasons is missing: this test cannot work!");
            return;
        }
        checks.add((event, query) -> s == compatibility.isSpring(Tools.getServerWorld(query.getWorld(event))));
    }

    private void addAutumnCheck(Boolean s) {
        if (!compatibility.hasSereneSeasons()) {
            TestingTools.warn("Serene Seasons is missing: this test cannot work!");
            return;
        }
        checks.add((event, query) -> s == compatibility.isAutumn(Tools.getServerWorld(query.getWorld(event))));
    }

    private void addGameStageCheck(String stage) {
        if (!compatibility.hasGameStages()) {
            TestingTools.warn("Game Stages is missing: the 'gamestage' test cannot work!");
            return;
        }
        checks.add((event, query) -> compatibility.hasGameStage(query.getPlayer(event), stage));
    }

    private void addInCityCheck(boolean incity) {
        if (!compatibility.hasLostCities()) {
            TestingTools.warn("The Lost Cities is missing: the 'incity' test cannot work!");
            return;
        }
        if (incity) {
            checks.add((event,query) -> compatibility.isCity(query, event));
        } else {
            checks.add((event,query) -> !compatibility.isCity(query, event));
        }
    }

    private void addInStreetCheck(boolean instreet) {
        if (!compatibility.hasLostCities()) {
            TestingTools.warn("The Lost Cities is missing: the 'instreet' test cannot work!");
            return;
        }
        if (instreet) {
            checks.add((event,query) -> compatibility.isStreet(query, event));
        } else {
            checks.add((event,query) -> !compatibility.isStreet(query, event));
        }
    }

    private void addInSphereCheck(boolean insphere) {
        if (!compatibility.hasLostCities()) {
            TestingTools.warn("The Lost Cities is missing: the 'insphere' test cannot work!");
            return;
        }
        if (insphere) {
            checks.add((event,query) -> compatibility.inSphere(query, event));
        } else {
            checks.add((event,query) -> !compatibility.inSphere(query, event));
        }
    }

    private void addInBuildingCheck(boolean inbuilding) {
        if (!compatibility.hasLostCities()) {
            TestingTools.warn("The Lost Cities is missing: the 'inbuilding' test cannot work!");
            return;
        }
        if (inbuilding) {
            checks.add((event,query) -> compatibility.isBuilding(query, event));
        } else {
            checks.add((event,query) -> !compatibility.isBuilding(query, event));
        }
    }

    private void addBuildingCheck(List<String> buildings) {
        if (!compatibility.hasLostCities()) {
            TestingTools.warn("The Lost Cities is missing: the 'building' test cannot work!");
            return;
        }
        Set<String> buildingSet = new HashSet<>(buildings);
        checks.add((event,query) -> {
            String building = compatibility.getBuilding(query, event);
            return building != null && buildingSet.contains(building);
        });
    }

    private void addBaubleCheck(List<String> itemList, Supplier<int[]> slotSupplier) {
        if (!compatibility.hasBaubles()) {
            TestingTools.warn("Baubles is missing: this test cannot work!");
            return;
        }

        List<Predicate<ItemStack>> items = TestingTools.getItems(itemList);
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                for (int slot : slotSupplier.get()) {
                    ItemStack stack = compatibility.getBaubleStack(player, slot);
                    if (!stack.isEmpty()) {
                        for (Predicate<ItemStack> item : items) {
                            if (item.test(stack)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        });
    }

    private void addMinCountCheck(String json) {
        CountInfo info = CountInfo.parseCountInfo(json);
        if (info == null) {
            return;
        }

        BiFunction<LevelAccessor, Entity, Integer> counter = info.getCounter();
        Function<LevelAccessor, Integer> amountAdjuster = CountInfo.getAmountAdjuster(info, info.amount);

        checks.add((event, query) -> {
            LevelAccessor world = query.getWorld(event);
            Entity entity = query.getEntity(event);
            int count = counter.apply(world, entity);
            int amount = amountAdjuster.apply(world);
            return count >= amount;
        });
    }

    private void addMaxCountCheck(String json) {
        CountInfo info = CountInfo.parseCountInfo(json);

        BiFunction<LevelAccessor, Entity, Integer> counter = info.getCounter();
        Function<LevelAccessor, Integer> amountAdjuster = CountInfo.getAmountAdjuster(info, info.amount);

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

    private void addPlayerCheck(boolean asPlayer) {
        if (asPlayer) {
            checks.add((event, query) -> query.getAttacker(event) instanceof Player);
        } else {
            checks.add((event, query) -> query.getAttacker(event) instanceof Player);
        }
    }

    private void addRealPlayerCheck(boolean asPlayer) {
        if (asPlayer) {
            checks.add((event, query) -> query.getAttacker(event) == null ? false : TestingTools.isRealPlayer(query.getAttacker(event)));
        } else {
            checks.add((event, query) -> query.getAttacker(event) == null ? true : !TestingTools.isRealPlayer(query.getAttacker(event)));
        }
    }

    private void addFakePlayerCheck(boolean asPlayer) {
        if (asPlayer) {
            checks.add((event, query) -> query.getAttacker(event) == null ? false : TestingTools.isFakePlayer(query.getAttacker(event)));
        } else {
            checks.add((event, query) -> query.getAttacker(event) == null ? true : !TestingTools.isFakePlayer(query.getAttacker(event)));
        }
    }

    private void addExplosionCheck(boolean explosion) {
        if (explosion) {
            checks.add((event, query) -> query.getSource(event) == null ? false : query.getSource(event).is(DamageTypes.EXPLOSION));
        } else {
            checks.add((event, query) -> query.getSource(event) == null ? true : !query.getSource(event).is(DamageTypes.EXPLOSION));
        }
    }

    private void addProjectileCheck(boolean projectile) {
        if (projectile) {
            checks.add((event, query) -> query.getSource(event) == null ? false : query.getSource(event).is(DamageTypes.EXPLOSION));
        } else {
            checks.add((event, query) -> query.getSource(event) == null ? true : !query.getSource(event).is(DamageTypes.EXPLOSION));
        }
    }

    private void addFireCheck(boolean fire) {
        if (fire) {
            checks.add((event, query) -> query.getSource(event) == null ? false : query.getSource(event).is(DamageTypes.IN_FIRE));
        } else {
            checks.add((event, query) -> query.getSource(event) == null ? true : !query.getSource(event).is(DamageTypes.IN_FIRE));
        }
    }

    private void addMagicCheck(boolean magic) {
        if (magic) {
            checks.add((event, query) -> query.getSource(event) == null ? false : query.getSource(event).is(DamageTypes.MAGIC));
        } else {
            checks.add((event, query) -> query.getSource(event) == null ? true : !query.getSource(event).is(DamageTypes.MAGIC));
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

    private void addAreaCheck(String areaName) {
        Area area = AreaSystem.getArea(areaName);
        if (area == null) {
            ErrorHandler.error("Cannot find area '" + areaName + "'!");
        } else {
            checks.add((event, query) -> {
                LevelAccessor world = query.getWorld(event);
                ResourceKey<Level> key = Tools.getDimensionKey(world);
                if (area.dimension() == key) {
                    BlockPos pos = query.getPos(event);
                    return area.isInArea(pos.getX(), pos.getY(), pos.getZ());
                }
                return false;
            });
        }
    }

    public boolean match(Object event, IEventQuery query) {
        for (BiFunction<Object, IEventQuery, Boolean> rule : checks) {
            if (!rule.apply(event, query)) {
                return false;
            }
        }
        return true;
    }
}
