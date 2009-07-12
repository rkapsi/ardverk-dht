package com.ardverk.dht.routing;

import java.net.SocketAddress;
import java.util.Map;

import com.ardverk.dht.KUID;
import com.ardverk.dht.KeyFactory;
import com.ardverk.dht.routing.Contact.Type;

public class DefaultContactFactory extends AbstractContactFactory {

    public DefaultContactFactory(KeyFactory keyFactory) {
        super(keyFactory);
    }

    @Override
    public Contact createUncharted(KUID contactId, int instanceId,
            SocketAddress address, Map<?, ?> attributes)  {
        
        return new DefaultContact(Type.UNCHARTED, contactId, 
                instanceId, address, attributes);
    }
    
    @Override
    public Contact createCharted(KUID contactId, int instanceId,
            SocketAddress address, Map<?, ?> attributes)  {
        
        return new DefaultContact(Type.CHARTED, contactId, 
                instanceId, address, attributes);
    }

    @Override
    public Contact merge(Contact existing, Contact contact) {
        return new DefaultContact(existing, contact);
    }
}
