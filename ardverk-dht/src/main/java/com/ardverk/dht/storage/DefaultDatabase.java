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
    
    private final Map<KUID, ValueTuple> database 
        = new ConcurrentHashMap<KUID, ValueTuple>();
    
    @Override
    public ValueTuple get(KUID key) {
        return database.get(key);
    }

    @Override
    public Condition store(ValueTuple value) {
        KUID primaryKey = value.getPrimaryKey();
        
        if (!value.isEmpty()) {
            database.put(primaryKey, value);
        } else {
            database.remove(primaryKey);
        }
        
        return DefaultCondition.SUCCESS;
    }
    
    @Override
    public int size() {
        return database.size();
    }
    
    @Override
    public ValueTuple[] select(final KUID key) {
        ValueTuple[] values = values();
        Comparator<ValueTuple> comparator 
                = new Comparator<ValueTuple>() {
            @Override
            public int compare(ValueTuple o1, ValueTuple o2) {
                KUID xor1 = o1.getPrimaryKey().xor(key);
                KUID xor2 = o2.getPrimaryKey().xor(key);
                return xor1.compareTo(xor2);
            }
        };
        
        Arrays.sort(values, comparator);
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
