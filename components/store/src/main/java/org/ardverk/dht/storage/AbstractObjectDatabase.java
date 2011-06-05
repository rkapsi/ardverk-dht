package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractObjectDatabase extends AbstractDatabase {

    private static final Logger LOG 
        = LoggerFactory.getLogger(AbstractObjectDatabase.class);
    
    @Override
    public Response store(Contact src, Key key, Value value) {
        
        InputStream in = null;
        try {
            in = value.getContent();
            
            Request request = Request.valueOf(in);
            Method method = request.getMethod();
            
            Response response = null;
            switch (method) {
                case PUT:
                    response = handlePut(src, key, request, in);
                    break;
                case GET:
                    response = handleGet(src, key, request, in);
                    break;
                case DELETE:
                    response = handleDelete(src, key, request, in);
                    break;
                default:
                    throw new IllegalArgumentException("method=" + method);
            }
            
            if (response == null) {
                throw new IllegalStateException();
            }
            
            return response;
            
        } catch (Exception err) {
            LOG.error("Exception", err);
            return ExceptionResponse.create(err);
        } finally {
            IoUtils.close(in);
        }
    }

    protected abstract Response handlePut(Contact src, Key key, 
            Request request, InputStream in) throws IOException;
    
    protected Response handleGet(Contact src, Key key, 
            Request request, InputStream in) throws IOException {
        Response response = get(src, key);
        if (response == null) {
            response = Response.NOT_FOUND;
        }
        return response;
    }
    
    protected abstract Response handleDelete(Contact src, Key key, 
            Request request, InputStream in) throws IOException;
    
    @Override
    public abstract Response get(Contact src, Key key);
}
