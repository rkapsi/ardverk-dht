/*
 * Copyright 2009-2010 Roger Kapsi
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

package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.MessageType;
import com.ardverk.dht.message.PingRequest;
import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.message.RequestMessage;

/**
 * The {@link PingRequestHandler} handles {@link PingRequest} 
 * ({@link MessageType#PING}) messages.
 */
public class PingRequestHandler extends AbstractRequestHandler {
    
    public PingRequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }

    @Override
    public void handleRequest(RequestMessage message) throws IOException {
        PingRequest request = (PingRequest)message;
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        PingResponse response = factory.createPingResponse(request);
        send(request, response);
    }
}