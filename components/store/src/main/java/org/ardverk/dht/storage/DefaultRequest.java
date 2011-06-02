package org.ardverk.dht.storage;

import org.ardverk.dht.routing.Contact;

public class DefaultRequest extends Request {

    public DefaultRequest(Contact creator, ValueEntity entity) {
        super(entity);
        
        addHeader(Constants.CLIENT_ID, creator.getId().toHexString());
    }
}
