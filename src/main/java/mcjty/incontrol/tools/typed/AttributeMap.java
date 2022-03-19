package mcjty.incontrol.tools.typed;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AttributeMap {

    private final Map<Key<?>, Object> values = new HashMap<>();

    public <A> boolean has(@Nonnull Key<A> key) {
        return values.containsKey(key);
    }

    public <A> void set(@Nonnull Key<A> key, A value) {
        values.put(key, value);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Set<Key<?>> getKeys() {
        return values.keySet();
    }

    public <A> void setNonnull(@Nonnull Key<A> key, A value) {
        if (value != null) {
            values.put(key, value);
        }
    }

    public <A> void consume(@Nonnull Key<A> key, Consumer<A> consumer) {
        if (has(key)) {
            consumer.accept(get(key));
            values.remove(key);
        }
    }

    @Nullable
    public <A> A consumeAndFetch(@Nonnull Key<A> key) {
        return consumeAndFetch(key, null);
    }

    @Nullable
    public <A> A consumeAndFetch(@Nonnull Key<A> key, A def) {
        if (has(key)) {
            A value = get(key);
            values.remove(key);
            return value;
        }
        return def;
    }

    public <A> void consumeAsList(@Nonnull Key<A> key, Consumer<List<A>> consumer) {
        if (has(key)) {
            consumer.accept(getList(key));
            values.remove(key);
        }
    }

    public <A, B> void consume2(@Nonnull Key<A> key1, @Nonnull Key<B> key2, BiConsumer<A, B> consumer) {
        if (has(key1) || has(key2)) {
            consumer.accept(get(key1), get(key2));
            values.remove(key1);
            values.remove(key2);
        }
    }

    public <A> void consumeOrElse(@Nonnull Key<A> key, Consumer<A> consumer, Runnable elseRun) {
        if (has(key)) {
            consumer.accept(get(key));
            values.remove(key);
        } else {
            elseRun.run();
        }
    }

    public <A> A get(@Nonnull Key<A> key) {
        //noinspection unchecked
        return (A) values.get(key);
    }

    public <A> Optional<A> getOptional(@Nonnull Key<A> key) {
        //noinspection unchecked
        return Optional.ofNullable((A) values.get(key));
    }

    public <A> void addList(@Nonnull Key<A> key, A value) {
        if (!values.containsKey(key)) {
            values.put(key, new ArrayList<>());
        }
        //noinspection unchecked
        List<A> l = (List<A>) values.get(key);
        l.add(value);
    }

    public <A> void addListNonnull(@Nonnull Key<A> key, A value) {
        if (value == null) {
            return;
        }
        if (!values.containsKey(key)) {
            values.put(key, new ArrayList<>());
        }
        //noinspection unchecked
        List<A> l = (List<A>) values.get(key);
        l.add(value);
    }

    public <A> List<A> getList(@Nonnull Key<A> key) {
        if (!values.containsKey(key)) {
            return Collections.emptyList();
        }
        //noinspection unchecked
        return (List<A>) values.get(key);
    }
}
