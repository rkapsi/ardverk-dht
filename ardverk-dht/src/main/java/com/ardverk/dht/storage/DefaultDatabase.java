package com.ardverk.dht.storage;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultDatabase extends AbstractDatabase {

    public static enum DefaultCondition implements Condition {
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
    public Condition store(Contact src, KUID key, byte[] value) {
        if (value != null) {
            database.put(key, new DefaultValueEntity(src, key, value));
        } else {
            database.remove(key);
        }
        
        return DefaultCondition.SUCCESS;
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
    public ValueEntity[] select(final KUID key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        
        ValueEntity[] values = values();
        Comparator<ValueEntity> comparator 
                = new Comparator<ValueEntity>() {
            @Override
            public int compare(ValueEntity o1, ValueEntity o2) {
                KUID xor1 = o1.getKey().xor(key);
                KUID xor2 = o2.getKey().xor(key);
                return xor1.compareTo(xor2);
            }
        };
        
        Arrays.sort(values, comparator);
        return values;
    }
    
    

    @Override
    public ValueEntity[] values() {
        return database.values().toArray(new ValueEntity[0]);
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
