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

package org.ardverk.dht.io;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.ardverk.dht.KUID;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.message.MessageType;
import org.ardverk.dht.message.NodeRequest;
import org.ardverk.dht.message.RequestMessage;
import org.ardverk.dht.message.ResponseMessage;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;


/**
 * The {@link NodeRequestHandler} handles {@link NodeRequest} 
 * ({@link MessageType#FIND_NODE}) messages. 
 */
@Singleton
public class NodeRequestHandler extends AbstractRequestHandler {
    
    private final RouteTable routeTable;
    
    @Inject
    public NodeRequestHandler(
            Provider<MessageDispatcher> messageDispatcher, 
            RouteTable routeTable) {
        super(messageDispatcher);
        
        this.routeTable = routeTable;
    }
    
    @Override
    public ResponseMessage handleRequest(RequestMessage message) throws IOException {
        NodeRequest request = (NodeRequest)message;
        
        KUID lookupId = request.getId();
        
        // This is an idea where I'm not sure if it's improving anything 
        // or not. In short we're excluding the localhost from the result 
        // set. The other guy knows us already and our information is in 
        // the message header anyways. There is therefore no reason to
        // send it again and the remote host can determinate computationally 
        // if we were supposed to be in the result set or not. 
        //
        // Instead we could send back some other contact. It's maybe not
        // useful in the context of this lookup but maybe helps the remote
        // host to keep its RouteTable "more" fresh.
        
        /*Contact localhost = routeTable.getLocalhost();
        int k = routeTable.getK();
        
        Contact[] contacts = routeTable.select(lookupId, k+1);
        if (k < contacts.length) {
            Contact[] kContacts = new Contact[k];
            for (int i = 0, j = 0; i < contacts.length 
                    && j < kContacts.length; i++) {
                
                Contact contact = contacts[i];
                if (!contact.equals(localhost)) {
                    kContacts[j++] = contact;
                }
            }
            contacts = kContacts;
        }*/
        
        Contact[] contacts = routeTable.select(lookupId);
        
        MessageFactory factory = getMessageFactory();
        return factory.createNodeResponse(request, contacts);
    }
}