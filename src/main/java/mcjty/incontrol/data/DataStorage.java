package mcjty.incontrol.data;

import mcjty.incontrol.rules.PhaseRule;
import mcjty.incontrol.rules.RulesManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class DataStorage extends SavedData {

    private static final String NAME = "InControlData";

    private Boolean isDay = null;
    private int daycounter = 0;
    private final Set<String> phases = new HashSet<>();

    private int checkCounter = 0;   // We only check every X ticks for efficiency

    public DataStorage() {
    }

    public DataStorage(CompoundTag tag) {
        daycounter = tag.getInt("daycounter");
        if (tag.contains("isday")) {
            isDay = tag.getBoolean("isday");
        } else {
            isDay = null;
        }
    }

    @Nonnull
    public static DataStorage getData(Level world) {
        if (world.isClientSide()) {
            throw new RuntimeException("Don't access this client-side!");
        }
        MinecraftServer server = world.getServer();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);

        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(DataStorage::new, DataStorage::new, NAME);
    }

    public int getDaycounter() {
        return daycounter;
    }

    public void setDaycounter(int daycounter) {
        this.daycounter = daycounter;
        setDirty();
    }

    public Boolean getDay() {
        return isDay;
    }

    public void setDay(Boolean day) {
        isDay = day;
    }

    public Set<String> getPhases() {
        return phases;
    }

    public void tick(Level world) {
        tickTime(world);

        checkCounter--;
        if (checkCounter <= 0) {
            checkCounter = 10;
            tickPhases(world);
        }
    }

    private void tickTime(Level world) {
        long time = world.getDayTime() % 24000;
        boolean day = time >= 0 && time < 12000;
        if (isDay == null) {
            isDay = day;
            setDirty();
        } else {
            if (day != isDay) {
                if (day) {    // New day
                    daycounter++;
                }
                isDay = day;
                setDirty();
            }
        }
    }

    private void tickPhases(Level world) {
        boolean dirty = false;
        for (PhaseRule rule : RulesManager.phaseRules) {
            if (rule.match(world)) {
                if (phases.add(rule.getName())) {
                    dirty = true;
                }
            } else {
                if (phases.remove(rule.getName())) {
                    dirty = true;
                }
            }
        }
        if (dirty) {
            // We need to reevaluate the rules
            RulesManager.onPhaseChange();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("daycounter", daycounter);
        if (isDay != null) {
            tag.putBoolean("isday", isDay);
        }
        return tag;
    }
}
