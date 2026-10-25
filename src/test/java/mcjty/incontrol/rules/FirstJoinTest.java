package mcjty.incontrol.rules;

import com.google.gson.JsonParser;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import mcjty.incontrol.platform.RuleEvents.EntityJoinLevelEvent;
import mcjty.incontrol.rules.support.FirstJoinTracker;
import mcjty.incontrol.setup.ModSetup;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FirstJoinTest {

    @Test
    void omittedConditionKeepsMatchingEveryJoin() {
        SpawnRule rule = rule("");
        Set<String> tags = new HashSet<>();
        assertTrue(rule.match(null, FirstJoinTracker.beginJoin(false, tags)));
        assertTrue(rule.match(null, FirstJoinTracker.beginJoin(true, tags)));
        assertTrue(rule.match(null, FirstJoinTracker.beginJoin(false, tags)));
    }

    @Test
    void firstJoinConditionExcludesReloadsAndDimensionTransfers() {
        SpawnRule first = rule(",\"firstjoin\":true");
        SpawnRule subsequent = rule(",\"firstjoin\":false");
        Set<String> tags = new HashSet<>();
        boolean firstJoin = FirstJoinTracker.beginJoin(false, tags);
        assertTrue(first.match(null, firstJoin));
        assertFalse(subsequent.match(null, firstJoin));

        Set<String> restoredTags = new HashSet<>(tags);
        boolean reloaded = FirstJoinTracker.beginJoin(true, restoredTags);
        assertFalse(first.match(null, reloaded));
        assertTrue(subsequent.match(null, reloaded));

        boolean transferred = FirstJoinTracker.beginJoin(false, new HashSet<>(restoredTags));
        assertFalse(first.match(null, transferred));
        assertTrue(subsequent.match(null, transferred));
    }

    @Test
    void existingUntaggedMobStaysExcludedAfterLoadingAndThenTransferring() {
        Set<String> legacyTags = new HashSet<>();
        assertFalse(FirstJoinTracker.beginJoin(true, legacyTags));
        assertFalse(FirstJoinTracker.beginJoin(false, new HashSet<>(legacyTags)));
    }

    @Test
    void continuedRulesShareTheSameFirstJoinSnapshot() {
        boolean firstJoin = FirstJoinTracker.beginJoin(false, new HashSet<>());
        SpawnRule first = rule(",\"firstjoin\":true,\"continue\":true");
        SpawnRule second = rule(",\"firstjoin\":true");
        assertTrue(first.match(null, firstJoin));
        assertTrue(first.isDoContinue());
        assertTrue(second.match(null, firstJoin));
    }

    @Test
    void unmatchedInitialRulesDoNotGrantAnotherFirstJoinLater() {
        Set<String> tags = new HashSet<>();
        boolean firstJoin = FirstJoinTracker.beginJoin(false, tags);
        assertFalse(rule(",\"firstjoin\":true,\"random\":0").match(null, firstJoin));
        assertFalse(rule(",\"firstjoin\":true").match(null, FirstJoinTracker.beginJoin(false, tags)));
    }

    @Test
    void firstJoinRequiresOnJoinTiming() throws ReflectiveOperationException {
        // Initialize error logging without starting Fabric or creating configuration files.
        var loggerField = ModSetup.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        Object previousLogger = loggerField.get(InControl.setup);
        loggerField.set(InControl.setup, LogManager.getLogger(InControl.MODID));
        try {
            for (String when : new String[]{"position", "finalize", "despawn"}) {
                for (boolean firstJoin : new boolean[]{true, false}) {
                    assertNull(SpawnRule.parse(JsonParser.parseString(
                            "{\"when\":\"" + when + "\",\"firstjoin\":" + firstJoin + "}"), 0));
                }
            }
            assertNull(SpawnRule.parse(JsonParser.parseString("{\"firstjoin\":true}"), 0));
        } finally {
            loggerField.set(InControl.setup, previousLogger);
            ErrorHandler.clearErrors();
        }
    }

    @Test
    void fabricJoinContextPreservesDiskLoadFlag() {
        assertTrue(new EntityJoinLevelEvent(null, null, true).loadedFromDisk());
        assertFalse(new EntityJoinLevelEvent(null, null, false).loadedFromDisk());
        assertFalse(new EntityJoinLevelEvent(null, null).loadedFromDisk());
    }

    private static SpawnRule rule(String conditions) {
        return SpawnRule.parse(JsonParser.parseString("{\"when\":\"onjoin\"" + conditions + "}"), 0);
    }
}
