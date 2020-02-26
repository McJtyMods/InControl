package mcjty.tools.typed;


import javax.annotation.Nonnull;

public class Key<V> {

    @Nonnull private final Type<V> type;
    @Nonnull private final String name;

    Key(@Nonnull final Type<V> type, @Nonnull final String name) {
        this.type = type;
        this.name = name;
    }

    @Nonnull
    public static <V> Key<V> create(@Nonnull final Type<V> type,
                                    @Nonnull final String code) {
        return new Key<V>(type, code);
    }

    @Nonnull
    public Type<V> getType() {
        return type;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Key(" + name + ')';
    }
}
