package org.ardverk.dht.storage;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ardverk.collection.CollectionUtils;
import org.ardverk.dht.KUID;
import org.ardverk.version.Occured;
import org.ardverk.version.VectorClock;

public class VclockMap {
    
    private final Map<VectorClock<KUID>, Entry> map 
        = new LinkedHashMap<VectorClock<KUID>, Entry>();
    
    public void upsert(VectorClock<KUID> clock, Context context, ValueEntity value) {
        
        Iterator<VectorClock<KUID>> it = map.keySet().iterator();
        while (it.hasNext()) {
            VectorClock<KUID> existing = it.next();
            
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
        
        map.put(clock, new Entry(context, value));
    }
    
    public boolean remove(VectorClock<?> clock) {
        return map.remove(clock) != null;
    }
    
    public Entry value() {
        return CollectionUtils.last(map.values());
    }
    
    public Entry[] values() {
        return CollectionUtils.toArray(map.values(), Entry.class);
    }
    
    public int size() {
        return map.size();
    }
    
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    public static class Entry {
        
        private final Context context;
        
        private final ValueEntity entity;

        public Entry(Context context, ValueEntity entity) {
            this.context = context;
            this.entity = entity;
        }

        public Context getContext() {
            return context;
        }

        public ValueEntity getValueEntity() {
            return entity;
        }
    }
}
