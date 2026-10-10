package mcjty.incontrol.platform;

import mcjty.incontrol.InControl;
import mcjty.incontrol.platform.RuleEvents.MobSpawnEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;

public final class SpawnHooks {
    private static final ThreadLocal<java.util.Set<Mob>> FINALIZING = ThreadLocal.withInitial(() -> java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>()));
    private SpawnHooks() {}
    public static SpawnGroupData finalize(Mob mob, ServerLevelAccessor level, MobSpawnType type, SpawnGroupData data, java.util.function.Supplier<SpawnGroupData> original) {
        java.util.Set<Mob> active = FINALIZING.get();
        if (!active.add(mob)) return original.get();
        try {
            MobState state = (MobState) mob;
            state.incontrol$setSpawnType(type);
            RuleEvents.FinalizeSpawnEvent event = new RuleEvents.FinalizeSpawnEvent(mob, level, type);
            InControl.HANDLERS.onEntitySpawnEvent(event);
            state.incontrol$setSpawnCancelled(event.isSpawnCancelled());
            return event.isCanceled() ? data : original.get();
        } finally {
            active.remove(mob);
            if (active.isEmpty()) FINALIZING.remove();
        }
    }
    public static SpawnGroupData finalizeMobSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, SpawnGroupData data) {
        return mob.finalizeSpawn(level, difficulty, type, data);
    }
    public static boolean checkSpawnPosition(Mob mob, ServerLevelAccessor level, MobSpawnType type) {
        return position(mob, level, type, mob.checkSpawnRules(level, type) && mob.checkSpawnObstruction(level));
    }
    public static boolean position(Mob mob, ServerLevelAccessor level, MobSpawnType type, boolean vanilla) {
        ((MobState) mob).incontrol$setSpawnType(type);
        MobSpawnEvent.PositionCheck event = new MobSpawnEvent.PositionCheck(mob, level, type);
        InControl.HANDLERS.onPositionCheck(event);
        return switch (event.getResult()) {
            case SUCCEED -> true;
            case FAIL -> false;
            case DEFAULT -> vanilla;
        };
    }
}
