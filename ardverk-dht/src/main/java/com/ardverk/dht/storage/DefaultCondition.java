package com.ardverk.dht.storage;

import com.ardverk.dht.storage.Database.Condition;

public enum DefaultCondition implements Condition {
    SUCCESS,
    FAILURE;

    @Override
    public boolean isSuccess() {
        return this == SUCCESS;
    }
    
    @Override
    public String stringValue() {
        return name();
    }
}