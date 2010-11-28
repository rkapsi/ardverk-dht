package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

public class DefaultNodeStoreEntity extends DefaultStoreEntity 
        implements NodeStoreEntity {

    private final NodeEntity nodeEntity;
    
    private final StoreEntity storeEntity;
    
    public DefaultNodeStoreEntity(NodeEntity nodeEntity, StoreEntity storeEntity) {
        super(storeEntity.getStoreResponses(), 
                time(nodeEntity, storeEntity, TimeUnit.MILLISECONDS), 
                TimeUnit.MILLISECONDS);
        
        this.nodeEntity = nodeEntity;
        this.storeEntity = storeEntity;
    }

    @Override
    public NodeEntity getNodeEntity() {
        return nodeEntity;
    }
    
    private static long time(NodeEntity nodeEntity, 
            StoreEntity storeEntity, TimeUnit unit) {
        return nodeEntity.getTime(unit) + storeEntity.getTime(unit);
    }
}
