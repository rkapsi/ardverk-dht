package com.ardverk.dht.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeRequest;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

public class NodeRequestHandler extends AbstractRequestHandler {
    
    public final List<KUID> c = Collections.synchronizedList(new ArrayList<KUID>());
    
    private final RouteTable routeTable;
    
    public NodeRequestHandler(
            MessageDispatcher messageDispatcher, 
            RouteTable routeTable) {
        super(messageDispatcher);
        
        this.routeTable = Arguments.notNull(routeTable, "routeTable");
    }

    @Override
    public void handleRequest(RequestMessage message) throws IOException {
        
        //System.out.println("REQUEST: " + message);
        c.add(message.getContact().getId());
        
        NodeRequest request = (NodeRequest)message;
        KUID lookupId = request.getKey();
        
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
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        NodeResponse response = factory.createNodeResponse(request, contacts);
        send(request, response);
    }
}
