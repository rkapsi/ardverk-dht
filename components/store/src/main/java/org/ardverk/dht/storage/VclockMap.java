package org.ardverk.dht.storage;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ardverk.collection.CollectionUtils;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.version.Occured;
import org.ardverk.version.VectorClock;

public class VclockMap<K, V extends Value> {
    
    private final Map<VectorClock<K>, V> map 
        = new LinkedHashMap<VectorClock<K>, V>();
    
    public void upsert(VectorClock<K> clock, V value) {
        
        Iterator<VectorClock<K>> it = map.keySet().iterator();
        while (it.hasNext()) {
            VectorClock<K> existing = it.next();
            
            Occured occured = VclockUtils.compare(existing, clock);
            switch (occured) {
                case AFTER:
                    it.remove();
                    break;
                case BEFORE:
                case IDENTICAL:
                    if (map.size() == 1) {
                        return;
                    }
                    break;
            }
        }
        
        map.put(clock, value);
    }
    
    public boolean remove(VectorClock<K> clock) {
        return map.remove(clock) != null;
    }
    
    public V value() {
        return CollectionUtils.last(map.values());
    }
    
    /*public Value[] values() {
        return values(Value.class);
    }*/
    
    public V[] values(Class<V> clazz) {
        return CollectionUtils.toArray(map.values(), clazz);
    }
    
    public int size() {
        return map.size();
    }
    
    public boolean isEmpty() {
        return map.isEmpty();
    }
}
