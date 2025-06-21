package mcjty.incontrol.events;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static mcjty.incontrol.rules.support.RuleKeys.ACTION_COMMAND;

public record CommandAction(List<String> commands) {

    @Nullable
    static CommandAction parse(JsonObject object) {
        JsonArray cmdArray = object.getAsJsonArray(ACTION_COMMAND.name());
        if (cmdArray == null) {
            // Valid
            return null;
        }
        List<String> commands = new ArrayList<>();
        for (JsonElement element : cmdArray) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                ErrorHandler.error("Invalid command for command action!");
                return null;
            }
            commands.add(element.getAsString());
        }
        return new CommandAction(commands);
    }
}
