package mcjty.incontrol.rules.support;

import net.minecraftforge.eventbus.api.Event;

public enum ICResult {
    DENY(Event.Result.DENY),
    DENY_WITH_ACTIONS(Event.Result.DENY),
    DEFAULT(Event.Result.DEFAULT),
    ALLOW(Event.Result.ALLOW);

    private final Event.Result mcResult;

    ICResult(Event.Result mcResult) {
        this.mcResult = mcResult;
    }

    public Event.Result getMcResult() {
        return mcResult;
    }
}
