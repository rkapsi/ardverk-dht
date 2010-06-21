package com.ardverk.dht.storage;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ardverk.dht.KUID;

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
    
    private static class XorComparator implements Comparator<ValueTuple> {
        
        private final KUID primaryKey;
        
        public XorComparator(Key key) {
            this.primaryKey = key.getPrimaryKey();
        }

        private KUID xor(ValueTuple tuple) {
            return tuple.getKey().getPrimaryKey().xor(primaryKey);
        }
        
        @Override
        public int compare(ValueTuple o1, ValueTuple o2) {
            return xor(o1).compareTo(xor(o2));
        }
    }
}
