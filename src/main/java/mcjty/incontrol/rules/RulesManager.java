package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import mcjty.incontrol.data.DataStorage;
import mcjty.tools.varia.JSonTools;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RulesManager {

    private static List<SpawnRule> rules = new ArrayList<>();
    private static List<SpawnRule> filteredRules = null;

    private static List<SummonAidRule> summonAidRules = new ArrayList<>();
    private static List<SummonAidRule> filteredSummonAidRules = null;

    private static List<LootRule> lootRules = new ArrayList<>();
    private static List<LootRule> filteredLootRules = null;

    private static List<ExperienceRule> experienceRules = new ArrayList<>();
    private static List<ExperienceRule> filteredExperienceRuiles = null;

    public static List<PotentialSpawnRule> potentialSpawnRules = new ArrayList<>();
    public static List<PhaseRule> phaseRules = new ArrayList<>();
    private static String path;

    public static void reloadRules() {
        rules.clear();
        summonAidRules.clear();
        potentialSpawnRules.clear();
        lootRules.clear();
        experienceRules.clear();
        phaseRules.clear();
        onPhaseChange();
        readAllRules();
    }

    public static void setRulePath(Path path) {
        RulesManager.path = path.toString();
    }

    public static void readRules() {
        readAllRules();
    }

    public static void onPhaseChange() {
        filteredRules = null;
        filteredSummonAidRules = null;
        filteredLootRules = null;
        filteredExperienceRuiles = null;
    }

    public static List<SpawnRule> getFilteredRules(World world) {
        if (filteredRules == null) {
            Set<String> phases = DataStorage.getData(world).getPhases();
            filteredRules = rules.stream().filter(r -> phases.containsAll(r.getPhases())).collect(Collectors.toList());
        }
        return filteredRules;
    }

    public static List<SummonAidRule> getFilteredSummonAidRules(World world) {
        if (filteredSummonAidRules == null) {
            Set<String> phases = DataStorage.getData(world).getPhases();
            filteredSummonAidRules = summonAidRules.stream().filter(r -> phases.containsAll(r.getPhases())).collect(Collectors.toList());
        }
        return filteredSummonAidRules;
    }

    public static List<LootRule> getFilteredLootRules(World world) {
        if (filteredLootRules == null) {
            Set<String> phases = DataStorage.getData(world).getPhases();
            filteredLootRules = lootRules.stream().filter(r -> phases.containsAll(r.getPhases())).collect(Collectors.toList());
        }
        return filteredLootRules;
    }

    public static List<ExperienceRule> getFilteredExperienceRuiles(World world) {
        if (filteredExperienceRuiles == null) {
            Set<String> phases = DataStorage.getData(world).getPhases();
            filteredExperienceRuiles = experienceRules.stream().filter(r -> phases.containsAll(r.getPhases())).collect(Collectors.toList());
        }
        return filteredExperienceRuiles;
    }

    private static boolean exists(String file) {
        File f = new File(file);
        return f.exists() && !f.isDirectory();
    }

    public static boolean readCustomSpawn(String file) {
        System.out.println("file = " + file);
        if (!exists(file)) {
            return false;
        }
        rules.clear();
        readRules(null, file, SpawnRule::parse, rules);
        return true;
    }

    public static boolean readCustomSummonAid(String file) {
        if (!exists(file)) {
            return false;
        }
        summonAidRules.clear();
        readRules(null, file, SummonAidRule::parse, summonAidRules);
        return true;
    }

    public static boolean readCustomPotentialSpawn(String file) {
        if (!exists(file)) {
            return false;
        }
        potentialSpawnRules.clear();
        readRules(null, file, PotentialSpawnRule::parse, potentialSpawnRules);
        return true;
    }

    public static boolean readCustomLoot(String file) {
        if (!exists(file)) {
            return false;
        }
        lootRules.clear();
        readRules(null, file, LootRule::parse, lootRules);
        return true;
    }

    private static void readAllRules() {
        File directory = new File(path + File.separator + "incontrol");
        if (!directory.exists()) {
            directory.mkdir();
        }

        safeCall("spawn.json", () -> readRules(path, "spawn.json", SpawnRule::parse, rules));
        safeCall("summonaid.json", () -> readRules(path, "summonaid.json", SummonAidRule::parse, summonAidRules));
        safeCall("potentialspawn.json", () -> readRules(path, "potentialspawn.json", PotentialSpawnRule::parse, potentialSpawnRules));
        safeCall("loot.json", () -> readRules(path, "loot.json", LootRule::parse, lootRules));
        safeCall("experience.json", () -> readRules(path, "experience.json", ExperienceRule::parse, experienceRules));
        safeCall("phases.json", () -> readRules(path, "phases.json", PhaseRule::parse, phaseRules));
    }

    private static void safeCall(String name, Runnable code) {
        try {
            code.run();
        } catch (Exception e) {
            ErrorHandler.error("JSON error in '" + name + "': check log for details (" + e.getMessage() + ")");
            InControl.setup.getLogger().log(Level.ERROR, "Error parsing '" + name + "'", e);
        }
    }

    private static <T> void readRules(String path, String filename, Function<JsonElement, T> parser, List<T> rules) {
        JsonElement element = getRootElement(path, filename);
        if (element == null) {
            return;
        }
        int i = 0;
        for (JsonElement entry : element.getAsJsonArray()) {
            T rule = parser.apply(entry);
            if (rule != null) {
                rules.add(rule);
            } else {
                InControl.setup.getLogger().log(Level.ERROR, "Rule " + i + " in " + filename + " is invalid, skipping!");
            }
            i++;
        }
    }

    private static JsonElement getRootElement(String path, String filename) {
        return JSonTools.getRootElement(path, filename, InControl.setup.getLogger());
    }
}
