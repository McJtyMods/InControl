package mcjty.incontrol.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DataStorageTest {

    @TempDir
    Path directory;

    @Test
    void preservesLegacyFieldsThroughCodecRoundTrip() {
        CompoundTag original = legacyData();
        DataStorage data = DataStorage.CODEC.parse(NbtOps.INSTANCE, original).getOrThrow();
        assertEquals(42, data.getDaycounter());
        assertEquals(Set.of("night", "invasion"), data.getPhases());
        assertEquals(-3, data.getNumber("kills"));

        data.setDaycounter(43);
        data.setNumber("kills", 7);
        CompoundTag encoded = (CompoundTag) DataStorage.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        assertTrue(encoded.getBooleanOr("isday", false));
        DataStorage reloaded = DataStorage.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        assertEquals(43, reloaded.getDaycounter());
        assertEquals(7, reloaded.getNumber("kills"));
        assertEquals(data.getPhases(), reloaded.getPhases());
    }

    @Test
    void keepsUnknownDayStateAbsent() {
        DataStorage data = DataStorage.CODEC.parse(NbtOps.INSTANCE, new CompoundTag()).getOrThrow();
        CompoundTag encoded = (CompoundTag) DataStorage.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        assertFalse(encoded.contains("isday"));
        assertEquals(0, data.getDaycounter());
        assertEquals(0, data.getNumber("missing"));
        assertTrue(data.getPhases().isEmpty());
    }

    @Test
    void readsOldCompressedSaveAndMarksItForMigration() throws Exception {
        Path file = directory.resolve("InControlData.dat");
        CompoundTag root = new CompoundTag();
        root.put("data", legacyData());
        NbtIo.writeCompressed(root, file);

        DataStorage data = DataStorage.readLegacyData(file);
        assertEquals(42, data.getDaycounter());
        assertEquals(-3, data.getNumber("kills"));
        assertEquals(Set.of("night", "invasion"), data.getPhases());
        assertTrue(data.isDirty());
        assertTrue(Files.isRegularFile(file));
    }

    @Test
    void startsFreshWhenThereIsNoLegacySaveAndRejectsCorruptSaves() throws Exception {
        Path file = directory.resolve("InControlData.dat");
        assertEquals(0, DataStorage.readLegacyData(file).getDaycounter());
        Files.writeString(file, "corrupt data");
        assertThrows(IllegalStateException.class, () -> DataStorage.readLegacyData(file));
    }

    private static CompoundTag legacyData() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("daycounter", 42);
        tag.putBoolean("isday", true);
        ListTag phases = new ListTag();
        phases.add(StringTag.valueOf("night"));
        phases.add(StringTag.valueOf("invasion"));
        tag.put("phases", phases);
        CompoundTag numbers = new CompoundTag();
        numbers.putInt("kills", -3);
        tag.put("numbers", numbers);
        return tag;
    }
}
