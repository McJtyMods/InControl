package mcjty.incontrol.tools.typed;


import javax.annotation.Nonnull;

public record Key<V>(@Nonnull Type<V> type, @Nonnull String name) {

    @Nonnull
    public static <V> Key<V> create(@Nonnull final Type<V> type,
                                    @Nonnull final String code) {
        return new Key<V>(type, code);
    }

    @Override
    public String toString() {
        return "Key(" + name + ')';
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }
}
