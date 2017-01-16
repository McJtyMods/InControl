package mcjty.incontrol.rules;

import com.google.gson.*;
import mcjty.incontrol.InControl;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SpawnRules {

    private static String path;
    public static List<SpawnRule> rules = new ArrayList<>();

    public static void reloadRuiles() {
        rules.clear();
        readRules();
    }

    public static void readRules(File directory) {
        path = directory.getPath();
        readRules();
    }

    private static void readRules() {
        File file = new File(path + File.separator + "incontrol", "spawn.json");
        if (!file.exists()) {
            // Create an empty rule file
            makeEmptyRuleFile(file);
            return;
        }

        InControl.logger.log(Level.INFO, "Reading spawn rules from spawn.json");
        InputStream inputstream = null;
        try {
            inputstream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            InControl.logger.log(Level.ERROR, "Error reading spawn.json!");
            return;
        }

        readRulesFromFile(inputstream);
    }

    private static void makeEmptyRuleFile(File file) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            InControl.logger.log(Level.ERROR, "Error writing spawn.json!");
            return;
        }
        JsonArray array = new JsonArray();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        writer.print(gson.toJson(array));
        writer.close();
    }

    private static void readRulesFromFile(InputStream inputstream) {
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            InControl.logger.log(Level.ERROR, "Error reading spawn.json!");
            return;
        }
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(br);
        for (JsonElement entry : element.getAsJsonArray()) {
            SpawnRule rule = SpawnRule.parse(entry);
            rules.add(rule);
        }
    }

}
