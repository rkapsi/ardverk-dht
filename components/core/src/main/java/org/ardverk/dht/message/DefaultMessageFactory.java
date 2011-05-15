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

import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.Localhost;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.lang.Precoditions;


public class DefaultMessageFactory extends AbstractMessageFactory {

    private final Localhost localhost;
    
    public DefaultMessageFactory(Localhost localhost) {
        this(localhost.getId().length(), localhost);
    }
    
    public DefaultMessageFactory(int length, Localhost localhost) {
        super(length);
        this.localhost = Precoditions.notNull(localhost, "localhost");
    }
    
    @Override
    public PingRequest createPingRequest(Contact dst) {
        return createPingRequest(dst.getRemoteAddress());
    }

    @Override
    public PingRequest createPingRequest(SocketAddress dst) {
        MessageId messageId = createMessageId(dst);
        return new DefaultPingRequest(messageId, localhost, dst);
    }

    @Override
    public PingResponse createPingResponse(PingRequest request) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        return new DefaultPingResponse(messageId, localhost, address);
    }

    @Override
    public NodeRequest createNodeRequest(Contact dst, KUID key) {
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = createMessageId(address);
        return new DefaultNodeRequest(messageId, localhost, address, key);
    }

    @Override
    public NodeResponse createNodeResponse(LookupRequest request, Contact[] contacts) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        return new DefaultNodeResponse(messageId, localhost, address, contacts);
    }

    @Override
    public ValueRequest createValueRequest(Contact dst, Key key) {
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = createMessageId(address);
        return new DefaultValueRequest(messageId, localhost, address, key);
    }

    @Override
    public ValueResponse createValueResponse(LookupRequest request, Value value) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        return new DefaultValueResponse(messageId, localhost, address, value);
    }

    @Override
    public StoreRequest createStoreRequest(Contact dst, Key key, 
            Value value) {
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = createMessageId(address);
        
        return new DefaultStoreRequest(messageId, localhost, 
                address, key, value);
    }

    @Override
    public StoreResponse createStoreResponse(StoreRequest request, Value value) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        return new DefaultStoreResponse(messageId, localhost, address, value);
    }
}