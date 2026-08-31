package mcjty.incontrol.compat;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KubeJSSupportTest {

    @Test
    void parsesTheDocumentedConditionShape() {
        JsonParser parser = new JsonParser();

        var bool = KubeJSSupport.parseCondition(parser.parse("{\"variable\":\"Potato\",\"bool\":true}"));
        var integer = KubeJSSupport.parseCondition(parser.parse("{\"variable\":\"Amount\",\"condition\":\">=\",\"int\":5}"));
        var decimal = KubeJSSupport.parseCondition(parser.parse("{\"variable\":\"Ratio\",\"condition\":\"<\",\"double\":2.5}"));

        assertTrue(bool.matches(true));
        assertTrue(integer.matches(5.0D));
        assertTrue(decimal.matches(2.25D));
    }

    @Test
    void matchesBooleanVariablesByTypeAndValue() {
        var condition = new KubeJSSupport.BooleanCondition("Potato", true);

        assertTrue(condition.matches(true));
        assertFalse(condition.matches(false));
        assertFalse(condition.matches(1));
        assertFalse(condition.matches(null));
    }

    @Test
    void supportsAllIntegerOperatorsForAnyNumberType() {
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", ">=", 5).matches(5.0D));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", ">", 5).matches(6L));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", "<=", 5).matches(5F));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", "<", 5).matches(4));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", "=", 5).matches(5.0D));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", "==", 5).matches(5));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", "!=", 5).matches(6));
        assertTrue(new KubeJSSupport.IntegerCondition("Amount", "<>", 5).matches(6));
        assertFalse(new KubeJSSupport.IntegerCondition("Amount", ">=", 5).matches("5"));
    }

    @Test
    void comparesDoubleValuesWithoutIntegerTruncation() {
        var condition = new KubeJSSupport.DoubleCondition("Ratio", ">=", 5.25D);

        assertTrue(condition.matches(5.25D));
        assertTrue(condition.matches(5.5F));
        assertFalse(condition.matches(5.2D));
        assertFalse(condition.matches("5.25"));
    }

    @Test
    void requiresEveryVariableConditionToMatch() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("Potato", true);
        variables.put("Amount", 5.0D);
        List<KubeJSSupport.VariableCondition> conditions = List.of(
                new KubeJSSupport.BooleanCondition("Potato", true),
                new KubeJSSupport.IntegerCondition("Amount", ">=", 5));

        assertTrue(KubeJSSupport.matches(variables, conditions));

        variables.remove("Amount");
        assertFalse(KubeJSSupport.matches(variables, conditions));
    }
}
