package com.ardverk.dht.io;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.MessageId;
import com.ardverk.dht.message.NodeRequest;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.PingRequest;
import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.StoreRequest;
import com.ardverk.dht.message.StoreResponse;
import com.ardverk.dht.message.ValueRequest;
import com.ardverk.dht.message.ValueResponse;
import com.ardverk.dht.routing.Contact2;

public class RequestEntity {

    private final KUID contactId;
    
    private final SocketAddress address;
    
    private final RequestMessage request;
    
    public RequestEntity(KUID contactId, 
            SocketAddress address, RequestMessage request) {
        
        this.contactId = contactId;
        this.address = address;
        this.request = request;
    }

    public KUID getContactId() {
        return contactId;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public RequestMessage getRequest() {
        return request;
    }
    
    MessageId getMessageId() {
        return request.getMessageId();
    }
    
    /**
     * Checks if the {@link ResponseMessage} of the expected type.
     */
    boolean check(ResponseMessage response) {
        return checkType(response) && checkContactId(response);
    }
    
    /**
     * Checks the {@link Contact2}'s {@link KUID}
     */
    private boolean checkContactId(ResponseMessage response) {
        if (contactId == null) {
            return (response instanceof PingResponse);
        }
        
        Contact2 contact = response.getContact();
        KUID otherId = contact.getContactId();
        
        return contactId.equals(otherId);
    }
    
    /**
     * Checks the type of the {@link ResponseMessage}.
     */
    private boolean checkType(ResponseMessage response) {
        if (request instanceof PingRequest) {
            return response instanceof PingResponse;
        } else if (request instanceof NodeRequest) {
            return response instanceof NodeResponse;
        } else if (request instanceof ValueRequest) {
            return response instanceof ValueResponse 
                || response instanceof NodeResponse;
        } else if (request instanceof StoreRequest) {
            return response instanceof StoreResponse;
        }
        
        return false;
    }
}
