package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.message.HeaderGroup;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.io.ValueInputStream;
import org.ardverk.io.IoUtils;

public class Status extends BasicObjectValue {
    
    public static final String X_ARDVERK_MESSAGE = "X-Ardverk-Message";
    
    public static final Status SUCCESS = new Status("SUCCESS");
    
    public static final Status FAILURE = new Status("FAILURE");
    
    private Status(String message) {
        setHeader(X_ARDVERK_MESSAGE, message);
    }
    
    private Status(HeaderGroup headers) {
        super(headers);
    }
    
    public static Status valueOf(Value value) throws IOException {
        InputStream in = value.getContent();
        try {
            return valueOf(in);
        } finally {
            IoUtils.close(in);
        }
    }
    
    public static Status valueOf(InputStream in) throws IOException {
        ValueInputStream vis = new ValueInputStream(in);
        HeaderGroup headers = vis.readHeaderGroup();
        
        return new Status(headers);
    }
}
