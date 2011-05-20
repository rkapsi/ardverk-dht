package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.message.HeaderGroup;
import org.ardverk.dht.storage.io.ValueOutputStream;
import org.ardverk.io.InputOutputStream;
import org.ardverk.io.IoUtils;

public class BasicObjectValue extends AbstractObjectValue {
    
    public BasicObjectValue() {
        super();
    }

    public BasicObjectValue(HeaderGroup headers) {
        super(headers);
    }

    @Override
    public final InputStream getContent() throws IOException {
        return new InputOutputStream() {
            @Override
            protected void produce(OutputStream out) throws IOException {
                ValueOutputStream vos = new ValueOutputStream(out);
                try {
                    writeHeaders(vos);
                    writeContent(vos);
                } finally {
                    IoUtils.close(vos);
                }
            }
        };
    }
    
    protected void writeHeaders(ValueOutputStream out) throws IOException {
        out.writeHeaderGroup(headers);
    }
    
    protected void writeContent(ValueOutputStream out) throws IOException {
        
    }

    @Override
    public final void writeTo(OutputStream out) throws IOException {
        super.writeTo(out);
    }
}
