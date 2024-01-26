package mcjty.incontrol.events;

import java.util.List;
import java.util.function.BiFunction;

public record NumberAction(String name, List<Action> actions) {

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
