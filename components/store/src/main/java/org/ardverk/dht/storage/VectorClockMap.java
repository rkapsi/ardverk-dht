package org.ardverk.dht.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ardverk.collection.CollectionUtils;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.version.Occured;
import org.ardverk.version.VectorClock;

public class VectorClockMap<K, V extends Value> {
    
    private final List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>();
    
    public void upsert(VectorClock<K> clock, V value) {
        Entry<K, V> entry = new Entry<K, V>(clock, value);
        
        Iterator<Entry<K, V>> it = entries.iterator();
        while (it.hasNext()) {
            Entry<K, V> existing = it.next();
            
            Occured occured = existing.compareTo(clock);
            if (occured == Occured.AFTER) {
                it.remove();
            }
            
            // TODO: What about BEFORE and IDENTICIAL?
        }
        
        entries.add(entry);
    }
    
    public boolean remove(VectorClock<K> clock) {
        if (clock == null && entries.size() == 1) {
            entries.clear();
            return true;
        }
        
        boolean success = false;
        Iterator<Entry<K, V>> it = entries.iterator();
        while (it.hasNext()) {
            Entry<K, V> existing = it.next();
            
            Occured occured = existing.compareTo(clock);
            if (occured == Occured.IDENTICAL) {
                it.remove();
                success = true;
            }
        }
        return success;
    }
    
    public V value() {
        if (!entries.isEmpty()) {
            Entry<?, V> entry = CollectionUtils.last(entries);
            return entry.value;
        }
        return null;
    }
    
    public Value[] values() {
        Value[] values = new Value[entries.size()];
        int index = 0;
        for (Entry<?, V> entry : entries) {
            values[index++] = entry.value;
        }
        return values;
    }
    
    public int size() {
        return entries.size();
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    private static class Entry<K, V extends Value> {
        
        private final VectorClock<K> clock;
        
        private final V value;
        
        public Entry(VectorClock<K> clock, V value) {
            this.clock = clock;
            this.value = value;
        }
        
        public Occured compareTo(VectorClock<K> other) {
            return VectorClockUtils.compare(clock, other);
        }
    }
}
