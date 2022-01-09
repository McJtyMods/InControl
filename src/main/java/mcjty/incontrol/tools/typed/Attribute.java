package mcjty.incontrol.tools.typed;

public record Attribute<T>(Key<T> key, boolean multi) {

    public static <T> Attribute<T> create(Key<T> key) {
        return new Attribute<T>(key, false);
    }

    public static <T> Attribute<T> createMulti(Key<T> key) {
        return new Attribute<T>(key, true);
    }
}
