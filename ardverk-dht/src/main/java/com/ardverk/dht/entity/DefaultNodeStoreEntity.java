package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

public class DefaultNodeStoreEntity extends DefaultStoreEntity 
        implements NodeStoreEntity {

    private final NodeEntity nodeEntity;
    
    private final StoreEntity storeEntity;
    
    public DefaultNodeStoreEntity(NodeEntity nodeEntity, StoreEntity storeEntity) {
        super(storeEntity.getStoreResponses(), 
                EntityUtils.getTimeInMillis(nodeEntity, storeEntity), 
                TimeUnit.MILLISECONDS);
        
        this.nodeEntity = nodeEntity;
        this.storeEntity = storeEntity;
    }

    @Override
    public NodeEntity getNodeEntity() {
        return nodeEntity;
    }
    
    public StoreEntity getStoreEntity() {
        return storeEntity;
    }
}
