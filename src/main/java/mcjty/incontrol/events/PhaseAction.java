package mcjty.incontrol.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static mcjty.incontrol.rules.support.RuleKeys.PHASE;

public record PhaseAction(List<String> phases, boolean set) {

    @Nullable
    static PhaseAction parse(JsonObject object) {
        JsonObject value = object.getAsJsonObject(PHASE.name());
        if (value == null) {
            // Valid
            return null;
        }
        List<String> phases = new ArrayList<>();
        JsonElement names = value.get("names");
        if (names == null) {
            ErrorHandler.error("No names specified for phase action!");
            return null;
        }
        if (names.isJsonPrimitive()) {
            if (!names.getAsJsonPrimitive().isString()) {
                ErrorHandler.error("Invalid names specified for phase action!");
                return null;
            }
            phases.add(names.getAsString());
        } else {
            for (JsonElement element : names.getAsJsonArray()) {
                if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                    ErrorHandler.error("Invalid names specified for phase action!");
                    return null;
                }
                phases.add(element.getAsString());
            }
        }

        boolean set;
        if (value.has("set")) {
            set = value.getAsJsonPrimitive("set").getAsBoolean();
        } else {
            set = true;
        }
        return new PhaseAction(phases, set);
    }
}
