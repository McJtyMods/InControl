package mcjty.incontrol.events;

import com.google.gson.JsonObject;

public interface EventType {

    Type type();
    boolean parse(JsonObject object);

    enum Type {
        MOB_KILLED,
        BLOCK_BROKEN;

        public static EventType.Type getType(String str) {
            return valueOf(str.toUpperCase());
        }
    }
}
