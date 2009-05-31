package com.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.Map;

import com.ardverk.dht.KUID;
import com.ardverk.dht.KeyFactory;
import com.ardverk.dht.routing.Contact.State;

public class DefaultContactFactory extends AbstractContactFactory {

    public DefaultContactFactory(KeyFactory keyFactory) {
        super(keyFactory);
    }

    @Override
    public Contact createUnknown(KUID contactId, int instanceId,
            SocketAddress address, Map<?, ?> attributes)  {
        
        return new DefaultContact(contactId, instanceId, 
                address, State.UNKNOWN, attributes);
    }
    
    @Override
    public Contact createAlive(KUID contactId, int instanceId,
            SocketAddress address, Map<?, ?> attributes)  {
        
        return new DefaultContact(contactId, instanceId, 
                address, State.ALIVE, attributes);
    }

    @Override
    public Contact merge(Contact existing, Contact contact) {
        return new DefaultContact(existing, contact);
    }
}
