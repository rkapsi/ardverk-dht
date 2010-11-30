package com.ardverk.dht.entity;

public interface BootstrapEntity extends Entity {

    /**
     * 
     */
    public PingEntity getPingEntity();
    
    /**
     * 
     */
    public NodeEntity getNodeEntity();
}
