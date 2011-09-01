package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.message.Method;
import org.ardverk.dht.storage.message.Request;
import org.ardverk.dht.storage.message.Response2;
import org.ardverk.dht.storage.message.ResponseFactory2;
import org.ardverk.io.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractIndexDatastore extends AbstractDatastore {

    private static final Logger LOG 
        = LoggerFactory.getLogger(AbstractIndexDatastore.class);
    
    @Override
    public Value store(Contact src, Key key, Value value) {
        
        InputStream in = null;
        try {
            in = value.getContent();
            
            Request request = Request.valueOf(in);
            Method method = request.getMethod();
            
            Response2 response = null;
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
                case HEAD:
                    response = handleHead(src, key, request, in);
                    break;
                default:
                    throw new IllegalArgumentException("method=" + method);
            }
            
            if (response == null) {
                throw new IllegalStateException();
            }
            
            return response.commit();
            
        } catch (Exception err) {
            LOG.error("Exception", err);
            return ResponseFactory2.commit(ResponseFactory2.error(err));
        } finally {
            IoUtils.close(in);
        }
    }

    protected abstract Response2 handlePut(Contact src, Key key, 
            Request request, InputStream in) throws IOException;
    
    protected Response2 handleGet(Contact src, Key key, 
            Request request, InputStream in) throws IOException {
        
        Response2 response = handleGet(src, key, true);
        if (response == null) {
            response = ResponseFactory2.notFound();
        }
        
        return response;
    }
    
    protected abstract Response2 handleDelete(Contact src, Key key, 
            Request request, InputStream in) throws IOException;
    
    protected abstract Response2 handleHead(Contact src, Key key, 
            Request request, InputStream in) throws IOException;
    
    @Override
    public Value get(Contact src, Key key) {
        try {
            Response2 response = handleGet(src, key, false);
            if (response != null) {
                return response.commit();
            }
        } catch (Exception err) {
            LOG.error("Exception", err);
            return ResponseFactory2.commit(ResponseFactory2.error(err));
        }
        
        return null;
    }
    
    protected abstract Response2 handleGet(Contact src, 
            Key key, boolean store) throws IOException;
}
