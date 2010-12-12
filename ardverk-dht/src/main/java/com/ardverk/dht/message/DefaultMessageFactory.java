/*
 * Copyright 2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Database.Condition;
import com.ardverk.dht.storage.ValueTuple;

public class DefaultMessageFactory extends AbstractMessageFactory {

    private final Contact localhost;
    
    public DefaultMessageFactory(int length, Contact localhost) {
        super(length);
        
        this.localhost = Arguments.notNull(localhost, "localhost");
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
    public ValueRequest createValueRequest(Contact dst, KUID key) {
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = createMessageId(address);
        return new DefaultValueRequest(messageId, localhost, address, key);
    }

    @Override
    public ValueResponse createValueResponse(LookupRequest request, ValueTuple tuple) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        
        return new DefaultValueResponse(messageId, localhost, address, tuple);
    }

    @Override
    public StoreRequest createStoreRequest(Contact dst, ValueTuple tuple) {
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = createMessageId(address);
        return new DefaultStoreRequest(messageId, localhost, address, tuple);
    }

    @Override
    public StoreResponse createStoreResponse(StoreRequest request, Condition condition) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        return new DefaultStoreResponse(messageId, localhost, address, condition);
    }
}