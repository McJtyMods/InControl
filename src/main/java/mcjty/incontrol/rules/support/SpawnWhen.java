package mcjty.incontrol.rules.support;

public enum SpawnWhen {
    POSITION,
    ONJOIN,
    FINALIZE,
    DESPAWN;

    public static SpawnWhen getByName(String name) {
        for (SpawnWhen when : values()) {
            if (when.name().equalsIgnoreCase(name)) {
                return when;
            }
        }
        return null;
    }
}
