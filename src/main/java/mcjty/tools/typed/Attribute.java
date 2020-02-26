package mcjty.tools.typed;

public class Attribute<T> {

    private final Key<T> key;
    private final boolean multi;

    private Attribute(Key<T> key, boolean multi) {
        this.key = key;
        this.multi = multi;
    }

    public static <T> Attribute<T> create(Key<T> key) {
        return new Attribute<T>(key, false);
    }

    public static <T> Attribute<T> createMulti(Key<T> key) {
        return new Attribute<T>(key, true);
    }

    public Key<T> getKey() {
        return key;
    }

    public boolean isMulti() {
        return multi;
    }
}
