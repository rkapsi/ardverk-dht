package com.ardverk.dht.routing;

import java.io.Serializable;

import com.ardverk.dht.KUID;
import com.ardverk.dht.KeyFactory;

public interface RouteTable extends Serializable {
    
    public void add(Contact contact);
    
    public ContactFactory getContactFactory();
    
    public KeyFactory getKeyFactory() ;
    
    public int getK();
    
    public Contact[] select(KUID contactId);
    
    public Contact[] select(KUID contactId, int count);
}
