package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.storage.ValueTuple;

public class DefaultValueEntity extends AbstractEntity implements ValueEntity {
    
    private final NodeEntity nodeEntity;
    
    private final ValueTuple[] values;
    
    public DefaultValueEntity(NodeEntity nodeEntity, ValueTuple[] values) {
        super(nodeEntity.getTimeInMillis(), TimeUnit.MILLISECONDS);
        
        this.nodeEntity = nodeEntity;
        this.values = values;
    }
    
    @Override
    public ValueTuple getValue() {
        return values[0];
    }
    
    public ValueTuple[] getValues() {
        return values;
    }
    
    public NodeEntity getNodeEntity() {
        return nodeEntity;
    }
}
