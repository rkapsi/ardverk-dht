package org.ardverk.dht.storage.message;

import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.storage.Constants;

public class DefaultRequest extends Request {

    public DefaultRequest(Contact creator, ValueEntity entity) {
        super(entity);
        
        addHeader(Constants.CLIENT_ID, creator.getId().toHexString());
    }
}
