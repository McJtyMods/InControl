package mcjty.incontrol.tools.distance;

import java.lang.ref.WeakReference;
import java.util.Iterator;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

/**
 * This is a memory-efficient data structure that maintains a pool of deduplicated sets.
 * Instead of creating a new HashSet every time a player is added or removed from a chunk's tracking, it reuses existing set instances that have the same contents.
 * This is a direct port of the pooled hash sets from Paper's 'per-player mob spawns' (https://github.com/PaperMC/Paper/pull/2171).
 */
public class PooledHashSets<E> {
    // A map from set to the same set used as a lookup table. Since the key and value are the same object,
    // it's essentially a HashSet that lets you retrieve the existing instance by value equality.
    protected final Object2ObjectOpenHashMap<PooledObjectLinkedOpenHashSet<E>, PooledObjectLinkedOpenHashSet<E>> mapPool = new Object2ObjectOpenHashMap<>(64, 0.25f);

    // Reduces the reference count of a set. When it hits 0, the set is removed from the pool since nothing references it anymore.
    // The -1 is a special case that marks sets that are "permanent" and should never be deleted.
    protected void decrementReferenceCount(final PooledObjectLinkedOpenHashSet<E> current) {
        if (current.referenceCount == 0) {
            throw new IllegalStateException("Cannot decrement reference count for " + current);
        }
        if (current.referenceCount == -1 || --current.referenceCount > 0) {
            return;
        }
        this.mapPool.remove(current);
    }

    // Returns a set that is identical to 'current' but with the object added.
    // - Checks the add cache first.
    // - Adds the element to 'current' temporarily to see how the new set would look like.
    // - If found in the pool, reuses it, if not, creates a new entry.
    // - Restores 'current' to its original state, updates the cache and reduces 'current''s reference count since the caller is moving to the new set
    public PooledObjectLinkedOpenHashSet<E> findMapWith(final PooledObjectLinkedOpenHashSet<E> current, final E object) {
        final PooledObjectLinkedOpenHashSet<E> cached = current.getAddCache(object);
        if (cached != null) {
            if (cached.referenceCount != -1)
                ++cached.referenceCount;
            decrementReferenceCount(current);
            return cached;
        }

        if (!current.add(object)) {
            return current;
        }

        PooledObjectLinkedOpenHashSet<E> ret = this.mapPool.get(current);
        if (ret == null) {
            ret = new PooledObjectLinkedOpenHashSet<>(current);
            current.remove(object);
            this.mapPool.put(ret, ret);
            ret.referenceCount = 1;
        } else {
            if (ret.referenceCount != -1)
                ++ret.referenceCount;
            current.remove(object);
        }

        current.updateAddCache(object, ret);
        decrementReferenceCount(current);
        return ret;
    }

    // Returns a set that is identical to 'current' but with the object removed.
    public PooledObjectLinkedOpenHashSet<E> findMapWithout(final PooledObjectLinkedOpenHashSet<E> current, final E object) {
        if (current.set.size() == 1) {
            decrementReferenceCount(current);
            return null;
        }

        final PooledObjectLinkedOpenHashSet<E> cached = current.getRemoveCache(object);
        if (cached != null) {
            if (cached.referenceCount != -1)
                ++cached.referenceCount;
            decrementReferenceCount(current);
            return cached;
        }

        if (!current.remove(object)) {
            return current;
        }

        PooledObjectLinkedOpenHashSet<E> ret = this.mapPool.get(current);
        if (ret == null) {
            ret = new PooledObjectLinkedOpenHashSet<>(current);
            current.add(object);
            this.mapPool.put(ret, ret);
            ret.referenceCount = 1;
        } else {
            if (ret.referenceCount != -1)
                ++ret.referenceCount;
            current.add(object);
        }

        current.updateRemoveCache(object, ret);
        decrementReferenceCount(current);
        return ret;
    }

    // Set wrapper.
    public static final class PooledObjectLinkedOpenHashSet<E> implements Iterable<E> {

        private static final WeakReference NULL_REFERENCE = new WeakReference(null);

        // Contains the actual elements.
        final ObjectLinkedOpenHashSet<E> set;
        // How many things are pointing to this set instance. -1 means its permanent and never deleted.
        int referenceCount;
        // A running XOR-style hash maintained incrementally so equality checks are fast.
        int hash;

        // One-entry caches stored as WeakReferences so they don't prevent garbage collection.
        WeakReference<E> lastAddObject = NULL_REFERENCE;
        WeakReference<PooledObjectLinkedOpenHashSet<E>> lastAddMap = NULL_REFERENCE;
        WeakReference<E> lastRemoveObject = NULL_REFERENCE;
        WeakReference<PooledObjectLinkedOpenHashSet<E>> lastRemoveMap = NULL_REFERENCE;

        public PooledObjectLinkedOpenHashSet() {
            this.set = new ObjectLinkedOpenHashSet<>(2, 0.6f);
        }

        public PooledObjectLinkedOpenHashSet(final E single) {
            this();
            this.referenceCount = -1;
            this.add(single);
        }

        public PooledObjectLinkedOpenHashSet(final PooledObjectLinkedOpenHashSet<E> other) {
            this.set = other.set.clone();
            this.hash = other.hash;
        }

        // Fast integer mixing function used to compute each element's contribution to the set's hash.
        // Using an additive hash makes it so that the overall hash stays correct without recomputing from scratch.
        static int hash0(int x) {
            x *= 0x36935555;
            x ^= x >>> 16;
            return x;
        }

        // Checks if the last add operation involved the same element and if the resulting set is still alive.
        // If so, returns it directly and skips the pool lookup entirely.
        public PooledObjectLinkedOpenHashSet<E> getAddCache(final E element) {
            final E currentAdd = this.lastAddObject.get();
            if (currentAdd == null || !(currentAdd == element || currentAdd.equals(element)))
                return null;
            final PooledObjectLinkedOpenHashSet<E> map = this.lastAddMap.get();
            if (map == null || map.referenceCount == 0)
                return null;
            return map;
        }

        // Checks if the last remove operation involved the same element and if the resulting set is still alive.
        // If so, returns it directly and skips the pool lookup entirely.
        public PooledObjectLinkedOpenHashSet<E> getRemoveCache(final E element) {
            final E currentRemove = this.lastRemoveObject.get();
            if (currentRemove == null || !(currentRemove == element || currentRemove.equals(element)))
                return null;
            final PooledObjectLinkedOpenHashSet<E> map = this.lastRemoveMap.get();
            if (map == null || map.referenceCount == 0)
                return null;
            return map;
        }

        // Stores the result of an add operation into the one-entry cache.
        public void updateAddCache(final E element, final PooledObjectLinkedOpenHashSet<E> map) {
            this.lastAddObject = new WeakReference<>(element);
            this.lastAddMap = new WeakReference<>(map);
        }

        // // Stores the result of a remove operation into the one-entry cache.
        public void updateRemoveCache(final E element, final PooledObjectLinkedOpenHashSet<E> map) {
            this.lastRemoveObject = new WeakReference<>(element);
            this.lastRemoveMap = new WeakReference<>(map);
        }

        // Adds an element to a set
        boolean add(final E element) {
            boolean added = this.set.add(element);
            if (added)
                this.hash += hash0(element.hashCode());
            return added;
        }

        // Removes an element from a set
        boolean remove(E element) {
            boolean removed = this.set.remove(element);
            if (removed)
                this.hash -= hash0(element.hashCode());
            return removed;
        }

        @Override
        public @NotNull Iterator<E> iterator() {
            return this.set.iterator();
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        // We add a special case where if referenceCount == 0, then the set is being used as a temporary set
        // inside findMapWith/Without, so it uses identity equality instead of full equality.
        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof PooledObjectLinkedOpenHashSet))
                return false;
            if (this.referenceCount == 0) {
                return other == this;
            } else {
                if (other == this)
                    return false;
                return this.hash == ((PooledObjectLinkedOpenHashSet<?>) other).hash
                        && this.set.equals(((PooledObjectLinkedOpenHashSet<?>) other).set);
            }
        }
    }
}