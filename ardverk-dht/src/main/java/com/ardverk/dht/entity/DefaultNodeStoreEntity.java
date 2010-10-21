package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

public class DefaultNodeStoreEntity extends DefaultStoreEntity 
        implements NodeStoreEntity {

    private final NodeEntity nodeEntity;
    
    public DefaultNodeStoreEntity(NodeEntity nodeEntity, 
            long time, TimeUnit unit) {
        super(time, unit);
        
        this.nodeEntity = nodeEntity;
    }

    @Override
    public NodeEntity getNodeEntity() {
        return nodeEntity;
    }
}
