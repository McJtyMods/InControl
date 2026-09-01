package mcjty.incontrol.compat;

import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KubeJSSupportTest {

    @Test
    void parsesTheDocumentedConditionShape() {
        var bool = KubeJSSupport.parseCondition(JsonParser.parseString("{\"variable\":\"Potato\",\"bool\":true}"));
        var integer = KubeJSSupport.parseCondition(JsonParser.parseString("{\"variable\":\"Amount\",\"condition\":\">=\",\"int\":5}"));
        var decimal = KubeJSSupport.parseCondition(JsonParser.parseString("{\"variable\":\"Ratio\",\"condition\":\"<\",\"double\":2.5}"));

        CompoundTag variables = new CompoundTag();
        variables.putBoolean("Potato", true);
        variables.putInt("Amount", 5);
        variables.putDouble("Ratio", 2.25D);

        assertTrue(bool.matches(variables));
        assertTrue(integer.matches(variables));
        assertTrue(decimal.matches(variables));
    }

    @Test
    void matchesBooleanStoredInServerPersistentData() {
        var condition = new KubeJSSupport.BooleanCondition("extremeMode", true);
        CompoundTag persistentData = new CompoundTag();

        persistentData.putBoolean("extremeMode", true);
        assertTrue(condition.matches(persistentData));

        persistentData.putBoolean("extremeMode", false);
        assertFalse(condition.matches(persistentData));

        persistentData.putInt("extremeMode", 1);
        assertFalse(condition.matches(persistentData));

        persistentData.remove("extremeMode");
        assertFalse(condition.matches(persistentData));
    }

    @Test
    void supportsAllIntegerOperatorsForAnyNumberType() {
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", ">=", 5).matches(number("Amount", 5.0D)));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", ">", 5).matches(number("Amount", 6L)));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", "<=", 5).matches(number("Amount", 5F)));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", "<", 5).matches(number("Amount", 4)));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", "=", 5).matches(number("Amount", 5.0D)));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", "==", 5).matches(number("Amount", 5)));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", "!=", 5).matches(number("Amount", 6)));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", "<>", 5).matches(number("Amount", 6)));

        CompoundTag nonNumeric = new CompoundTag();
        nonNumeric.putString("Amount", "5");
        assertFalse(new KubeJSSupport.IntegerCondition("Amount", ">=", 5).matches(nonNumeric));
    }

    @Test
    void comparesDoubleValuesWithoutIntegerTruncation() {
        var condition = new KubeJSSupport.DoubleCondition("Ratio", ">=", 5.25D);

        assertTrue(condition.matches(number("Ratio", 5.25D)));
        assertTrue(condition.matches(number("Ratio", 5.5F)));
        assertFalse(condition.matches(number("Ratio", 5.2D)));

        CompoundTag nonNumeric = new CompoundTag();
        nonNumeric.putString("Ratio", "5.25");
        assertFalse(condition.matches(nonNumeric));
    }

    @Test
    void requiresEveryVariableConditionToMatch() {
        CompoundTag variables = new CompoundTag();
        variables.putBoolean("Potato", true);
        variables.putDouble("Amount", 5.0D);
        List<KubeJSSupport.VariableCondition> conditions = List.of(
                new KubeJSSupport.BooleanCondition("Potato", true),
                new KubeJSSupport.IntegerCondition("Amount", ">=", 5));

        assertTrue(KubeJSSupport.matches(variables, conditions));

        variables.remove("Amount");
        assertFalse(KubeJSSupport.matches(variables, conditions));
    }

    private static CompoundTag number(String variable, Number value) {
        CompoundTag variables = new CompoundTag();
        if (value instanceof Integer integer) {
            variables.putInt(variable, integer);
        } else if (value instanceof Long longValue) {
            variables.putLong(variable, longValue);
        } else if (value instanceof Float floatValue) {
            variables.putFloat(variable, floatValue);
        } else {
            variables.putDouble(variable, value.doubleValue());
        }
        return variables;
    }
}
