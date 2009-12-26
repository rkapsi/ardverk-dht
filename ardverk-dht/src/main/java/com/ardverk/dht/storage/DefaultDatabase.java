package com.ardverk.dht.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultDatabase implements Database {

    private final Map<KUID, ValueEntity> database 
        = new ConcurrentHashMap<KUID, ValueEntity>();
    
    @Override
    public byte[] get(KUID key) {
        ValueEntity entity = database.get(key);
        return entity != null ? entity.getValue() : null;
    }

    @Override
    public byte[] store(Contact src, KUID key, byte[] value) {
        ValueEntity existing = null;
        if (value != null) {
            existing = database.put(key, new ValueEntity(src, key, value));
        } else {
            existing = database.remove(key);
        }
        
        return existing != null ? existing.getValue() : null;
    }
    
    private static class ValueEntity {
        
        private final Contact src;
        
        private final KUID key;
        
        private final byte[] value;
        
        public ValueEntity(Contact src, KUID key, byte[] value) {
            this.src = src;
            this.key = key;
            this.value = value;
        }

        public Contact getContact() {
            return src;
        }

        public KUID getKey() {
            return key;
        }

        public byte[] getValue() {
            return value;
        }
    }
}
