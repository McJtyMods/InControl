package mcjty.incontrol.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

/** Loader-independent contexts passed to the existing rule engine. */
public final class RuleEvents {
    private RuleEvents() {}

    public enum TriState { TRUE, FALSE, DEFAULT }
    public static class Cancellable {
        private boolean canceled;
        public boolean isCanceled() { return canceled; }
        public void setCanceled(boolean canceled) { this.canceled = canceled; }
    }
    public static class EntityJoinLevelEvent extends Cancellable {
        private final Entity entity;
        private final Level level;
        private final boolean loadedFromDisk;
        public EntityJoinLevelEvent(Entity entity, Level level) { this(entity, level, false); }
        public EntityJoinLevelEvent(Entity entity, Level level, boolean loadedFromDisk) {
            this.entity = entity; this.level = level; this.loadedFromDisk = loadedFromDisk;
        }
        public Entity getEntity() { return entity; }
        public Level getLevel() { return level; }
        public boolean loadedFromDisk() { return loadedFromDisk; }
    }
    public static class EntityLeaveLevelEvent extends EntityJoinLevelEvent {
        public EntityLeaveLevelEvent(Entity entity, Level level) { super(entity, level); }
    }
    public static class MobSpawnEvent extends Cancellable {
        private final Mob entity;
        private final ServerLevelAccessor level;
        private final MobSpawnType spawnType;
        public MobSpawnEvent(Mob entity, ServerLevelAccessor level, MobSpawnType spawnType) {
            this.entity = entity; this.level = level; this.spawnType = spawnType;
        }
        public Mob getEntity() { return entity; }
        public ServerLevelAccessor getLevel() { return level; }
        public MobSpawnType getSpawnType() { return spawnType; }
        public double getX() { return entity.getX(); }
        public double getY() { return entity.getY(); }
        public double getZ() { return entity.getZ(); }
        public static class PositionCheck extends MobSpawnEvent {
            public enum Result { DEFAULT, SUCCEED, FAIL }
            private Result result = Result.DEFAULT;
            public PositionCheck(Mob mob, ServerLevelAccessor level, MobSpawnType type) { super(mob, level, type); }
            public Result getResult() { return result; }
            public void setResult(Result result) { this.result = result; }
        }
        public static class SpawnPlacementCheck {
            public enum Result { DEFAULT, SUCCEED, FAIL }
        }
    }
    public static class FinalizeSpawnEvent extends MobSpawnEvent {
        private boolean spawnCancelled;
        public FinalizeSpawnEvent(Mob mob, ServerLevelAccessor level, MobSpawnType type) { super(mob, level, type); }
        public void setSpawnCancelled(boolean value) { spawnCancelled = value; }
        public boolean isSpawnCancelled() { return spawnCancelled; }
    }
    public static class MobDespawnEvent {
        public enum Result { DEFAULT, ALLOW, DENY }
        private final Mob entity;
        private Result result = Result.DEFAULT;
        public MobDespawnEvent(Mob entity) { this.entity = entity; }
        public Mob getEntity() { return entity; }
        public Level getLevel() { return entity.level(); }
        public double getX() { return entity.getX(); }
        public double getY() { return entity.getY(); }
        public double getZ() { return entity.getZ(); }
        public Result getResult() { return result; }
        public void setResult(Result result) { this.result = result; }
    }
    public static class LivingExperienceDropEvent extends Cancellable {
        private final LivingEntity entity;
        private final Player player;
        private int experience;
        public LivingExperienceDropEvent(LivingEntity entity, Player player, int experience) {
            this.entity = entity; this.player = player; this.experience = experience;
        }
        public LivingEntity getEntity() { return entity; }
        public Player getAttackingPlayer() { return player; }
        public int getDroppedExperience() { return experience; }
        public void setDroppedExperience(int value) { experience = value; }
    }
    public record LivingDeathEvent(LivingEntity entity, DamageSource source) {
        public LivingEntity getEntity() { return entity; }
        public DamageSource getSource() { return source; }
    }
    public static class PlayerInteractEvent extends Cancellable {
        private final Player entity;
        private final BlockPos pos;
        private final InteractionHand hand;
        private TriState useBlock = TriState.DEFAULT;
        public PlayerInteractEvent(Player player, BlockPos pos, InteractionHand hand) {
            entity = player; this.pos = pos; this.hand = hand;
        }
        public Player getEntity() { return entity; }
        public Level getLevel() { return entity.level(); }
        public BlockPos getPos() { return pos; }
        public ItemStack getItemStack() { return entity.getItemInHand(hand); }
        public TriState getUseBlock() { return useBlock; }
        public void setUseBlock(TriState value) { useBlock = value; }
        public static class RightClickBlock extends PlayerInteractEvent {
            public RightClickBlock(Player player, BlockPos pos, InteractionHand hand) { super(player, pos, hand); }
        }
        public static class LeftClickBlock extends PlayerInteractEvent {
            public LeftClickBlock(Player player, BlockPos pos) { super(player, pos, InteractionHand.MAIN_HAND); }
        }
    }
    public static class BlockEvent extends Cancellable {
        private final LevelAccessor level;
        private final BlockPos pos;
        public BlockEvent(LevelAccessor level, BlockPos pos) { this.level = level; this.pos = pos; }
        public LevelAccessor getLevel() { return level; }
        public BlockPos getPos() { return pos; }
        public static class EntityPlaceEvent extends BlockEvent {
            private final Entity entity;
            public EntityPlaceEvent(LevelAccessor level, BlockPos pos, Entity entity) { super(level, pos); this.entity = entity; }
            public Entity getEntity() { return entity; }
        }
        public static class BreakEvent extends BlockEvent {
            private final Player player;
            public BreakEvent(LevelAccessor level, BlockPos pos, Player player) { super(level, pos); this.player = player; }
            public Player getPlayer() { return player; }
        }
    }
    public static class LevelTickEvent {
        public record Pre(Level level) { public Level getLevel() { return level; } }
    }
    public static class PlayerTickEvent {
        private final Player player;
        public PlayerTickEvent(Player player) { this.player = player; }
        public Player getEntity() { return player; }
        public static class Post extends PlayerTickEvent {
            public Post(Player player) { super(player); }
        }
    }
}
