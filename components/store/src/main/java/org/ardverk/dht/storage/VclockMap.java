package org.ardverk.dht.storage;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ardverk.collection.CollectionUtils;
import org.ardverk.version.Occured;

class VclockMap {
    
    private final Map<String, Entry> map 
        = new LinkedHashMap<String, Entry>();
    
    public void upsert(Vclock vclock, Context context, ValueEntity value) {
        
        Iterator<Entry> it = map.values().iterator();
        while (it.hasNext()) {
            
            Vclock existing = it.next().vclock;
            
            Occured occured = VclockUtils.compare(existing, vclock);
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
        
        String vtag = vclock.getVTag();
        map.put(vtag, new Entry(vclock, context, value));
    }
    
    public boolean remove(Vclock vclock) {
        return map.remove(vclock.getVTag()) != null;
    }
    
    public Entry value(String vtag) {
        return map.get(vtag);
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
        
        private final Vclock vclock;
        
        private final Context context;
        
        private final ValueEntity entity;

        public Entry(Vclock vclock, Context context, ValueEntity entity) {
            this.vclock = vclock;
            this.context = context;
            this.entity = entity;
        }

        public Vclock getVclock() {
            return vclock;
        }
        
        public Context getContext() {
            return context;
        }

        public ValueEntity getValueEntity() {
            return entity;
        }
    }
}
