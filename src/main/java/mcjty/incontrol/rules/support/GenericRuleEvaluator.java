package mcjty.incontrol.rules.support;

import mcjty.incontrol.InControl;
import mcjty.incontrol.compat.ModRuleCompatibilityLayer;
import mcjty.incontrol.rules.PotentialSpawnRule;
import mcjty.tools.rules.CommonRuleEvaluator;
import mcjty.tools.rules.IEventQuery;
import mcjty.tools.typed.AttributeMap;
import mcjty.tools.varia.Tools;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import static mcjty.incontrol.rules.support.RuleKeys.*;


public class GenericRuleEvaluator extends CommonRuleEvaluator {

    public GenericRuleEvaluator(AttributeMap map) {
        super(map, InControl.setup.getLogger(), new ModRuleCompatibilityLayer());
    }

    @Override
    protected void addChecks(AttributeMap map) {
        super.addChecks(map);

        if (map.has(HOSTILE)) {
            addHostileCheck(map);
        }
        if (map.has(PASSIVE)) {
            addPassiveCheck(map);
        }
        if (map.has(CANSPAWNHERE)) {
            addCanSpawnHereCheck(map);
        }
        if (map.has(NOTCOLLIDING)) {
            addNotCollidingCheck(map);
        }
        if (map.has(SPAWNER)) {
            addSpawnerCheck(map);
        }
        if (map.has(MOB)) {
            addMobsCheck(map);
        }
        if (map.has(PLAYER)) {
            addPlayerCheck(map);
        }
        if (map.has(REALPLAYER)) {
            addRealPlayerCheck(map);
        }
        if (map.has(FAKEPLAYER)) {
            addFakePlayerCheck(map);
        }
        if (map.has(EXPLOSION)) {
            addExplosionCheck(map);
        }
        if (map.has(PROJECTILE)) {
            addProjectileCheck(map);
        }
        if (map.has(FIRE)) {
            addFireCheck(map);
        }
        if (map.has(MAGIC)) {
            addMagicCheck(map);
        }
        if (map.has(SOURCE)) {
            addSourceCheck(map);
        }
        if (map.has(MOD)) {
            addModsCheck(map);
        }
        if (map.has(MINCHUNKCOUNT)) {
            addMinCountChunkCheck(map);
        }
        if (map.has(MAXCHUNKCOUNT)) {
            addMaxCountChunkCheck(map);
        }
        if (map.has(MINCOUNT)) {
            addMinCountCheck(map);
        }
        if (map.has(MAXCOUNT)) {
            addMaxCountCheck(map);
        }
        if (map.has(RANDOM)) {
            addRandomCheck(map);
        }
        if (map.has(PLAYERHEIGHTCHECK)) {
            addPlayerHeightRelativeCheck(map);
        }
        if (map.has(BLOCKBURNING)) {
            addBurningBlockCheck(map);
        }
    }

    private void addCanSpawnHereCheck(AttributeMap map) {
        boolean c = map.get(CANSPAWNHERE);
        if (c) {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof EntityLiving) {
                    return ((EntityLiving) entity).getCanSpawnHere();
                } else {
                    return false;
                }
            });
        } else {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof EntityLiving) {
                    return !((EntityLiving) entity).getCanSpawnHere();
                } else {
                    return true;
                }
            });
        }
    }

    private void addNotCollidingCheck(AttributeMap map) {
        boolean c = map.get(NOTCOLLIDING);
        if (c) {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof EntityLiving) {
                    return ((EntityLiving) entity).isNotColliding();
                } else {
                    return false;
                }
            });
        } else {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if (entity instanceof EntityLiving) {
                    return !((EntityLiving) entity).isNotColliding();
                } else {
                    return true;
                }
            });
        }
    }

    private void addSpawnerCheck(AttributeMap map) {
        boolean c = map.get(SPAWNER);
        if (c) {
            checks.add((event, query) -> {
                if (event instanceof LivingSpawnEvent.CheckSpawn) {
                    LivingSpawnEvent.CheckSpawn checkSpawn = (LivingSpawnEvent.CheckSpawn) event;
                    return checkSpawn.isSpawner();
                } else {
                    return false;
                }
            });
        } else {
            checks.add((event, query) -> {
                if (event instanceof LivingSpawnEvent.CheckSpawn) {
                    LivingSpawnEvent.CheckSpawn checkSpawn = (LivingSpawnEvent.CheckSpawn) event;
                    return !checkSpawn.isSpawner();
                } else {
                    return false;
                }
            });
        }
    }

    private void addHostileCheck(AttributeMap map) {
        if (map.get(HOSTILE)) {
            checks.add((event, query) -> query.getEntity(event) instanceof IMob);
        } else {
            checks.add((event, query) -> !(query.getEntity(event) instanceof IMob));
        }
    }

    private void addPassiveCheck(AttributeMap map) {
        if (map.get(PASSIVE)) {
            checks.add((event, query) -> (query.getEntity(event) instanceof IAnimals && !(query.getEntity(event) instanceof IMob)));
        } else {
            checks.add((event, query) -> !(query.getEntity(event) instanceof IAnimals && !(query.getEntity(event) instanceof IMob)));
        }
    }

    private void addMobsCheck(AttributeMap map) {
        List<String> mobs = map.getList(MOB);
        if (mobs.size() == 1) {
            String name = mobs.get(0);
            String id = PotentialSpawnRule.fixEntityId(name);
            EntityEntry ee = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
            Class<? extends Entity> clazz = ee == null ? null : ee.getEntityClass();
            if (clazz != null) {
                checks.add((event, query) -> clazz.equals(query.getEntity(event).getClass()));
            } else {
                InControl.setup.getLogger().log(Level.ERROR, "Unknown mob '" + name + "'!");
            }
        } else {
            Set<Class> classes = new HashSet<>();
            for (String name : mobs) {
                String id = PotentialSpawnRule.fixEntityId(name);
                EntityEntry ee = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
                Class<? extends Entity> clazz = ee == null ? null : ee.getEntityClass();
                if (clazz != null) {
                    classes.add(clazz);
                } else {
                    InControl.setup.getLogger().log(Level.ERROR, "Unknown mob '" + name + "'!");
                }
            }
            if (!classes.isEmpty()) {
                checks.add((event, query) -> classes.contains(query.getEntity(event).getClass()));
            }
        }
    }

    private void addModsCheck(AttributeMap map) {
        List<String> mods = map.getList(MOD);
        if (mods.size() == 1) {
            String modid = mods.get(0);
            checks.add((event, query) -> {
                String id = Tools.findModID(query.getEntity(event));
                return modid.equals(id);
            });
        } else {
            Set<String> modids = new HashSet<>(mods);
            checks.add((event, query) -> {
                String id = Tools.findModID(query.getEntity(event));
                return modids.contains(id);
            });
        }
    }

    private void addMinCountChunkCheck(AttributeMap map) {
        final String minchunkcount = map.get(MINCHUNKCOUNT);
        String[] splitted = StringUtils.split(minchunkcount, ',');
        Class<?> entityClass = null;
        int amount;
        try {
            amount = Integer.parseInt(splitted[0]);
        } catch (NumberFormatException e) {
            InControl.setup.getLogger().log(Level.ERROR, "Bad amount for minchunkcount '" + splitted[0] + "'!");
            return;
        }
        if (splitted.length > 1) {
            String id = PotentialSpawnRule.fixEntityId(splitted[1]);
            EntityEntry ee = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
            entityClass = ee == null ? null : ee.getEntityClass();
            if (entityClass == null) {
                InControl.setup.getLogger().log(Level.ERROR, "Unknown mob '" + splitted[1] + "'!");
                return;
            }
        }

        Class<?> finalEntityClass = entityClass;
        checks.add((event, query) -> {
            Chunk chunk = query.getWorld(event).getChunk(query.getPos(event));
            int count = 0;

            for (ClassInheritanceMultiMap<Entity> entity : chunk.getEntityLists()) {
                if (entity.getClass() != (finalEntityClass == null ? query.getEntity(event).getClass() : finalEntityClass)) {
                    continue;
                }

                count++;

                if (count >= amount) {
                    return true;
                }
            }

            return false;
        });
    }

    private void addMaxCountChunkCheck(AttributeMap map) {
        final String maxchunkcount = map.get(MAXCHUNKCOUNT);
        String[] splitted = StringUtils.split(maxchunkcount, ',');
        Class<?> entityClass = null;
        int amount;
        try {
            amount = Integer.parseInt(splitted[0]);
        } catch (NumberFormatException e) {
            InControl.setup.getLogger().log(Level.ERROR, "Bad amount for maxchunkcount '" + splitted[0] + "'!");
            return;
        }
        if (splitted.length > 1) {
            String id = PotentialSpawnRule.fixEntityId(splitted[1]);
            EntityEntry ee = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
            entityClass = ee == null ? null : ee.getEntityClass();
            if (entityClass == null) {
                InControl.setup.getLogger().log(Level.ERROR, "Unknown mob '" + splitted[1] + "'!");
                return;
            }
        }

        Class<?> finalEntityClass = entityClass;
        checks.add((event, query) -> {
            Chunk chunk = query.getWorld(event).getChunk(query.getPos(event));
            int count = 0;

            for (ClassInheritanceMultiMap<Entity> entity : chunk.getEntityLists()) {
                if (entity.getClass() != (finalEntityClass == null ? query.getEntity(event).getClass() : finalEntityClass)) {
                    continue;
                }

                count++;

                if (count >= amount) {
                    return false;
                }
            }

            return count < amount;
        });
    }

    private void addMinCountCheck(AttributeMap map) {
        final String mincount = map.get(MINCOUNT);
        String[] splitted = StringUtils.split(mincount, ',');
        Class<?> entityClass = null;
        int amount;
        try {
            amount = Integer.parseInt(splitted[0]);
        } catch (NumberFormatException e) {
            InControl.setup.getLogger().log(Level.ERROR, "Bad amount for mincount '" + splitted[0] + "'!");
            return;
        }
        if (splitted.length > 1) {
            String id = PotentialSpawnRule.fixEntityId(splitted[1]);
            EntityEntry ee = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
            entityClass = ee == null ? null : ee.getEntityClass();
            if (entityClass == null) {
                InControl.setup.getLogger().log(Level.ERROR, "Unknown mob '" + splitted[1] + "'!");
                return;
            }
        }

        Class<?> finalEntityClass = entityClass;
        checks.add((event, query) -> {
            int count = query.getWorld(event).countEntities(finalEntityClass == null ? query.getEntity(event).getClass() : finalEntityClass);
            return count >= amount;
        });
    }

    private void addMaxCountCheck(AttributeMap map) {
        final String maxcount = map.get(MAXCOUNT);
        String[] splitted = StringUtils.split(maxcount, ',');
        Class<?> entityClass = null;
        int amount;
        try {
            amount = Integer.parseInt(splitted[0]);
        } catch (NumberFormatException e) {
            InControl.setup.getLogger().log(Level.ERROR, "Bad amount for maxcount '" + splitted[0] + "'!");
            return;
        }
        if (splitted.length > 1) {
            String id = PotentialSpawnRule.fixEntityId(splitted[1]);
            EntityEntry ee = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
            entityClass = ee == null ? null : ee.getEntityClass();
            if (entityClass == null) {
                InControl.setup.getLogger().log(Level.ERROR, "Unknown mob '" + splitted[1] + "'!");
                return;
            }
        }

        Class<?> finalEntityClass = entityClass;
        checks.add((event, query) -> {
            int count = query.getWorld(event).countEntities(finalEntityClass == null ? query.getEntity(event).getClass() : finalEntityClass);
            return count < amount;
        });
    }


    private void addPlayerCheck(AttributeMap map) {
        boolean asPlayer = map.get(PLAYER);
        if (asPlayer) {
            checks.add((event, query) -> query.getAttacker(event) instanceof EntityPlayer);
        } else {
            checks.add((event, query) -> query.getAttacker(event) instanceof EntityPlayer);
        }
    }


    private boolean isFakePlayer(Entity entity) {
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }

        if (entity instanceof FakePlayer) {
            return true;
        }

        // If this returns false it is still possible we have a fake player. Try to find the player in the list of online players
        PlayerList playerList = Objects.requireNonNull(DimensionManager.getWorld(0).getMinecraftServer()).getPlayerList();
        EntityPlayerMP playerByUUID = playerList.getPlayerByUUID(((EntityPlayer) entity).getGameProfile().getId());

        // The player is in the list. But is it this player?
        return entity != playerByUUID;
    }

    private boolean isRealPlayer(Entity entity) {
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }
        return !isFakePlayer(entity);
    }

    private void addRealPlayerCheck(AttributeMap map) {
        boolean asPlayer = map.get(REALPLAYER);
        if (asPlayer) {
            checks.add((event, query) -> query.getAttacker(event) != null && isRealPlayer(query.getAttacker(event)));
        } else {
            checks.add((event, query) -> query.getAttacker(event) == null || !isRealPlayer(query.getAttacker(event)));
        }
    }

    private void addFakePlayerCheck(AttributeMap map) {
        boolean asPlayer = map.get(FAKEPLAYER);
        if (asPlayer) {
            checks.add((event, query) -> query.getAttacker(event) != null && isFakePlayer(query.getAttacker(event)));
        } else {
            checks.add((event, query) -> query.getAttacker(event) == null || !isFakePlayer(query.getAttacker(event)));
        }
    }

    private void addExplosionCheck(AttributeMap map) {
        boolean explosion = map.get(EXPLOSION);
        if (explosion) {
            checks.add((event, query) -> query.getSource(event) != null && query.getSource(event).isExplosion());
        } else {
            checks.add((event, query) -> query.getSource(event) == null || !query.getSource(event).isExplosion());
        }
    }

    private void addProjectileCheck(AttributeMap map) {
        boolean projectile = map.get(PROJECTILE);
        if (projectile) {
            checks.add((event, query) -> query.getSource(event) != null && query.getSource(event).isProjectile());
        } else {
            checks.add((event, query) -> query.getSource(event) == null || !query.getSource(event).isProjectile());
        }
    }

    private void addFireCheck(AttributeMap map) {
        boolean fire = map.get(FIRE);
        if (fire) {
            checks.add((event, query) -> query.getSource(event) != null && query.getSource(event).isFireDamage());
        } else {
            checks.add((event, query) -> query.getSource(event) == null || !query.getSource(event).isFireDamage());
        }
    }

    private void addMagicCheck(AttributeMap map) {
        boolean magic = map.get(MAGIC);
        if (magic) {
            checks.add((event, query) -> query.getSource(event) != null && query.getSource(event).isMagicDamage());
        } else {
            checks.add((event, query) -> query.getSource(event) == null || !query.getSource(event).isMagicDamage());
        }
    }

    private void addSourceCheck(AttributeMap map) {
        List<String> sources = map.getList(SOURCE);
        Set<String> sourceSet = new HashSet<>(sources);
        checks.add((event, query) -> {
            if (query.getSource(event) == null) {
                return false;
            }
            return sourceSet.contains(query.getSource(event).getDamageType());
        });
    }

    private void addRandomCheck(AttributeMap map) {
        float random = map.get(RANDOM);
        checks.add((event, query) -> query.getWorld(event).rand.nextFloat() <= random);
    }

    private void addPlayerHeightRelativeCheck(AttributeMap map) {
        checks.add((event, query) -> {
            if (!map.has(PLAYERHEIGHTCHECK) || !map.get(PLAYERHEIGHTCHECK)) {
                return true;
            }

            BlockPos eventPos = query.getPos(event);

            if (eventPos == null) {
                return false;
            }

            float eventY = eventPos.getY();

            int phUp = 12;
            int phDown = 12;
            float phDistance = 24.0f;

            if (map.has(PHMAXDIFFUP)) {
                phUp = map.get(PHMAXDIFFUP);
            }

            if (map.has(PHMAXDIFFDOWN)) {
                phUp = map.get(PHMAXDIFFDOWN);
            }

            if (map.has(PHMAXDIST)) {
                phDistance = map.get(PHMAXDIST);
            }

            EntityPlayer closestPlayer = query.getWorld(event).getClosestPlayer(
                    (double) eventPos.getX(), (double) eventPos.getY(), (double) eventPos.getZ(), (double) phDistance, false);

            if (closestPlayer == null) {
                return false;
            }

            float playerBlockHeight = closestPlayer.getPosition().getY();
            float maxSpawnDistanceUp = playerBlockHeight + phUp;
            float maxSpawnDistanceDown = playerBlockHeight - phDown;

            return !(eventY > maxSpawnDistanceUp) && !(eventY < maxSpawnDistanceDown);
        });
    }

    private void addBurningBlockCheck(AttributeMap map) {
        checks.add((event, query) -> {
            if (!map.has(BLOCKBURNING) || !map.get(BLOCKBURNING)) {
                return true;
            }

            BlockPos eventPos = query.getPos(event);

            if (eventPos == null) {
                return false;
            }

            Block eventBlock = query.getWorld(event).getBlockState(eventPos).getBlock();

            BlockPos upOne = eventPos.up();
            Block upBlock;

            if (eventBlock.equals(Blocks.FIRE)) {
                return true;
            }

            upBlock = query.getWorld(event).getBlockState(upOne).getBlock();
            return upBlock.equals(Blocks.FIRE);

        });
    }

    @Override
    public boolean match(Event event, IEventQuery query) {
        for (BiFunction<Event, IEventQuery, Boolean> rule : checks) {
            if (!rule.apply(event, query)) {
                return false;
            }
        }
        return true;
    }

}
