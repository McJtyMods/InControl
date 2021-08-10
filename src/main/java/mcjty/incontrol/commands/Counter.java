package mcjty.incontrol.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Counter<T> {
    private Map<T, Integer> internalMap = new HashMap();

    public Counter() {
    }

    public void add(T key) {
        if (!this.internalMap.containsKey(key)) {
            this.internalMap.put(key, 0);
        }

        this.internalMap.put(key, (Integer)this.internalMap.get(key) + 1);
    }

    public Map<T, Integer> getMap() {
        return this.internalMap;
    }

    public int get(T key) {
        return this.internalMap.containsKey(key) ? (Integer)this.internalMap.get(key) : 0;
    }

    public T getMostOccuring() {
        T max = null;
        int maxCount = -1;
        Iterator var3 = this.internalMap.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<T, Integer> entry = (Map.Entry)var3.next();
            if ((Integer)entry.getValue() > maxCount) {
                maxCount = (Integer)entry.getValue();
                max = entry.getKey();
            }
        }

        return max;
    }
}
