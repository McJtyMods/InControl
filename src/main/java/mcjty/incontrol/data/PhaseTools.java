package mcjty.incontrol.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.HashSet;
import java.util.Set;

import static mcjty.incontrol.rules.support.RuleKeys.PHASE;

public class PhaseTools {

    public static Set<String> getPhases(JsonElement element) {
        Set<String> phases = new HashSet<>();
        if (element.getAsJsonObject().has(PHASE.name())) {
            JsonElement phaseElement = element.getAsJsonObject().get(PHASE.name());
            if (phaseElement.isJsonArray()) {
                JsonArray phasesArray = phaseElement.getAsJsonArray();
                phasesArray.forEach(e -> phases.add(e.getAsString()));
            } else {
                phases.add(phaseElement.getAsString());
            }
        }
        return phases;
    }
}
