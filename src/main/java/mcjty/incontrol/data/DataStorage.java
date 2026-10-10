package mcjty.incontrol.data;

import com.mojang.serialization.Codec;
import mcjty.incontrol.InControl;
import mcjty.incontrol.rules.PhaseRule;
import mcjty.incontrol.rules.RulesManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.SavedDataStorage;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataStorage extends SavedData {

    static final Codec<DataStorage> CODEC = CompoundTag.CODEC.xmap(DataStorage::new, DataStorage::save);

    private static final SavedDataType<DataStorage> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(InControl.MODID, "data"),
            DataStorage::new, CODEC);

    private Boolean isDay = null;
    private int daycounter = 0;
    private final Set<String> phases = new HashSet<>();
    private final Map<String, Integer> numbers = new HashMap<>();

    private int checkCounter = 0;   // We only check every X ticks for efficiency

    public DataStorage() {
    }

    public DataStorage(CompoundTag tag) {
        daycounter = tag.getIntOr("daycounter", 0);
        if (tag.contains("isday")) {
            isDay = tag.getBooleanOr("isday", false);
        } else {
            isDay = null;
        }
        ListTag phasesTag = tag.getListOrEmpty("phases");
        for (int i = 0 ; i < phasesTag.size() ; i++) {
            phases.add(phasesTag.getStringOr(i, ""));
        }
        CompoundTag numbersTag = tag.getCompoundOrEmpty("numbers");
        for (String key : numbersTag.keySet()) {
            numbers.put(key, numbersTag.getIntOr(key, 0));
        }
    }

    @Nonnull
    public static DataStorage getData(LevelAccessor world) {
        if (world.isClientSide()) {
            throw new RuntimeException("Don't access this client-side!");
        }
        MinecraftServer server = world.getServer();
        SavedDataStorage storage = server.getDataStorage();
        DataStorage data = storage.get(TYPE);
        if (data == null) {
            // Older versions stored this in the overworld under an unnamespaced name.
            Path legacyFile = server.getWorldPath(LevelResource.ROOT).resolve("data/InControlData.dat");
            data = readLegacyData(legacyFile);
            storage.set(TYPE, data);
        }
        return data;
    }

    static DataStorage readLegacyData(Path legacyFile) {
        if (!Files.isRegularFile(legacyFile)) {
            return new DataStorage();
        }
        try {
            CompoundTag tag = NbtIo.readCompressed(legacyFile, NbtAccounter.unlimitedHeap());
            DataStorage data = new DataStorage(tag.getCompoundOrEmpty("data"));
            data.setDirty();
            return data;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read legacy In Control saved data", e);
        }
    }

    public int getDaycounter() {
        return daycounter;
    }

    public void setDaycounter(int daycounter) {
        this.daycounter = daycounter;
        setDirty();
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
        long time = world.getOverworldClockTime() % 24000;
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

    public Map<String, Integer> getNumbers() {
        return numbers;
    }

    public void setNumber(String name, int value) {
        numbers.put(name, value);
        setDirty();
    }

    public int getNumber(String name) {
        return numbers.getOrDefault(name, 0);
    }

    public Set<String> getPhases() {
        return phases;
    }

    public void setPhase(String phase, boolean value) {
        if (value) {
            if (phases.add(phase)) {
                RulesManager.onPhaseChange();
                setDirty();
            }
        } else {
            if (phases.remove(phase)) {
                RulesManager.onPhaseChange();
                setDirty();
            }
        }
    }

    public void togglePhase(String phase) {
        if (phases.contains(phase)) {
            phases.remove(phase);
        } else {
            phases.add(phase);
        }
        RulesManager.onPhaseChange();
        setDirty();
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
            setDirty();
        }
    }

    private CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("daycounter", daycounter);
        if (isDay != null) {
            tag.putBoolean("isday", isDay);
        }
        ListTag phasesTag = new ListTag();
        for (String phase : phases) {
            phasesTag.add(StringTag.valueOf(phase));
        }
        tag.put("phases", phasesTag);
        CompoundTag numbersTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : numbers.entrySet()) {
            numbersTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("numbers", numbersTag);
        return tag;
    }
}
