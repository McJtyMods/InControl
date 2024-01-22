package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import mcjty.incontrol.data.DataStorage;
import mcjty.incontrol.rules.support.SpawnWhen;
import mcjty.incontrol.tools.varia.JSonTools;
import net.minecraft.world.level.Level;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RulesManager {

    private static final List<SpawnRule> rules = new ArrayList<>();
    private static List<SpawnRule> filteredRulesPosition = null;
    private static List<SpawnRule> filteredRulesOnJoin = null;
    private static List<SpawnRule> filteredRulesFinalize = null;
    private static List<SpawnRule> filteredRulesDespawn = null;

    private static final List<SummonAidRule> summonAidRules = new ArrayList<>();
    private static List<SummonAidRule> filteredSummonAidRules = null;

    private static final List<LootRule> lootRules = new ArrayList<>();
    private static List<LootRule> filteredLootRules = null;

    private static final List<ExperienceRule> experienceRules = new ArrayList<>();
    private static List<ExperienceRule> filteredExperienceRules = null;

    public static List<PhaseRule> phaseRules = new ArrayList<>();
    private static String path;

    public static List<EffectRule> effectRules = new ArrayList<>();
    public static List<HarvestRule> harvestRules = new ArrayList<>();
    public static List<PlaceRule> placeRules = new ArrayList<>();
    public static List<RightClickRule> rightclickRules = new ArrayList<>();
    public static List<LeftClickRule> leftclickRules = new ArrayList<>();

    public static void reloadRules() {
        rules.clear();
        summonAidRules.clear();
        lootRules.clear();
        experienceRules.clear();
        phaseRules.clear();
        effectRules.clear();
        harvestRules.clear();
        placeRules.clear();
        rightclickRules.clear();
        leftclickRules.clear();
        onPhaseChange();
        readAllRules();
    }

    public static void setRulePath(Path path) {
        RulesManager.path = path.toString();
    }

    public static void readRules() {
        readAllRules();
    }

    public static void onPhaseChange() {
        filteredRulesPosition = null;
        filteredRulesOnJoin = null;
        filteredRulesFinalize = null;
        filteredRulesDespawn = null;
        filteredSummonAidRules = null;
        filteredLootRules = null;
        filteredExperienceRules = null;
    }

    private static List<SpawnRule> getCorrectList(SpawnWhen when) {
        return switch (when) {
            case POSITION -> filteredRulesPosition;
            case ONJOIN -> filteredRulesOnJoin;
            case FINALIZE -> filteredRulesFinalize;
            case DESPAWN -> filteredRulesDespawn;
        };
    }

    private static void setCorrectList(SpawnWhen when, List<SpawnRule> list) {
        switch (when) {
            case POSITION -> filteredRulesPosition = list;
            case ONJOIN -> filteredRulesOnJoin = list;
            case FINALIZE -> filteredRulesFinalize = list;
            case DESPAWN -> filteredRulesDespawn = list;
        };
    }

    public static List<SpawnRule> getFilteredRules(Level world, SpawnWhen when) {
        List<SpawnRule> correctList = getCorrectList(when);
        if (correctList == null) {
            Set<String> phases = DataStorage.getData(world).getPhases();
            correctList = rules.stream().filter(r -> r.getWhen() == when && phases.containsAll(r.getPhases())).collect(Collectors.toList());
        }
        setCorrectList(when, correctList);
        return correctList;
    }

    public static List<SummonAidRule> getFilteredSummonAidRules(Level world) {
        if (filteredSummonAidRules == null) {
            Set<String> phases = DataStorage.getData(world).getPhases();
            filteredSummonAidRules = summonAidRules.stream().filter(r -> phases.containsAll(r.getPhases())).collect(Collectors.toList());
        }
        return filteredSummonAidRules;
    }

    public static List<LootRule> getFilteredLootRules(Level world) {
        if (filteredLootRules == null) {
            Set<String> phases = DataStorage.getData(world).getPhases();
            filteredLootRules = lootRules.stream().filter(r -> phases.containsAll(r.getPhases())).collect(Collectors.toList());
        }
        return filteredLootRules;
    }

    public static List<ExperienceRule> getFilteredExperienceRuiles(Level world) {
        if (filteredExperienceRules == null) {
            Set<String> phases = DataStorage.getData(world).getPhases();
            filteredExperienceRules = experienceRules.stream().filter(r -> phases.containsAll(r.getPhases())).collect(Collectors.toList());
        }
        return filteredExperienceRules;
    }

    private static boolean exists(String file) {
        File f = new File(file);
        return f.exists() && !f.isDirectory();
    }

    private static void readAllRules() {
        File directory = new File(path + File.separator + "incontrol");
        if (!directory.exists()) {
            directory.mkdir();
        }

        safeCall("spawn.json", () -> readRules(path, "spawn.json", SpawnRule::parse, rules));
        safeCall("summonaid.json", () -> readRules(path, "summonaid.json", SummonAidRule::parse, summonAidRules));
        safeCall("loot.json", () -> readRules(path, "loot.json", LootRule::parse, lootRules));
        safeCall("experience.json", () -> readRules(path, "experience.json", ExperienceRule::parse, experienceRules));
        safeCall("phases.json", () -> readRules(path, "phases.json", PhaseRule::parse, phaseRules));
        safeCall("effects.json", () -> readRules(path, "effects.json", EffectRule::parse, effectRules));
        safeCall("breakevents.json", () -> readRules(path, "breakevents.json", HarvestRule::parse, harvestRules));
        safeCall("placeevents.json", () -> readRules(path, "placeevents.json", PlaceRule::parse, placeRules));
        safeCall("rightclicks.json", () -> readRules(path, "rightclicks.json", RightClickRule::parse, rightclickRules));
        safeCall("leftclicks.json", () -> readRules(path, "leftclicks.json", LeftClickRule::parse, leftclickRules));
    }

    private static void safeCall(String name, Runnable code) {
        try {
            code.run();
        } catch (Exception e) {
            ErrorHandler.error("JSON error in '" + name + "': check log for details (" + e.getMessage() + ")");
            InControl.setup.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Error parsing '" + name + "'", e);
        }
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
                InControl.setup.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Rule " + i + " in " + filename + " is invalid, skipping!");
            }
            i++;
        }
    }

    private static JsonElement getRootElement(String path, String filename) {
        return JSonTools.getRootElement(path, filename, InControl.setup.getLogger());
    }
}
