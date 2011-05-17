package org.ardverk.dht.storage.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.ardverk.dht.codec.bencode.MessageOutputStream;
import org.ardverk.dht.storage.ObjectValue.Property;

public class ValueOutputStream extends MessageOutputStream {

    public ValueOutputStream(OutputStream out) {
        super(out);
    }
    
    @Override
    protected void writeCustom(Object obj) throws IOException {
        if (obj instanceof Property) {
            writeProperty((Property)obj);
        } else {
            super.writeCustom(obj);
        }
    }
    
    public void writeProperty(Property property) throws IOException {
        writeString(property.getName());
        writeArray(property.values());
    }
    
    @Override
    public void close() throws IOException {
        super.flush();
        
        Thread.dumpStack();
    }
}
