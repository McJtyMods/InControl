package mcjty.incontrol.spawner;

import com.google.gson.JsonElement;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import mcjty.incontrol.tools.varia.JSonTools;
import org.apache.logging.log4j.Level;

import java.nio.file.Path;

public class SpawnerParser {

    private static String path;

    public static void readRules(String filename) {
        JsonElement element = JSonTools.getRootElement(path, filename, InControl.setup.getLogger());
        if (element == null) {
            return;
        }
        for (JsonElement entry : element.getAsJsonArray()) {
            SpawnerRule.Builder builder = SpawnerRule.create();
            try {
                SpawnerRule.parse(entry.getAsJsonObject(), builder);
                SpawnerSystem.addRule(builder.build());
            } catch (Exception e) {
                ErrorHandler.error("JSON error in 'spawner.json': check log for details (" + e.getMessage() + ")");
                InControl.setup.getLogger().log(Level.ERROR, "Error parsing 'spawner.json'", e);
            }
        }
    }

    public static void setRulePath(Path path) {
        SpawnerParser.path = path.toString();
    }
}
