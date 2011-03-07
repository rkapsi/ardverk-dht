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

import org.ardverk.dht.io.transport.Endpoint;
import org.ardverk.dht.message.RequestMessage;
import org.ardverk.dht.message.ResponseMessage;
import org.ardverk.dht.routing.Contact;

/**
 * An abstract base class for {@link RequestHandler}s.
 */
abstract class AbstractRequestHandler 
        extends AbstractMessageHandler implements RequestHandler {

    public AbstractRequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }
    
    protected void send(Endpoint endpoint, RequestMessage request, 
            ResponseMessage message) throws IOException {
        Contact src = request.getContact();
        messageDispatcher.send(endpoint, src, message);
    }
}