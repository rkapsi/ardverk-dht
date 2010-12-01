package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.routing.Contact;

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
    
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("PONG: ").append(pingEntity.getContact()).append("\n");
        Contact[] contacts = nodeEntity.getContacts();
        buffer.append("CONTACTS ").append(contacts.length).append("\n");
        for (Contact contact : contacts) {
            buffer.append(" ").append(contact);
        }
        return buffer.toString();
    }
}
