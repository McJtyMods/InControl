package mcjty.incontrol.events;

import com.google.gson.JsonObject;
import mcjty.incontrol.ErrorHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public record NumberAction(String name, List<Action> actions) {

    @Nullable
    static NumberAction parse(JsonObject object) {
        JsonObject number = object.getAsJsonObject("number");
        if (number == null) {
            // Valid
            return null;
        }
        if (!number.has("name")) {
            ErrorHandler.error("No name specified for number action!");
            return null;
        }
        if (!number.has("value")) {
            ErrorHandler.error("No set/add/mul specified for number action!");
            return null;
        }
        String name = number.getAsJsonPrimitive("name").getAsString();
        String value = number.getAsJsonPrimitive("value").getAsString();
        List<Action> actions = parseActions(value);
        if (actions == null) return null;
        return new NumberAction(name, actions);
    }

    @Nullable
    public static NumberAction createUnnamed(String value) {
        List<Action> actions = parseActions(value);
        if (actions == null) return null;
        return new NumberAction("", actions);
    }

    @Nullable
    private static List<Action> parseActions(String value) {
        // Parse value as a string with the following format:
        // [<operator><number>]+
        // Example: *30+2
        // This means: multiply by 30 and add 2
        // Example: 60-1
        // This means: Take 60 and subtract 1
        List<Action> actions = new ArrayList<>();
        value = value.trim();
        int pos = 0;
        while (pos < value.length()) {
            char c = value.charAt(pos);
            if (c == ' ') {
                pos++;
                continue;
            }
            Operator operator = Operator.getOperator(c);
            if (operator == Operator.NONE) {
                ErrorHandler.error("Invalid number action '" + value + "'!");
                return null;
            }
            pos++;
            int start = pos;
            while (pos < value.length() && Character.isDigit(value.charAt(pos))) {
                pos++;
            }
            if (pos > start) {
                int v = Integer.parseInt(value.substring(start, pos));
                actions.add(new Action(operator, v));
            } else {
                ErrorHandler.error("Invalid number action '" + value + "'!");
                return null;
            }
        }
        return actions;
    }

    public int perform(int a) {
        for (Action action : actions) {
            a = action.perform(a);
        }
        return a;
    }

    record Action(Operator operator, int value) {
        public int perform(int a) {
            return operator.perform(a, value);
        }
    }

    enum Operator {
        NONE("", (a, b) -> -1),
        SET("", (a, b) -> b),
        ADD("+", (a, b) -> a + b),
        SUB("-", (a, b) -> a - b),
        MUL("*", (a, b) -> a * b),
        DIV("/", (a, b) -> a / b),
        MOD("%", (a, b) -> a % b);

        private final String symbol;
        private final BiFunction<Integer, Integer, Integer> function;

        Operator(String symbol, BiFunction<Integer, Integer, Integer> function) {
            this.symbol = symbol;
            this.function = function;
        }

        public int perform(int a, int b) {
            return function.apply(a, b);
        }

        public static Operator getOperator(char op) {
            for (Operator operator : values()) {
                if (operator.symbol.length() >= 1 && operator.symbol.charAt(0) == op) {
                    return operator;
                }
            }
            // Return SET if str starts with a digit
            return Character.isDigit(op) ? SET : NONE;
        }
    }
}
