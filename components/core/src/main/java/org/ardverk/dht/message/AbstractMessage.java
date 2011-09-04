/*
 * Copyright 2009-2011 Roger Kapsi
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

package org.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.dht.routing.Contact2;
import org.ardverk.dht.rsrc.NoValue;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.lang.Epoch;

/**
 * An abstract implementation of {@link Message}.
 */
public abstract class AbstractMessage implements Message, Epoch {

    private final long creationTime = System.currentTimeMillis();
    
    private final MessageId messageId;
    
    private final Contact2 contact;
    
    private final SocketAddress address;
    
    private final Value value;
    
    public AbstractMessage(MessageId messageId, Contact2 contact, 
            SocketAddress address) {
        this(messageId, contact, address, NoValue.EMPTY);
    }
    
    public AbstractMessage(MessageId messageId, Contact2 contact, 
            SocketAddress address, Value value) {
        
        this.messageId = messageId;
        this.contact = contact;
        this.address = address;
        this.value = value;
    }
    
    @Override
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public MessageId getMessageId() {
        return messageId;
    }

    @Override
    public Contact2 getContact() {
        return contact;
    }
    
    @Override
    public SocketAddress getAddress() {
        return address;
    }
    
    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() 
            + "(" + messageId + ", " + contact + ", " + address + ")";
    }
}