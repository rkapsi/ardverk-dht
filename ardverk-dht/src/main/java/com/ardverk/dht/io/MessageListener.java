package com.ardverk.dht.io;

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.Message;

/**
 * 
 */
public interface MessageListener {
    
    /**
     * 
     */
    public void handleMessageSent(KUID contactId, Message message);
    
    /**
     * 
     */
    public void handleMessageReceived(Message message);
}