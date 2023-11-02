package mcjty.incontrol.events;

import com.google.gson.JsonElement;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import mcjty.incontrol.tools.varia.JSonTools;
import org.apache.logging.log4j.Level;

import java.nio.file.Path;

public class EventsParser {

    private static String path;

    public static void readRules(String filename) {
        JsonElement element = JSonTools.getRootElement(path, filename, InControl.setup.getLogger());
        if (element == null) {
            return;
        }
        for (JsonElement entry : element.getAsJsonArray()) {
            EventsRule.Builder builder = EventsRule.create();
            try {
                EventsRule.parse(entry.getAsJsonObject(), builder);
                EventsSystem.addRule(builder.build());
            } catch (Exception e) {
                ErrorHandler.error("JSON error in 'events.json': check log for details (" + e.getMessage() + ")");
                InControl.setup.getLogger().log(Level.ERROR, "Error parsing 'events.json'", e);
            }
        }
    }

    public static void setRulePath(Path path) {
        EventsParser.path = path.toString();
    }
}
