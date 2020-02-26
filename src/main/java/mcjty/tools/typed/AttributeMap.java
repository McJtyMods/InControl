package mcjty.tools.typed;

import javax.annotation.Nonnull;
import java.util.*;

public class AttributeMap {

    private final Map<Key<?>, Object> values = new HashMap<>();

    public <A> boolean has(@Nonnull Key<A> key) {
        return values.containsKey(key);
    }

    public <A> void set(@Nonnull Key<A> key, A value) {
        values.put(key, value);
    }

    public <A> void setNonnull(@Nonnull Key<A> key, A value) {
        if (value != null) {
            values.put(key, value);
        }
    }

    public <A> A get(@Nonnull Key<A> key) {
        return (A) values.get(key);
    }

    public <A> Optional<A> getOptional(@Nonnull Key<A> key) {
        return Optional.ofNullable((A) values.get(key));
    }

    public <A> void addList(@Nonnull Key<A> key, A value) {
        if (!values.containsKey(key)) {
            values.put(key, new ArrayList<>());
        }
        List l = (List) values.get(key);
        l.add(value);
    }

    public <A> void addListNonnull(@Nonnull Key<A> key, A value) {
        if (value == null) {
            return;
        }
        if (!values.containsKey(key)) {
            values.put(key, new ArrayList<>());
        }
        List l = (List) values.get(key);
        l.add(value);
    }

    public <A> List<A> getList(@Nonnull Key<A> key) {
        if (!values.containsKey(key)) {
            return Collections.emptyList();
        }
        return (List<A>) values.get(key);
    }
}
