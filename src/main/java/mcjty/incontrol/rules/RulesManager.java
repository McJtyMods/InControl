package mcjty.incontrol.rules;

import com.google.gson.*;
import mcjty.incontrol.InControl;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

    public static void setRulePath(File directory) {
        path = directory.getPath();
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
        File file;
        if (path == null) {
            file = new File(filename);
        } else {
            file = new File(path + File.separator + "incontrol", filename);
        }
        if (!file.exists()) {
            // Create an empty rule file
            makeEmptyRuleFile(file);
            return null;
        }

        InControl.setup.getLogger().log(Level.INFO, "Reading spawn rules from " + filename);
        InputStream inputstream = null;
        try {
            inputstream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            InControl.setup.getLogger().log(Level.ERROR, "Error reading " + filename + "!");
            return null;
        }

        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            InControl.setup.getLogger().log(Level.ERROR, "Error reading " + filename + "!");
            return null;
        }

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(br);

        return element;
    }

    private static void makeEmptyRuleFile(File file) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            InControl.setup.getLogger().log(Level.ERROR, "Error writing " + file.getName() + "!");
            return;
        }
        JsonArray array = new JsonArray();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        writer.print(gson.toJson(array));
        writer.close();
    }


}
