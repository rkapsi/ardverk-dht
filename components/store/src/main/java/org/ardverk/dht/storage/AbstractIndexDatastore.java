package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.message.Method;
import org.ardverk.dht.storage.message.Request;
import org.ardverk.dht.storage.message.Response;
import org.ardverk.dht.storage.message.ResponseFactory;
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
            return ResponseFactory.commit(ResponseFactory.error(err));
        } finally {
            IoUtils.close(in);
        }
    }

    protected abstract Response handlePut(Contact src, Key key, 
            Request request, InputStream in) throws IOException;
    
    protected Response handleGet(Contact src, Key key, 
            Request request, InputStream in) throws IOException {
        
        Response response = handleGet(src, key, true);
        if (response == null) {
            response = ResponseFactory.notFound();
        }
        
        return response;
    }
    
    protected abstract Response handleDelete(Contact src, Key key, 
            Request request, InputStream in) throws IOException;
    
    protected abstract Response handleHead(Contact src, Key key, 
            Request request, InputStream in) throws IOException;
    
    @Override
    public Value get(Contact src, Key key) {
        try {
            Response response = handleGet(src, key, false);
            if (response != null) {
                return response.commit();
            }
        } catch (Exception err) {
            LOG.error("Exception", err);
            return ResponseFactory.commit(ResponseFactory.error(err));
        }
        
        return null;
    }
    
    protected abstract Response handleGet(Contact src, 
            Key key, boolean store) throws IOException;
}
