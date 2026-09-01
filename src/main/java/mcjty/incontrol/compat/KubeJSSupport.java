package mcjty.incontrol.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.latvian.mods.kubejs.core.WithPersistentData;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.setup.ModSetup;
import mcjty.incontrol.tools.varia.JSonTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class KubeJSSupport {

    private static final Predicate<MinecraftServer> NEVER = server -> false;

    private KubeJSSupport() {
    }

    public static Predicate<MinecraftServer> parse(JsonElement element) {
        if (!ModSetup.kubejs) {
            ErrorHandler.error("KubeJS condition specified but KubeJS is not loaded!");
            return NEVER;
        }

        List<VariableCondition> conditions = new ArrayList<>();
        for (JsonElement condition : JSonTools.asArrayOrSingle(element).toList()) {
            VariableCondition parsed = parseCondition(condition);
            if (parsed == null) {
                return NEVER;
            }
            conditions.add(parsed);
        }

        if (conditions.isEmpty()) {
            ErrorHandler.error("KubeJS condition needs at least one variable check!");
            return NEVER;
        }
        return server -> server != null && matches(((WithPersistentData) server).kjs$getPersistentData(), conditions);
    }

    public static Predicate<MinecraftServer> parse(List<String> elements) {
        if (!ModSetup.kubejs) {
            ErrorHandler.error("KubeJS condition specified but KubeJS is not loaded!");
            return NEVER;
        }

        JsonParser parser = new JsonParser();
        List<VariableCondition> conditions = new ArrayList<>();
        for (String element : elements) {
            VariableCondition parsed = parseCondition(parser.parse(element));
            if (parsed == null) {
                return NEVER;
            }
            conditions.add(parsed);
        }

        if (conditions.isEmpty()) {
            ErrorHandler.error("KubeJS condition needs at least one variable check!");
            return NEVER;
        }
        return server -> server != null && matches(((WithPersistentData) server).kjs$getPersistentData(), conditions);
    }

    static VariableCondition parseCondition(JsonElement element) {
        if (!element.isJsonObject()) {
            ErrorHandler.error("KubeJS variable check needs to be an object!");
            return null;
        }

        JsonObject object = element.getAsJsonObject();
        if (!object.has("variable") || !object.get("variable").isJsonPrimitive()
                || !object.getAsJsonPrimitive("variable").isString()) {
            ErrorHandler.error("KubeJS variable check needs to have a 'variable' field!");
            return null;
        }

        String variable = object.get("variable").getAsString();
        if (variable.isEmpty()) {
            ErrorHandler.error("KubeJS 'variable' value cannot be empty!");
            return null;
        }
        boolean hasBool = object.has("bool");
        boolean hasInt = object.has("int");
        boolean hasDouble = object.has("double");
        if ((hasBool ? 1 : 0) + (hasInt ? 1 : 0) + (hasDouble ? 1 : 0) != 1) {
            ErrorHandler.error("KubeJS variable check needs exactly one 'bool', 'int', or 'double' field!");
            return null;
        }

        if (hasBool) {
            JsonElement bool = object.get("bool");
            if (!bool.isJsonPrimitive() || !bool.getAsJsonPrimitive().isBoolean()) {
                ErrorHandler.error("KubeJS 'bool' value needs to be a boolean!");
                return null;
            }
            if (object.has("condition")) {
                ErrorHandler.error("KubeJS boolean variable checks cannot have a 'condition' field!");
                return null;
            }
            return new BooleanCondition(variable, bool.getAsBoolean());
        }

        String operator = parseOperator(object);
        if (operator == null) {
            return null;
        }
        if (!isValidOperator(operator)) {
            ErrorHandler.error("Unknown KubeJS numeric condition '" + operator + "'!");
            return null;
        }

        if (hasInt) {
            JsonElement integer = object.get("int");
            if (!integer.isJsonPrimitive() || !integer.getAsJsonPrimitive().isNumber()) {
                ErrorHandler.error("KubeJS 'int' value needs to be an integer!");
                return null;
            }
            int expected = integer.getAsInt();
            if (integer.getAsDouble() != expected) {
                ErrorHandler.error("KubeJS 'int' value needs to be an integer!");
                return null;
            }
            return new IntegerCondition(variable, operator, expected);
        }

        JsonElement decimal = object.get("double");
        if (!decimal.isJsonPrimitive() || !decimal.getAsJsonPrimitive().isNumber()) {
            ErrorHandler.error("KubeJS 'double' value needs to be a number!");
            return null;
        }
        double expected = decimal.getAsDouble();
        if (!Double.isFinite(expected)) {
            ErrorHandler.error("KubeJS 'double' value needs to be finite!");
            return null;
        }
        return new DoubleCondition(variable, operator, expected);
    }

    private static String parseOperator(JsonObject object) {
        if (object.has("condition")) {
            JsonElement condition = object.get("condition");
            if (!condition.isJsonPrimitive() || !condition.getAsJsonPrimitive().isString()) {
                ErrorHandler.error("KubeJS 'condition' value needs to be a string!");
                return null;
            }
        }
        return object.has("condition") ? object.get("condition").getAsString() : "=";
    }

    private static boolean isValidOperator(String operator) {
        return switch (operator) {
            case "=", "==", "!=", "<>", ">", ">=", "<", "<=" -> true;
            default -> false;
        };
    }

    static boolean matches(CompoundTag variables, List<VariableCondition> conditions) {
        for (VariableCondition condition : conditions) {
            if (!condition.matches(variables)) {
                return false;
            }
        }
        return true;
    }

    interface VariableCondition {
        String variable();

        boolean matches(CompoundTag variables);
    }

    record BooleanCondition(String variable, boolean expected) implements VariableCondition {
        @Override
        public boolean matches(CompoundTag variables) {
            return variables.contains(variable, Tag.TAG_BYTE) && variables.getBoolean(variable) == expected;
        }
    }

    record IntegerCondition(String variable, String operator, int expected) implements VariableCondition {
        @Override
        public boolean matches(CompoundTag variables) {
            return variables.contains(variable, Tag.TAG_ANY_NUMERIC)
                    && compare(variables.getDouble(variable), operator, expected);
        }
    }

    record DoubleCondition(String variable, String operator, double expected) implements VariableCondition {
        @Override
        public boolean matches(CompoundTag variables) {
            return variables.contains(variable, Tag.TAG_ANY_NUMERIC)
                    && compare(variables.getDouble(variable), operator, expected);
        }
    }

    private static boolean compare(double actual, String operator, double expected) {
        return switch (operator) {
            case "=", "==" -> actual == expected;
            case "!=", "<>" -> actual != expected;
            case ">" -> actual > expected;
            case ">=" -> actual >= expected;
            case "<" -> actual < expected;
            case "<=" -> actual <= expected;
            default -> false;
        };
    }
}
