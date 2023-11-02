package mcjty.incontrol.events;

public record EventType(Type type, String extra) {

    enum Type {
        MOB_KILLED,
        BLOCK_BROKEN;

        public static Type getType(String str) {
            return valueOf(str.toUpperCase());
        }
    }
}
