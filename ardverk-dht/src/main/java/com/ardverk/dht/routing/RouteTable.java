package com.ardverk.dht.routing;

import java.net.SocketAddress;

import com.ardverk.dht.ContactPinger;
import com.ardverk.dht.KUID;
import com.ardverk.dht.KeyFactory;

public interface RouteTable {
    
    public void setContactPinger(ContactPinger pinger);
    
    public void add(Contact contact);
    
    public ContactFactory getContactFactory();
    
    public KeyFactory getKeyFactory() ;
    
    public int getK();
    
    public Contact[] select(KUID contactId);
    
    public Contact[] select(KUID contactId, int count);
    
    public void failure(KUID contactId, SocketAddress address);
}
