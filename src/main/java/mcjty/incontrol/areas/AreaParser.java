package mcjty.incontrol.areas;

import com.google.gson.JsonElement;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import mcjty.incontrol.tools.varia.JSonTools;
import org.apache.logging.log4j.Level;

import java.nio.file.Path;

public class AreaParser {

    private static String path;

    public static void readRules(String filename) {
        JsonElement element = JSonTools.getRootElement(path, filename, InControl.setup.getLogger());
        if (element == null) {
            return;
        }
        for (JsonElement entry : element.getAsJsonArray()) {
            Area.Builder builder = Area.create();
            try {
                if (!Area.parse(entry.getAsJsonObject(), builder)) {
                    continue;
                }
                AreaSystem.addArea(builder.build());
            } catch (Exception e) {
                ErrorHandler.error("JSON error in 'areas.json': check log for details (" + e.getMessage() + ")");
                InControl.setup.getLogger().log(Level.ERROR, "Error parsing 'areas.json'", e);
            }
        }
    }

    public static void setRulePath(Path path) {
        AreaParser.path = path.toString();
    }
}
