package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectDatabase2 extends AbstractObjectDatabase {

    private static final Logger LOG 
        = LoggerFactory.getLogger(ObjectDatabase2.class);

    @Override
    protected Response handlePut(Contact src, Key key, Request request,
            InputStream in) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Response handleDelete(Contact src, Key key, Request request,
            InputStream in) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response get(Contact src, Key key) {
        // TODO Auto-generated method stub
        return null;
    }
}
