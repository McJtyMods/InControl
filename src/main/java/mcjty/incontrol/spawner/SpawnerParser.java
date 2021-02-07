package mcjty.incontrol.spawner;

import com.google.gson.JsonElement;
import mcjty.incontrol.InControl;
import mcjty.tools.varia.JSonTools;

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
            SpawnerRule.parse(entry.getAsJsonObject(), builder);
            SpawnerSystem.addRule(builder.build());
        }
    }

    public static void setRulePath(Path path) {
        SpawnerParser.path = path.toString();
    }
}
