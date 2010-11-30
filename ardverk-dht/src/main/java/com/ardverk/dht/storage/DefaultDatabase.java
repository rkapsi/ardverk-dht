package com.ardverk.dht.storage;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ardverk.dht.utils.XorComparator;

public class DefaultDatabase extends AbstractDatabase {

    public static enum DefaultCondition implements Condition {
        SUCCESS,
        FAILURE;

        @Override
        public boolean isSuccess() {
            return this == SUCCESS;
        }
    }
    
    private final Map<Key, ValueTuple> database 
        = new ConcurrentHashMap<Key, ValueTuple>();
    
    @Override
    public ValueTuple get(Key key) {
        return database.get(key);
    }

    @Override
    public Condition store(ValueTuple tuple) {
        Key key = tuple.getKey();
        
        if (!tuple.isEmpty()) {
            database.put(key, tuple);
        } else {
            database.remove(key);
        }
        
        return DefaultCondition.SUCCESS;
    }
    
    @Override
    public int size() {
        return database.size();
    }
    
    @Override
    public ValueTuple[] select(Key key) {
        ValueTuple[] values = values();
        Arrays.sort(values, new XorComparator(key));
        return values;
    }
    
    @Override
    public ValueTuple[] values() {
        return database.values().toArray(new ValueTuple[0]);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        
        for (ValueTuple entity : database.values()) {
            buffer.append(entity).append("\n");
        }
        
        return buffer.toString();
    }
}
