package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.protocol.HTTP;
import org.ardverk.dht.rsrc.ByteArrayValue;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;
import org.ardverk.io.StreamUtils;

public class ByteArrayContextValue extends ContextValue {

    private ByteArrayContextValue(Context context, Value value) {
        super(context, value);
    }

    public static ByteArrayContextValue valueOf(Value value) throws IOException {
        InputStream in = value.getContent();
        try {
            return valueOf(in);
        } finally {
            IoUtils.close(in);
        }
    }
    
    public static ByteArrayContextValue valueOf(InputStream in) throws IOException {
        Context context = Context.valueOf(in);
        
        long length = 0L;
        if (context.containsHeader(HTTP.CONTENT_LEN)) {
            length = context.getContentLength();
        }
        
        byte[] data = new byte[(int)length];
        StreamUtils.readFully(in, data);
        
        return new ByteArrayContextValue(context, new ByteArrayValue(data));
    }
}
