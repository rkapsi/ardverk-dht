/*
 * Copyright 2009-2012 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.routing.Contact;

/**
 * A default implementation of {@link BootstrapEntity}.
 */
public class BootstrapEntity extends Entity {

    private final PingEntity pingEntity;
    
    private final NodeEntity nodeEntity;
    
    public BootstrapEntity(PingEntity pingEntity, 
            NodeEntity nodeEntity) {
        super(EntityUtils.getTimeInMillis(pingEntity, nodeEntity), 
                TimeUnit.MILLISECONDS);
        
        this.pingEntity = pingEntity;
        this.nodeEntity = nodeEntity;
    }

    public PingEntity getPingEntity() {
        return pingEntity;
    }

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