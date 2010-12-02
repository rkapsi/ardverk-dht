package com.ardverk.dht.ui;

import java.awt.Component;
import java.awt.Graphics;

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.Message;

interface Painter {

    public static enum EventType {
        MESSAGE_SENT,
        MESSAGE_RECEIVED;
    }
    
    public void clear();
    
    public void paint(Component c, Graphics g);
    
    public void handleEvent(EventType type, KUID contactId, Message message);
}
