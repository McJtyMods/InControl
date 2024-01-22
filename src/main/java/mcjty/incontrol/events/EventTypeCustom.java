package mcjty.incontrol.events;

import com.google.gson.JsonObject;

public class EventTypeCustom implements EventType {

    private String name;

    public EventTypeCustom() {
    }

    @Override
    public Type type() {
        return Type.CUSTOM;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean parse(JsonObject object) {
        name = object.get("name").getAsString();
        return true;
    }
}
