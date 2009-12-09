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
    public Contact createUnsolicited(KUID contactId, int instanceId,
            SocketAddress remoteAddress, SocketAddress address, 
            Map<?, ?> attributes)  {
        
        return new DefaultContact(Type.UNSOLICITED, contactId, 
                instanceId, remoteAddress, address, attributes);
    }
    
    @Override
    public Contact createSolicited(KUID contactId, int instanceId,
            SocketAddress remoteAddress, SocketAddress address, 
            Map<?, ?> attributes)  {
        
        return new DefaultContact(Type.SOLICITED, contactId, 
                instanceId, remoteAddress, address, attributes);
    }
    
    @Override
    public Contact createUnknown(KUID contactId, int instanceId,
            SocketAddress remoteAddress, SocketAddress address, 
            Map<?, ?> attributes)  {
        
        return new DefaultContact(Type.UNKNOWN, contactId, 
                instanceId, remoteAddress, address, attributes);
    }

    @Override
    public Contact merge(Contact existing, Contact contact) {
        return new DefaultContact(existing, contact);
    }
}
