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

package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;

public abstract class AbstractRequestHandler 
        extends AbstractMessageHandler implements RequestHandler {

    public AbstractRequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }
    
    public void send(RequestMessage request, 
            ResponseMessage message) throws IOException {
        messageDispatcher.send(request.getContact(), message);
    }
    
    public void send(Contact dst, ResponseMessage message) throws IOException {
        messageDispatcher.send(dst, message);
    }
}