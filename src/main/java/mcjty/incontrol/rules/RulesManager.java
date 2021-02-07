package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import mcjty.incontrol.InControl;
import mcjty.tools.varia.JSonTools;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RulesManager {

    public static List<SpawnRule> rules = new ArrayList<>();
    public static List<SummonAidRule> summonAidRules = new ArrayList<>();
    public static List<PotentialSpawnRule> potentialSpawnRules = new ArrayList<>();
    public static List<LootRule> lootRules = new ArrayList<>();
    public static List<ExperienceRule> experienceRules = new ArrayList<>();
    private static String path;

    public static void reloadRules() {
        rules.clear();
        summonAidRules.clear();
        potentialSpawnRules.clear();
        lootRules.clear();
        experienceRules.clear();
        readAllRules();
    }

    public static void setRulePath(Path path) {
        RulesManager.path = path.toString();
    }

    public static void readRules() {
        readAllRules();
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

        readRules(path, "spawn.json", SpawnRule::parse, rules);
        readRules(path, "summonaid.json", SummonAidRule::parse, summonAidRules);
        readRules(path, "potentialspawn.json", PotentialSpawnRule::parse, potentialSpawnRules);
        readRules(path, "loot.json", LootRule::parse, lootRules);
        readRules(path, "experience.json", ExperienceRule::parse, experienceRules);
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
