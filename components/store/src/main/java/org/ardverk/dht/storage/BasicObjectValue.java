package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.message.HeaderGroup;
import org.ardverk.dht.storage.io.ValueOutputStream;
import org.ardverk.io.IoUtils;

public class BasicObjectValue extends AbstractObjectValue {
    
    public BasicObjectValue() {
        super();
    }

    public BasicObjectValue(HeaderGroup headers) {
        super(headers);
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        ValueOutputStream vos = new ValueOutputStream(out);
        try {
            writeTo(vos);
        } finally {
            IoUtils.flush(vos);
        }
    }
    
    protected void writeTo(ValueOutputStream out) throws IOException {
        out.writeHeaderGroup(headers);
    }
}
