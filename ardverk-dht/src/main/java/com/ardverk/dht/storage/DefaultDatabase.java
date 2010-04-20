package com.ardverk.dht.storage;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact2;

public class DefaultDatabase extends AbstractDatabase {

    public static enum DefaultCondition implements Condition {
        SUCCESS,
        FAILURE;

        @Override
        public boolean isSuccess() {
            return this == SUCCESS;
        }
    }
    
    private final Map<KUID, DefaultValue> database 
        = new ConcurrentHashMap<KUID, DefaultValue>();
    
    @Override
    public Value get(KUID key) {
        return database.get(key);
    }

    @Override
    public Condition store(Contact2 src, KUID key, byte[] value) {
        if (value != null) {
            database.put(key, new DefaultValue(src, key, value));
        } else {
            database.remove(key);
        }
        
        return DefaultCondition.SUCCESS;
    }
    
    @Override
    public byte[] lookup(KUID key) {
        Value entity = database.get(key);
        return entity != null ? entity.getValue() : null;
    }

    @Override
    public int size() {
        return database.size();
    }
    
    @Override
    public Value[] select(final KUID key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        
        Value[] values = values();
        Comparator<Value> comparator 
                = new Comparator<Value>() {
            @Override
            public int compare(Value o1, Value o2) {
                KUID xor1 = o1.getKey().xor(key);
                KUID xor2 = o2.getKey().xor(key);
                return xor1.compareTo(xor2);
            }
        };
        
        Arrays.sort(values, comparator);
        return values;
    }
    
    @Override
    public Value[] values() {
        return database.values().toArray(new Value[0]);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        
        for (Value entity : database.values()) {
            buffer.append(entity).append("\n");
        }
        
        return buffer.toString();
    }
}
