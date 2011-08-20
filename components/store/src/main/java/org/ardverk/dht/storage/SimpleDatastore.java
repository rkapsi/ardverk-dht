package org.ardverk.dht.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.ByteArrayValue;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.StringValue;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleDatastore extends AbstractDatastore {

    private static final Logger LOG 
        = LoggerFactory.getLogger(SimpleDatastore.class);
    
    public static final Value OK = new StringValue("OK");
    
    public static final Value NOT_FOUND = new StringValue("Not Found");
    
    public static final Value INTERNAL_ERROR = new StringValue("Internal Error");
    
    private final Map<Key, Value> map = new ConcurrentHashMap<Key, Value>();
    
    @Override
    public Value store(Contact src, Key key, Value value) {
        Value response = OK;
        try {
            ByteArrayValue copy = consumeValue(value);
            if (copy.isEmpty()) {
                if (map.remove(key) != null) {
                    response = NOT_FOUND;
                }
            } else {
                map.put(key, value);
            }
        } catch (IOException err) {
            LOG.error("IOException", err);
            response = INTERNAL_ERROR;
        }
        
        return response;
    }

    @Override
    public Value get(Contact src, Key key) {
        return map.get(key);
    }
    
    private static ByteArrayValue consumeValue(Value value) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream in = value.getContent();
        try {
            byte[] buffer = new byte[4*1024];
            int len = -1;
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            
            return new ByteArrayValue(baos.toByteArray());
        } finally {
            IoUtils.close(in);
        }
    }
}
