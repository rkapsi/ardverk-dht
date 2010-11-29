package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

public class DefaultBootstrapEntity extends AbstractEntity 
        implements BootstrapEntity {

    private final PingEntity pingEntity;
    
    private final NodeEntity nodeEntity;
    
    public DefaultBootstrapEntity(PingEntity pingEntity, 
            NodeEntity nodeEntity) {
        super(EntityUtils.getTimeInMillis(pingEntity, nodeEntity), 
                TimeUnit.MILLISECONDS);
        
        this.pingEntity = pingEntity;
        this.nodeEntity = nodeEntity;
    }

    @Override
    public PingEntity getPingEntity() {
        return pingEntity;
    }

    @Override
    public NodeEntity getNodeEntity() {
        return nodeEntity;
    }
}
