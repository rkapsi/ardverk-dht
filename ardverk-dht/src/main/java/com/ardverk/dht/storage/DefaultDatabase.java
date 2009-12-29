package com.ardverk.dht.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultDatabase extends AbstractDatabase {

    public static enum DefaultStatus implements Status {
        SUCCESS,
        FAILURE;

        @Override
        public boolean isSuccess() {
            return this == SUCCESS;
        }
    }
    
    private final Map<KUID, DefaultValueEntity> database 
        = new ConcurrentHashMap<KUID, DefaultValueEntity>();
    
    @Override
    public ValueEntity get(KUID key) {
        return database.get(key);
    }

    @Override
    public Status store(Contact src, KUID key, byte[] value) {
        if (value != null) {
            database.put(key, new DefaultValueEntity(src, key, value));
        } else {
            database.remove(key);
        }
        
        return DefaultStatus.SUCCESS;
    }
    
    @Override
    public byte[] lookup(KUID key) {
        ValueEntity entity = database.get(key);
        return entity != null ? entity.getValue() : null;
    }

    @Override
    public int size() {
        return database.size();
    }
    
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        
        for (ValueEntity entity : database.values()) {
            buffer.append(entity).append("\n");
        }
        
        return buffer.toString();
    }
}
