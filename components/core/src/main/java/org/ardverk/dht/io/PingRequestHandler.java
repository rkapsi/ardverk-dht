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

package org.ardverk.dht.io;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.message.MessageType;
import org.ardverk.dht.message.PingRequest;
import org.ardverk.dht.message.PingResponse;
import org.ardverk.dht.message.RequestMessage;
import org.ardverk.dht.message.ResponseMessage;


/**
 * The {@link PingRequestHandler} handles {@link PingRequest} 
 * ({@link MessageType#PING}) messages.
 */
@Singleton
public class PingRequestHandler extends AbstractRequestHandler {
    
    @Inject
    public PingRequestHandler(Provider<MessageDispatcher> messageDispatcher) {
        super(messageDispatcher);
    }

    public PingResponse createResponse(PingRequest request) {
        MessageFactory factory = getMessageFactory();
        return factory.createPingResponse(request);
    }
    
    @Override
    public ResponseMessage handleRequest(RequestMessage request) throws IOException {
        return createResponse((PingRequest)request);
    }
}